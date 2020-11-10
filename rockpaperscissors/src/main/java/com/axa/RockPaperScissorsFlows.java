package com.axa;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import co.paralleluniverse.fibers.Suspendable;
import net.bytebuddy.implementation.bind.MethodDelegationBinder.BindingResolver.Unique;
import net.corda.core.contracts.StateRef;
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
import net.corda.core.transactions.SignedTransaction;
import net.corda.core.transactions.TransactionBuilder;


public class RockPaperScissorsFlows {
    
    @InitiatingFlow
    @StartableByRPC
    public static class IssueFlow extends FlowLogic<UniqueIdentifier> {

        private Party sender ;
        private Party challenged;
        private Party escrow;
        private String choice;
        public IssueFlow(Party escrow, Party challenged, String choice) {
            this.challenged=challenged;
            this.escrow=escrow;
            this.choice=choice;
        }
        @Suspendable
        @Override
        public UniqueIdentifier call() throws FlowException {
            this.sender = getOurIdentity();
            //TODO create our own Notary and get an instance by name
            final Party notary = getServiceHub().getNetworkMapCache().getNotaryIdentities().get(0);

            final RockPaperScissorsIssuedState output = new RockPaperScissorsIssuedState(choice,sender,challenged, escrow);
            final TransactionBuilder builder = new TransactionBuilder(notary);

            builder.addOutputState(output);
            builder.addCommand(new RockPaperScissorsContract.Commands.Issue(), Arrays.asList(this.sender.getOwningKey(),this.challenged.getOwningKey()) );

            builder.verify(getServiceHub());
            final SignedTransaction signedTransaction = getServiceHub().signInitialTransaction(builder);
            List<Party> otherParties = output.getParticipants().stream().map(el -> (Party)el).collect(Collectors.toList());
            otherParties.remove(getOurIdentity());
            List<FlowSession> sessions = otherParties.stream().map(el -> initiateFlow(el)).collect(Collectors.toList());
            SignedTransaction signatureTransaction = subFlow(new CollectSignaturesFlow(signedTransaction, sessions));
            SignedTransaction finalisedTx = subFlow(new FinalityFlow(signatureTransaction, sessions));
            return finalisedTx.getTx().outputsOfType(RockPaperScissorsIssuedState.class).get(0).getLinearId();
        }
        
    }
    @InitiatingFlow
    @StartableByRPC
    public static class ChallengeFlow extends FlowLogic<SignedTransaction> {

        private Party sender ;
        private Party challenged;
        private String choice;
        public ChallengeFlow(Party challenged, String choice, UniqueIdentifier uniqueIdentifier) {
            this.challenged=challenged;
            this.choice=choice;
        }
        @Suspendable
        @Override
        public SignedTransaction call() throws FlowException {
            this.sender = getOurIdentity();
            //TODO create our own Notary and get an instance by name
            final Party notary = getServiceHub().getNetworkMapCache().getNotaryIdentities().get(0);

            final RockPaperScissorsIssuedState output = new RockPaperScissorsIssuedState(choice,sender,challenged, uniqueIdentifier);
            final TransactionBuilder builder = new TransactionBuilder(notary);

            builder.addOutputState(output);
            builder.addCommand(new RockPaperScissorsContract.Commands.Challenge(), Arrays.asList(this.sender.getOwningKey(),this.receiver.getOwningKey()) );

            builder.verify(getServiceHub());
            final SignedTransaction signedTransaction = getServiceHub().signInitialTransaction(builder);
            List<Party> otherParties = output.getParticipants().stream().map(el -> (Party)el).collect(Collectors.toList());
            otherParties.remove(getOurIdentity());
            List<FlowSession> sessions = otherParties.stream().map(el -> initiateFlow(el)).collect(Collectors.toList());
            SignedTransaction finalTransaction = subFlow(new CollectSignaturesFlow(signedTransaction, sessions));
            return subFlow(new FinalityFlow(finalTransaction, sessions));
        }
        
    }
    @InitiatedBy(ChallengeFlow.class)
    public static class AcceptChallengeFlow extends FlowLogic<SignedTransaction> {
        private FlowSession counterpartySession;
        public AcceptChallengeFlow(FlowSession counterpartySession) {
            this.counterpartySession=counterpartySession;
        }
        @Suspendable
        @Override
        public SignedTransaction call() throws FlowException {
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
            return null;
        }

    }
}