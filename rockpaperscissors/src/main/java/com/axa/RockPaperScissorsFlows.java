package com.axa;

import static net.corda.core.contracts.ContractsDSL.requireThat;

import java.security.PublicKey;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import com.google.common.collect.ImmutableList;


import co.paralleluniverse.fibers.Suspendable;
import net.bytebuddy.implementation.bind.MethodDelegationBinder.BindingResolver.Unique;
import net.corda.core.contracts.ContractState;
import net.corda.core.contracts.LinearState;
import net.corda.core.contracts.StateAndRef;
import net.corda.core.contracts.UniqueIdentifier;
import net.corda.core.flows.CollectSignaturesFlow;
import net.corda.core.flows.FinalityFlow;
import net.corda.core.flows.FlowException;
import net.corda.core.flows.FlowLogic;
import net.corda.core.flows.FlowSession;
import net.corda.core.flows.InitiatedBy;
import net.corda.core.flows.InitiatingFlow;
import net.corda.core.flows.ReceiveFinalityFlow;
import net.corda.core.flows.SignTransactionFlow;
import net.corda.core.flows.StartableByRPC;
import net.corda.core.identity.Party;
import net.corda.core.node.services.vault.QueryCriteria;
import net.corda.core.transactions.SignedTransaction;
import net.corda.core.transactions.TransactionBuilder;
import net.corda.core.node.services.Vault.Page;
import net.corda.core.node.services.Vault.StateMetadata;
import net.corda.core.node.services.Vault.StateStatus;


public class RockPaperScissorsFlows {
    /**
     * IssueFlow starts the Rock Paper Scissors game.
     * It conveys the three involved parties: challenger, challenged, and escrow agent, and it 
     * includes the challengers choice, which must be "rock","paper", or "scissors".
     * It initiates a gather signatures flow for the challenger and escrow (TODO, why? Isn't it validated in the responding flow?)
     * It initiates a finality flow, which I believe is what kicks off the EscrowFlow...
     */
    @InitiatingFlow
    @StartableByRPC
    public static class IssueFlow extends FlowLogic<UniqueIdentifier> {

        private Party sender ;
        private Party challenged;
        private Party escrow;
        private String choice;
        public IssueFlow(Party challenged, Party escrow, String choice) {
            this.challenged=challenged;
            this.escrow=escrow;
            this.choice=choice;
        }
        @Suspendable
        @Override
        public UniqueIdentifier call() throws FlowException {
            System.out.println("IssueFlow call enter");
            this.sender = getOurIdentity();
            //TODO create our own Notary and get an instance by name
            final Party notary = getServiceHub().getNetworkMapCache().getNotaryIdentities().get(0);

            final RockPaperScissorsIssuedState output = new RockPaperScissorsIssuedState(choice,sender,challenged, escrow);
            final TransactionBuilder builder = new TransactionBuilder(notary);

            builder.addOutputState(output);
            //At this point, the signatories are the challenger and the escrow agent.
            List<PublicKey> signatories=Arrays.asList(this.sender.getOwningKey(),this.escrow.getOwningKey());
            builder.addCommand(new RockPaperScissorsContract.Commands.Issue(), signatories);
            builder.verify(getServiceHub());
            final SignedTransaction signedTransaction = getServiceHub().signInitialTransaction(builder);
            List<Party> otherParties = output.getParticipants().stream().map(el -> (Party)el).collect(Collectors.toList());
            otherParties.remove(getOurIdentity());
            //List<FlowSession> sessions = otherParties.stream().map(el -> initiateFlow(el)).collect(Collectors.toList());
            FlowSession sessions = initiateFlow(escrow);
            SignedTransaction signatureTransaction = subFlow(new CollectSignaturesFlow(signedTransaction, ImmutableList.of(sessions)));
            SignedTransaction finalisedTx = subFlow(new FinalityFlow(signatureTransaction, sessions));
            System.out.println("IssueFlow call exit");
            return finalisedTx.getTx().outputsOfType(RockPaperScissorsIssuedState.class).get(0).getLinearId();
        }
        
    }
    /**
     * The AcknowledgeIssueFlow is initiated by the IssueFlow.
     * It validates the RockPaperScissorsIssuedState.
     * It creates an EscrowedState
     * It gathers the signatures for that state 
     * - The signatorires are the escrow agent and the challenged.
     * This kicks off the AcceptChallenge flow
     */
    @InitiatingFlow
    @InitiatedBy(IssueFlow.class)
    public static class AcknowledgeIssueFlow extends FlowLogic<UniqueIdentifier> {
        private FlowSession counterpartySession;
        private RockPaperScissorsIssuedState rockPaperScissorsIssuedState;
        public AcknowledgeIssueFlow(FlowSession counterpartySession) {
            this.counterpartySession=counterpartySession;
        }
        @Suspendable
        @Override
        public UniqueIdentifier call() throws FlowException {
            System.out.println("AcknowledgeIssueFlow call enter");
            SignedTransaction signedTransaction=subFlow(new SignTransactionFlow(counterpartySession){
                @Suspendable
				@Override
				protected void checkTransaction(SignedTransaction signedTransaction) throws FlowException {
                    ContractState output = signedTransaction.getTx().getOutputs().get(0).getData();
					requireThat(require -> {
                        
                        require.using("This must be an IOU transaction", output instanceof RockPaperScissorsIssuedState);
                        return ((RockPaperScissorsIssuedState)output).getLinearId();
                    });
                    rockPaperScissorsIssuedState = (RockPaperScissorsIssuedState)output;
				}
                
            });
            subFlow(new ReceiveFinalityFlow(counterpartySession, signedTransaction.getId()));
            //This is the theoretical bit - can I just launch another flow from here?
            final RockPaperScissorsChallengeState challengeState = new RockPaperScissorsChallengeState(rockPaperScissorsIssuedState.getChallenger(),rockPaperScissorsIssuedState.getChallenged(),rockPaperScissorsIssuedState.getEscrow(), rockPaperScissorsIssuedState.getLinearId());
            final Party notary = getServiceHub().getNetworkMapCache().getNotaryIdentities().get(0);
            final TransactionBuilder builder = new TransactionBuilder(notary);
            builder.addOutputState(challengeState);
            //Now the signatories are the challenged and the escrow agent.
            List<PublicKey> signatories=Arrays.asList(challengeState.getChallenged().getOwningKey(),challengeState.getEscrow().getOwningKey());
            builder.addCommand(new RockPaperScissorsContract.Commands.Challenge(), signatories);
            builder.verify(getServiceHub());
            final SignedTransaction signedChallengeTransaction = getServiceHub().signInitialTransaction(builder);
            List<Party> otherParties = challengeState.getParticipants().stream().map(el -> (Party)el).collect(Collectors.toList());
            
            System.out.println("otherParties Before "+otherParties);
            otherParties.remove(getOurIdentity());
            System.out.println("otherParties After "+otherParties);
            System.out.println("our identitiy "+getOurIdentity());
            List<FlowSession> sessions = otherParties.stream().map(el -> initiateFlow(el)).collect(Collectors.toList());
            SignedTransaction signatureTransaction = subFlow(new CollectSignaturesFlow(signedChallengeTransaction, sessions));
            SignedTransaction finalisedTx = subFlow(new FinalityFlow(signatureTransaction, sessions));
            //Let's see what happens

            System.out.println("AcknowledgeIssueFlow call exit");
            return null;
        }

    }
    @InitiatedBy(AcknowledgeIssueFlow.class)
    public static class AcknowledgeChallengeFlow extends FlowLogic<SignedTransaction> {
        private FlowSession counterpartySession;
        public AcknowledgeChallengeFlow(FlowSession counterpartySession) {
            this.counterpartySession=counterpartySession;
        }
        @Suspendable
        @Override
        public SignedTransaction call() throws FlowException {
            System.out.println("AcceptChallengeFlow call enter");
            SignedTransaction signedTransaction=subFlow(new SignTransactionFlow(counterpartySession){
                @Suspendable
				@Override
				protected void checkTransaction(SignedTransaction signedTransaction) throws FlowException {
					// TODO Auto-generated method stub
                    //StateRef stateRef=signedTransaction.getInputs().get(0);
                    //System.out.println("StateRef "+stateRef);
				}
                
            });
            subFlow(new ReceiveFinalityFlow(counterpartySession, signedTransaction.getId()));
            System.out.println("AcceptChallengeFlow call exit");
            return null;
        }
    }

    /**
     * AcceptChallengeFlow accepts the flow, the state of which at this point should be
     * a consumed Challenge state.
     * It conveys the three involved parties: challenger, challenged, and escrow agent, and it 
     * includes the challenged party's choice, which must be "rock","paper", or "scissors".
     * It initiates a gather signatures flow for the challenger and escrow (TODO, why? Isn't it validated in the responding flow?)
     * It initiates a finality flow, which should kick off the ChallengeAcceptedFlow
     * It also starts a new flow - SettlementFlow
     */
    @InitiatingFlow
    @StartableByRPC
    public static class AcceptChallengeFlow extends FlowLogic<UniqueIdentifier> {

        private Party sender ;
        private Party challenged;
        private Party escrow;
        private String choice;
        private String linearId;
        public AcceptChallengeFlow(String linearId, Party challenged, Party escrow, String choice) {
            this.challenged=challenged;
            this.escrow=escrow;
            this.choice=choice;
            this.linearId=linearId;
            System.out.println("AcceptChallengeFlow linearId"+linearId);
        }
        @Suspendable
        @Override
        public UniqueIdentifier call() throws FlowException {
            System.out.println("AcceptChallengeFlow call enter");
            this.sender = getOurIdentity();
            System.out.println("AcceptChallengeFlow sender should be escrow "+this.sender+" "+this.escrow);
            final Party notary = getServiceHub().getNetworkMapCache().getNotaryIdentities().get(0);
            UniqueIdentifier uniqueIdentifier=new UniqueIdentifier(linearId);
            System.out.println("UniqueIdentifier ID "+uniqueIdentifier.getExternalId());
            System.out.println("UniqueIdentifier external ID "+uniqueIdentifier.getExternalId());
            System.out.println("UniqueIdentifier external external ID "+new UniqueIdentifier(uniqueIdentifier.getExternalId()));
            final RockPaperScissorsAcceptedState output = new RockPaperScissorsAcceptedState(choice,sender,challenged, escrow, uniqueIdentifier);
            final TransactionBuilder builder = new TransactionBuilder(notary);

            builder.addOutputState(output);
            //At this point, the signatories are the challenged party and the escrow agent.
            List<PublicKey> signatories=Arrays.asList(this.sender.getOwningKey(),this.escrow.getOwningKey());
            builder.addCommand(new RockPaperScissorsContract.Commands.Accept(), signatories);
            builder.verify(getServiceHub());
            final SignedTransaction signedTransaction = getServiceHub().signInitialTransaction(builder);
            List<Party> otherParties = output.getParticipants().stream().map(el -> (Party)el).collect(Collectors.toList());
            otherParties.remove(getOurIdentity());
            List<FlowSession> sessions = otherParties.stream().map(el -> initiateFlow(el)).collect(Collectors.toList());
            SignedTransaction signatureTransaction = subFlow(new CollectSignaturesFlow(signedTransaction, sessions));
            //FlowSession sessions = initiateFlow(this.sender);
            //SignedTransaction signatureTransaction = subFlow(new CollectSignaturesFlow(signedTransaction, ImmutableList.of(sessions)));
            
            SignedTransaction finalisedTx = subFlow(new FinalityFlow(signatureTransaction, sessions));
            System.out.println("AcceptChallengeFlow call exit");
            return null;//finalisedTx.getTx().outputsOfType(RockPaperScissorsIssuedState.class).get(0).getLinearId();
        }
        
    }
    @InitiatingFlow
    @InitiatedBy(AcceptChallengeFlow.class)
    public static class AcknowledgeAcceptanceFlow extends FlowLogic<SignedTransaction> {
        private FlowSession counterpartySession;
        public AcknowledgeAcceptanceFlow(FlowSession counterpartySession) {
            this.counterpartySession=counterpartySession;
        }
        RockPaperScissorsAcceptedState acceptedState;
        RockPaperScissorsIssuedState issuedState;
        @Suspendable
        @Override
        public SignedTransaction call() throws FlowException {
            System.out.println("AcknowledgeAcceptanceFlow call enter");
            
            SignedTransaction signedTransaction=subFlow(new SignTransactionFlow(counterpartySession){
                @Suspendable
				@Override
				protected void checkTransaction(SignedTransaction signedTransaction) throws FlowException {
                    ContractState output = signedTransaction.getTx().getOutputs().get(0).getData();
                    acceptedState = (RockPaperScissorsAcceptedState)output;
                    try {
                        System.out.println("AcknowledgeAcceptanceFlow linearID "+acceptedState.getLinearId());
                        java.util.UUID uuid;
                        if (acceptedState.getLinearId().getExternalId()!=null) {
                            uuid=UUID.fromString(acceptedState.getLinearId().getExternalId());
                        } else {
                            uuid=UUID.fromString(acceptedState.getLinearId().getId().toString());
                        }
                        QueryCriteria criteria = new QueryCriteria.LinearStateQueryCriteria(null,ImmutableList.of(uuid));
                        Page<LinearState> vaultQuery=getServiceHub().getVaultService().queryBy(LinearState.class, criteria);
                        List<StateAndRef<LinearState>> states=vaultQuery.getStates();
                        List<StateMetadata> statesMetaData=vaultQuery.getStatesMetadata();
                        //Until I figure out how to query by class, I'm iterating through the results looking for a 
                        //RockPaperScissorsIssuedState with the same ID

                        for (StateAndRef<LinearState> state : states) {
                            if (state.getState().getData() instanceof RockPaperScissorsIssuedState) {
                                System.out.println("RockPaperScissorsIssuedState from query "+((LinearState)state.getState().getData()).getLinearId());
                                issuedState=((RockPaperScissorsIssuedState)state.getState().getData());
                            }
                            for (StateMetadata metadata : statesMetaData) {
                                if (metadata.getRef().getTxhash().equals(state.getRef().getTxhash())) {
                                    System.out.println("We still have this bug...STATUS:"+metadata.getStatus());
                                }
                        }
                            System.out.println("STATEs from query "+((LinearState)state.getState().getData()).getLinearId());
                        }
                        
                    } catch (Exception any) {
                        any.printStackTrace();
                    }
					requireThat(require -> {
                        
                        //require.using("This must be an RockPaperScissorsAcceptedState transaction", output instanceof RockPaperScissorsAcceptedState);
                        return ((RockPaperScissorsAcceptedState)output).getLinearId();
                    });
                    
                }
                
                
            });
            //And finally, initiate the SettlementFlow to settle the game
            System.out.println("challenger:"+issuedState.getChallengerChoice()+" challenged:"+acceptedState.getChallengedChoice()+" ");
            
            Party winner=null;
            if (issuedState.getChallengerChoice().equals("rock")) {
                if (acceptedState.getChallengedChoice().equals("rock")) {
                    //No winner
                } else if (acceptedState.getChallengedChoice().equals("paper")) {
                    winner=acceptedState.getChallenged();
                } else if (acceptedState.getChallengedChoice().equals("scissors")) {
                    winner=issuedState.getChallenger();
                }
            } else if (issuedState.getChallengerChoice().equals("paper")) {
                if (acceptedState.getChallengedChoice().equals("rock")) {
                    winner=issuedState.getChallenger();
                } else if (acceptedState.getChallengedChoice().equals("paper")) {
                    //No winner
                } else if (acceptedState.getChallengedChoice().equals("scissors")) {
                    winner=issuedState.getChallenged();
                }
            } else if (issuedState.getChallengerChoice().equals("scissors")) {
                if (acceptedState.getChallengedChoice().equals("rock")) {
                    winner=issuedState.getChallenged();
                } else if (acceptedState.getChallengedChoice().equals("paper")) {
                    winner=issuedState.getChallenger();
                } else if (acceptedState.getChallengedChoice().equals("scissors")) {
                    //No winner
                }
            }
            try {
                final RockPaperScissorsSettledState settledState = new RockPaperScissorsSettledState(
                    issuedState.getChallenger(),issuedState.getChallengerChoice(),
                    acceptedState.getChallenged(), acceptedState.getChallengedChoice(), acceptedState.getEscrow(), winner, issuedState.getLinearId());
                final Party notary = getServiceHub().getNetworkMapCache().getNotaryIdentities().get(0);
                final TransactionBuilder builder = new TransactionBuilder(notary);
                builder.addOutputState(settledState);
                //Now the signatories are the challenged and the escrow agent.
                List<PublicKey> signatories=Arrays.asList(settledState.getChallenger().getOwningKey(),settledState.getChallenged().getOwningKey(),settledState.getEscrow().getOwningKey());
                builder.addCommand(new RockPaperScissorsContract.Commands.Settle(), signatories);
                builder.verify(getServiceHub());
                final SignedTransaction signedChallengeTransaction = getServiceHub().signInitialTransaction(builder);
                List<Party> otherParties = settledState.getParticipants().stream().map(el -> (Party)el).collect(Collectors.toList());
                
                otherParties.remove(getOurIdentity());
                List<FlowSession> sessions = otherParties.stream().map(el -> initiateFlow(el)).collect(Collectors.toList());
                SignedTransaction signatureTransaction = subFlow(new CollectSignaturesFlow(signedChallengeTransaction, sessions));
                SignedTransaction finalisedTx = subFlow(new FinalityFlow(signatureTransaction, sessions));
            } catch (Exception any) {
                System.out.println("Exception settling");
                any.printStackTrace();;
            }
            ///And then finalise
            subFlow(new ReceiveFinalityFlow(counterpartySession, signedTransaction.getId()));
            System.out.println("AcknowledgeAcceptanceFlow call exit");
            return null;
        }
    }
    /**
     * SettledFlow ends the Rock Paper Scissors game.
     * It's started by the escrow agent from the SettlementFlow response flow.
     * It conveys the three involved parties: challenger, challenged, and escrow agent, and it 
     * includes the challenger's choice, challenged party's choice, and the winner (which may be null,
     * case of a tie).
     * It initiates a gather signatures flow for the challenger and escrow (TODO, why? Isn't it validated in the responding flow?)
     * It initiates a finality flow, which should consume the SettleState
     */
/*
    @InitiatingFlow
    public static class SettledFlow extends FlowLogic<UniqueIdentifier> {

        private Party challenger;
        private Party challenged;
        private Party escrow;
        private String challengerChoice;
        private String challengedChoice;
        private String linearId;
        private Party winner;
        public SettledFlow(Party challenger, String challengerChoice, Party challenged, String challengedChoice, Party escrow, Party winner, String linearId) {
            this.challenger=challenger;
            this.challengerChoice=challengerChoice;
            this.challenged=challenged;
            this.challengedChoice=challengedChoice;
            this.escrow=escrow;
            this.winner=winner;
            this.linearId=linearId;
        }
        @Suspendable
        @Override
        public UniqueIdentifier call() throws FlowException {
            System.out.println("SettledFlow call enter");
            this.escrow = getOurIdentity();
            //TODO create our own Notary and get an instance by name
            final Party notary = getServiceHub().getNetworkMapCache().getNotaryIdentities().get(0);

            final RockPaperScissorsSettledState output = new RockPaperScissorsSettledState(
                challenger, challengerChoice, challenged, challengedChoice, escrow, winner, new UniqueIdentifier(linearId));
            final TransactionBuilder builder = new TransactionBuilder(notary);

            builder.addOutputState(output);
            //At this point, the signatories are the challenger and the escrow agent.
            List<PublicKey> signatories=Arrays.asList(this.escrow.getOwningKey(),this.challenger.getOwningKey(),this.challenged.getOwningKey());
            builder.addCommand(new RockPaperScissorsContract.Commands.Settle(), signatories);
            builder.verify(getServiceHub());
            final SignedTransaction signedTransaction = getServiceHub().signInitialTransaction(builder);
            List<Party> otherParties = output.getParticipants().stream().map(el -> (Party)el).collect(Collectors.toList());
            otherParties.remove(getOurIdentity());
            //List<FlowSession> sessions = otherParties.stream().map(el -> initiateFlow(el)).collect(Collectors.toList());
            FlowSession sessions = initiateFlow(escrow);
            SignedTransaction signatureTransaction = subFlow(new CollectSignaturesFlow(signedTransaction, ImmutableList.of(sessions)));
            SignedTransaction finalisedTx = subFlow(new FinalityFlow(signatureTransaction, sessions));
            System.out.println("SettledFlow call exit");
            return finalisedTx.getTx().outputsOfType(RockPaperScissorsSettledState.class).get(0).getLinearId();
        }
        
    }
*/
    @InitiatedBy(AcknowledgeAcceptanceFlow.class)
    public static class FinalisedSettlementFlow extends FlowLogic<SignedTransaction> {
        private FlowSession counterpartySession;
        public FinalisedSettlementFlow(FlowSession counterpartySession) {
            this.counterpartySession=counterpartySession;
        }
        @Suspendable
        @Override
        public SignedTransaction call() throws FlowException {
            System.out.println("FinalisedSettlementFlow call enter");
            SignedTransaction signedTransaction=subFlow(new SignTransactionFlow(counterpartySession){
                @Suspendable
				@Override
				protected void checkTransaction(SignedTransaction signedTransaction) throws FlowException {
					// TODO Auto-generated method stub
                    //StateRef stateRef=signedTransaction.getInputs().get(0);
                    //System.out.println("StateRef "+stateRef);
				}
                
            });
            subFlow(new ReceiveFinalityFlow(counterpartySession, signedTransaction.getId()));
            System.out.println("FinalisedSettlementFlow call exit");
            return null;
        }
    }
}