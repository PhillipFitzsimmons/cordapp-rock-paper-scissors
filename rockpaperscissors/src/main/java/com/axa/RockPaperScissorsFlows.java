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
import net.corda.core.node.ServiceHub;
import net.corda.core.node.services.Vault.Page;
import net.corda.core.node.services.Vault.StateMetadata;
import net.corda.core.node.services.Vault.StateStatus;

public class RockPaperScissorsFlows {
    /**
     * IssueFlow starts the Rock Paper Scissors game. It conveys the three involved
     * parties: challenger, challenged, and escrow agent, and it includes the
     * challengers choice, which must be "rock","paper", or "scissors". It initiates
     * a gather signatures flow for the challenger and escrow (TODO, why? Isn't it
     * validated in the responding flow?) It initiates a finality flow, which I
     * believe is what kicks off the EscrowFlow...
     */
    @InitiatingFlow
    @StartableByRPC
    public static class IssueFlow extends FlowLogic<UniqueIdentifier> {

        private Party sender;
        private Party challenged;
        private Party escrow;
        private String choice;

        public IssueFlow(Party challenged, Party escrow, String choice) {
            this.challenged = challenged;
            this.escrow = escrow;
            this.choice = choice;
        }

        @Suspendable
        @Override
        public UniqueIdentifier call() throws FlowException {
            System.out.println("IssueFlow call enter");
            this.sender = getOurIdentity();
            // TODO create our own Notary and get an instance by name
            final Party notary = getServiceHub().getNetworkMapCache().getNotaryIdentities().get(0);

            final RockPaperScissorsIssuedState output = new RockPaperScissorsIssuedState(choice, sender, challenged,
                    escrow);
            final TransactionBuilder builder = new TransactionBuilder(notary);

            builder.addOutputState(output);
            // At this point, the signatories are the challenger and the escrow agent.
            List<PublicKey> signatories = Arrays.asList(this.sender.getOwningKey(), this.escrow.getOwningKey());
            builder.addCommand(new RockPaperScissorsContract.Commands.Issue(), signatories);
            builder.verify(getServiceHub());
            final SignedTransaction signedTransaction = getServiceHub().signInitialTransaction(builder);
            List<Party> otherParties = output.getParticipants().stream().map(el -> (Party) el)
                    .collect(Collectors.toList());
            otherParties.remove(getOurIdentity());
            // List<FlowSession> sessions = otherParties.stream().map(el ->
            // initiateFlow(el)).collect(Collectors.toList());
            FlowSession sessions = initiateFlow(escrow);
            SignedTransaction signatureTransaction = subFlow(
                    new CollectSignaturesFlow(signedTransaction, ImmutableList.of(sessions)));
            SignedTransaction finalisedTx = subFlow(new FinalityFlow(signatureTransaction, sessions));
            System.out.println("IssueFlow call exit");
            return finalisedTx.getTx().outputsOfType(RockPaperScissorsIssuedState.class).get(0).getLinearId();
        }

    }

    /**
     * The AcknowledgeIssueFlow is initiated by the IssueFlow. It validates the
     * RockPaperScissorsIssuedState. It creates an EscrowedState It gathers the
     * signatures for that state - The signatorires are the escrow agent and the
     * challenged. This kicks off the AcceptChallenge flow
     */
    @InitiatingFlow
    @InitiatedBy(IssueFlow.class)
    public static class AcknowledgeIssueFlow extends FlowLogic<SignedTransaction> {
        private FlowSession counterpartySession;
        private RockPaperScissorsIssuedState rockPaperScissorsIssuedState;

        public AcknowledgeIssueFlow(FlowSession counterpartySession) {
            this.counterpartySession = counterpartySession;
        }

        @Suspendable
        @Override
        public SignedTransaction call() throws FlowException {
            System.out.println("AcknowledgeIssueFlow call enter");
            SignedTransaction signedTransaction = subFlow(new SignTransactionFlow(counterpartySession) {
                @Suspendable
                @Override
                protected void checkTransaction(SignedTransaction signedTransaction) throws FlowException {
                    ContractState output = signedTransaction.getTx().getOutputs().get(0).getData();
                    requireThat(require -> {

                        require.using("This must be an IOU transaction",
                                output instanceof RockPaperScissorsIssuedState);
                        return ((RockPaperScissorsIssuedState) output).getLinearId();
                    });
                    rockPaperScissorsIssuedState = (RockPaperScissorsIssuedState) output;
                }

            });
            subFlow(new ReceiveFinalityFlow(counterpartySession, signedTransaction.getId()));
            // This is the theoretical bit - can I just launch another flow from here?
            final RockPaperScissorsChallengeState challengeState = new RockPaperScissorsChallengeState(
                    rockPaperScissorsIssuedState.getChallenger(), rockPaperScissorsIssuedState.getChallenged(),
                    rockPaperScissorsIssuedState.getEscrow(), rockPaperScissorsIssuedState.getLinearId());
            final Party notary = getServiceHub().getNetworkMapCache().getNotaryIdentities().get(0);
            final TransactionBuilder builder = new TransactionBuilder(notary);
            builder.addOutputState(challengeState);

            // Input state. This mess is just to acquire the previous state, because the
            // argument to addInputState
            StateAndRef<LinearState> inputState = fetch(RockPaperScissorsIssuedState.class,
                    rockPaperScissorsIssuedState.getLinearId().getId(), getServiceHub());
            System.out.println("AcknowledgeIssueFlow call inputState " + inputState);
            if (inputState != null) {
                // It's worth nothing here for future reference that when I add the input state
                // I get an error
                // in the subsequent response flow because the Challenger isn't in the
                // FinalityFlow. As near
                // as I can tell, this is an evolution of Corda 4 which requires the
                // FinalityFlow to include all
                // participants (signatories or not) and that includes particpants to the input,
                // which I suppose
                // makes sense but it all seems rather arbitrary.
                // New theory - this isn't where I should be consuming the state - it should be
                // at the end, when I have all
                // signatories on hand.
                // builder.addInputState(inputState);
                // UPDATE for this git commit - it worked. See comments in AcknowledgeAcceptanceFlow
            } else {
                System.out.println("We didn't find the input state.");
            }
            // Now we should have added the input state
            // TODO obviously rationalise this.

            // Now the signatories are the challenged and the escrow agent.
            List<PublicKey> signatories = Arrays.asList(challengeState.getChallenged().getOwningKey(),
                    challengeState.getEscrow().getOwningKey());
            builder.addCommand(new RockPaperScissorsContract.Commands.Challenge(), signatories);
            builder.verify(getServiceHub());
            final SignedTransaction signedChallengeTransaction = getServiceHub().signInitialTransaction(builder);
            List<Party> otherParties = challengeState.getParticipants().stream().map(el -> (Party) el)
                    .collect(Collectors.toList());

            System.out.println("otherParties Before " + otherParties);
            otherParties.remove(getOurIdentity());
            List<FlowSession> sessions = otherParties.stream().map(el -> initiateFlow(el)).collect(Collectors.toList());
            SignedTransaction signatureTransaction = subFlow(
                    new CollectSignaturesFlow(signedChallengeTransaction, sessions));
            SignedTransaction finalisedTx = subFlow(new FinalityFlow(signatureTransaction, sessions));
            
            System.out.println("AcknowledgeIssueFlow call exit");
            return finalisedTx;
        }

    }

    @InitiatedBy(AcknowledgeIssueFlow.class)
    public static class AcknowledgeChallengeFlow extends FlowLogic<SignedTransaction> {
        private FlowSession counterpartySession;

        public AcknowledgeChallengeFlow(FlowSession counterpartySession) {
            this.counterpartySession = counterpartySession;
        }

        @Suspendable
        @Override
        public SignedTransaction call() throws FlowException {
            System.out.println("AcknowledgeIssueFlow call enter");
            SignedTransaction signedTransaction = subFlow(new SignTransactionFlow(counterpartySession) {
                @Suspendable
                @Override
                protected void checkTransaction(SignedTransaction signedTransaction) throws FlowException {
                    // TODO Auto-generated method stub
                    // StateRef stateRef=signedTransaction.getInputs().get(0);
                    // System.out.println("StateRef "+stateRef);
                }

            });
            SignedTransaction finalTransaction = subFlow(
                    new ReceiveFinalityFlow(counterpartySession, signedTransaction.getId()));
            System.out.println("AcknowledgeIssueFlow call exit");
            return finalTransaction;
        }
    }

    /**
     * AcceptChallengeFlow accepts the flow, the state of which at this point should
     * be a consumed Challenge state. It conveys the three involved parties:
     * challenger, challenged, and escrow agent, and it includes the challenged
     * party's choice, which must be "rock","paper", or "scissors". It initiates a
     * gather signatures flow for the challenger and escrow (TODO, why? Isn't it
     * validated in the responding flow?) It initiates a finality flow, which should
     * kick off the ChallengeAcceptedFlow It also starts a new flow - SettlementFlow
     */
    @InitiatingFlow
    @StartableByRPC
    public static class AcceptChallengeFlow extends FlowLogic<SignedTransaction> {

        private Party sender;
        private Party challenged;
        private Party escrow;
        private String choice;
        private String linearId;

        public AcceptChallengeFlow(String linearId, Party challenged, Party escrow, String choice) {
            this.challenged = challenged;
            this.escrow = escrow;
            this.choice = choice;
            this.linearId = linearId;
            System.out.println("AcceptChallengeFlow linearId" + linearId);
        }

        @Suspendable
        @Override
        public SignedTransaction call() throws FlowException {
            System.out.println("AcceptChallengeFlow call enter");
            this.sender = getOurIdentity();
            final Party notary = getServiceHub().getNetworkMapCache().getNotaryIdentities().get(0);
            UniqueIdentifier uniqueIdentifier = new UniqueIdentifier(linearId);
            final RockPaperScissorsAcceptedState output = new RockPaperScissorsAcceptedState(choice, sender, challenged,
                    escrow, uniqueIdentifier);
            final TransactionBuilder builder = new TransactionBuilder(notary);

            builder.addOutputState(output);
            // At this point, the signatories are the challenged party and the escrow agent.
            List<PublicKey> signatories = Arrays.asList(this.sender.getOwningKey(), this.escrow.getOwningKey());
            builder.addCommand(new RockPaperScissorsContract.Commands.Accept(), signatories);
            builder.verify(getServiceHub());
            final SignedTransaction signedTransaction = getServiceHub().signInitialTransaction(builder);
            List<Party> otherParties = output.getParticipants().stream().map(el -> (Party) el)
                    .collect(Collectors.toList());
            otherParties.remove(getOurIdentity());
            List<FlowSession> sessions = otherParties.stream().map(el -> initiateFlow(el)).collect(Collectors.toList());
            SignedTransaction signatureTransaction = subFlow(new CollectSignaturesFlow(signedTransaction, sessions));
            
            SignedTransaction finalisedTx = subFlow(new FinalityFlow(signatureTransaction, sessions));
            System.out.println("AcceptChallengeFlow call exit");
            return finalisedTx;
        }

    }
    /**
     * This poorly name flow is actually the last flow, and it's sent from the Challenged party
     * to the Escrow party, who calculates the winner and then gets all parties to sign the final state
     * and all outstanding input states.
     */
    @InitiatingFlow
    @InitiatedBy(AcceptChallengeFlow.class)
    public static class AcknowledgeAcceptanceFlow extends FlowLogic<SignedTransaction> {
        private FlowSession counterpartySession;

        public AcknowledgeAcceptanceFlow(FlowSession counterpartySession) {
            this.counterpartySession = counterpartySession;
        }

        RockPaperScissorsAcceptedState acceptedState;
        RockPaperScissorsIssuedState issuedState;

        @Suspendable
        @Override
        public SignedTransaction call() throws FlowException {
            System.out.println("AcknowledgeAcceptanceFlow call enter");

            SignedTransaction signedTransaction = subFlow(new SignTransactionFlow(counterpartySession) {
                @Suspendable
                @Override
                protected void checkTransaction(SignedTransaction signedTransaction) throws FlowException {
                    ContractState output = signedTransaction.getTx().getOutputs().get(0).getData();
                    acceptedState = (RockPaperScissorsAcceptedState) output;
                        System.out.println("AcknowledgeAcceptanceFlow linearID " + acceptedState.getLinearId());
                        java.util.UUID uuid;
                        //I recognise that this is a workaround to something I don't understand
                        if (acceptedState.getLinearId().getExternalId() != null) {
                            uuid = UUID.fromString(acceptedState.getLinearId().getExternalId());
                        } else {
                            uuid = UUID.fromString(acceptedState.getLinearId().getId().toString());
                        }
                        StateAndRef<LinearState> ref=fetch(RockPaperScissorsIssuedState.class, uuid, getServiceHub());
                        issuedState=(RockPaperScissorsIssuedState)ref.getState().getData();
                    requireThat(require -> {

                        // require.using("This must be an RockPaperScissorsAcceptedState transaction",
                        // output instanceof RockPaperScissorsAcceptedState);
                        return ((RockPaperScissorsAcceptedState) output).getLinearId();
                    });

                }

            });
            // And finally, initiate the SettlementFlow to settle the game
            System.out.println("challenger:" + issuedState.getChallengerChoice() + " challenged:"
                    + acceptedState.getChallengedChoice() + " ");

            Party winner = null;
            if (issuedState.getChallengerChoice().equals("rock")) {
                if (acceptedState.getChallengedChoice().equals("rock")) {
                    // No winner
                } else if (acceptedState.getChallengedChoice().equals("paper")) {
                    winner = acceptedState.getChallenged();
                } else if (acceptedState.getChallengedChoice().equals("scissors")) {
                    winner = issuedState.getChallenger();
                }
            } else if (issuedState.getChallengerChoice().equals("paper")) {
                if (acceptedState.getChallengedChoice().equals("rock")) {
                    winner = issuedState.getChallenger();
                } else if (acceptedState.getChallengedChoice().equals("paper")) {
                    // No winner
                } else if (acceptedState.getChallengedChoice().equals("scissors")) {
                    winner = issuedState.getChallenged();
                }
            } else if (issuedState.getChallengerChoice().equals("scissors")) {
                if (acceptedState.getChallengedChoice().equals("rock")) {
                    winner = issuedState.getChallenged();
                } else if (acceptedState.getChallengedChoice().equals("paper")) {
                    winner = issuedState.getChallenger();
                } else if (acceptedState.getChallengedChoice().equals("scissors")) {
                    // No winner
                }
            }
            final RockPaperScissorsSettledState settledState = new RockPaperScissorsSettledState(
                    issuedState.getChallenger(), issuedState.getChallengerChoice(), acceptedState.getChallenged(),
                    acceptedState.getChallengedChoice(), acceptedState.getEscrow(), winner, issuedState.getLinearId());
            final Party notary = getServiceHub().getNetworkMapCache().getNotaryIdentities().get(0);
            final TransactionBuilder builder = new TransactionBuilder(notary);
            builder.addOutputState(settledState);
            /*
            This is the big epiphany from the previous version.
            Previously, either nothing was consumed or I was getting errors when I tried to complete my transactions.
            The problem was that I was consuming them in the wrong place - in a transaction in which the signatories to the
            input states weren't present.
            So in this, the final transaction, I get all outstanding (unconsumed) states and add them to this transaction as inputs.
            It remains to be determined if it doesn't make more sense - in so far as implement Rock Paper Scissors in Corda makes sense -
            if some of these states - notably the IssuedState - shouldn't be consumed when it's just between the Escrow Agent and Challenger,
            but this is now working and hence seems like a good place for a commit to git.
            */
            List<StateAndRef<LinearState>> inputStates = fetchAll(issuedState.getLinearId().getId(), getServiceHub());
            for (StateAndRef<LinearState> inputState : inputStates) {
                builder.addInputState(inputState);
            }
            // Now the signatories are everybody.
            List<PublicKey> signatories = Arrays.asList(settledState.getChallenger().getOwningKey(),
                    settledState.getChallenged().getOwningKey(), settledState.getEscrow().getOwningKey());
            builder.addCommand(new RockPaperScissorsContract.Commands.Settle(), signatories);
            builder.verify(getServiceHub());
            final SignedTransaction signedChallengeTransaction = getServiceHub().signInitialTransaction(builder);
            List<Party> otherParties = settledState.getParticipants().stream().map(el -> (Party) el)
                    .collect(Collectors.toList());

            otherParties.remove(getOurIdentity());
            List<FlowSession> sessions = otherParties.stream().map(el -> initiateFlow(el)).collect(Collectors.toList());
            SignedTransaction signatureTransaction = subFlow(
                    new CollectSignaturesFlow(signedChallengeTransaction, sessions));
            subFlow(new FinalityFlow(signatureTransaction, sessions));
            // return finalisedTx;
            /// And then finalise
            System.out.println("AcknowledgeAcceptanceFlow about to finalise " + counterpartySession);
            SignedTransaction finalFinalisedTx = subFlow(
                    new ReceiveFinalityFlow(counterpartySession, signedTransaction.getId()));
            System.out.println("AcknowledgeAcceptanceFlow call exit");
            return finalFinalisedTx;
        }
    }

    @InitiatedBy(AcknowledgeAcceptanceFlow.class)
    public static class FinalisedSettlementFlow extends FlowLogic<SignedTransaction> {
        private FlowSession counterpartySession;

        public FinalisedSettlementFlow(FlowSession counterpartySession) {
            this.counterpartySession = counterpartySession;
        }

        @Suspendable
        @Override
        public SignedTransaction call() throws FlowException {
            System.out.println("FinalisedSettlementFlow call enter " + counterpartySession.getCounterparty());
            SignedTransaction signedTransaction = subFlow(new SignTransactionFlow(counterpartySession) {
                @Suspendable
                @Override
                protected void checkTransaction(SignedTransaction signedTransaction) throws FlowException {
                    // TODO Auto-generated method stub
                    // StateRef stateRef=signedTransaction.getInputs().get(0);
                    // System.out.println("StateRef "+stateRef);
                }

            });
            SignedTransaction finalisedTx = subFlow(
                    new ReceiveFinalityFlow(counterpartySession, signedTransaction.getId()));
            System.out.println("FinalisedSettlementFlow call exit " + getOurIdentity());
            return finalisedTx;
        }
    }

    static StateAndRef<LinearState> fetch(Class cl, UUID lid, ServiceHub serviceHub) {
        QueryCriteria criteria = new QueryCriteria.LinearStateQueryCriteria(null, ImmutableList.of(lid));
        Page<LinearState> vaultQuery = serviceHub.getVaultService().queryBy(LinearState.class, criteria);
        List<StateAndRef<LinearState>> states = vaultQuery.getStates();
        for (StateAndRef<LinearState> state : states) {
            if (state.getState().getData().getClass().equals(cl)) {
                return state;
            }
        }
        return null;
    }

    static List<StateAndRef<LinearState>> fetchAll(UUID lid, ServiceHub serviceHub) {
        QueryCriteria criteria = new QueryCriteria.LinearStateQueryCriteria(null, ImmutableList.of(lid));
        Page<LinearState> vaultQuery = serviceHub.getVaultService().queryBy(LinearState.class, criteria);
        return vaultQuery.getStates();
    }
}