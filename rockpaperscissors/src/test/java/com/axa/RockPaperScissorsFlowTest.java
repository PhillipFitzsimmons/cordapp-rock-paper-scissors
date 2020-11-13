package com.axa;

import net.corda.testing.node.StartedMockNode;
import net.corda.testing.node.TestCordapp;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import net.corda.core.concurrent.CordaFuture;
import net.corda.core.contracts.LinearState;
import net.corda.core.contracts.StateAndRef;
import net.corda.core.contracts.UniqueIdentifier;
import net.corda.core.node.services.Vault;
import net.corda.core.node.services.Vault.StateMetadata;
import net.corda.core.transactions.SignedTransaction;
import net.corda.testing.node.MockNetwork;
import com.google.common.collect.ImmutableList;
import net.corda.testing.node.MockNetworkParameters;
import static org.junit.Assert.assertEquals;

import java.util.List;

public class RockPaperScissorsFlowTest {
    private MockNetwork network;
    private StartedMockNode a;
    private StartedMockNode b;
    private StartedMockNode c;
    @Before
    public void setup() {
        network = new MockNetwork(new MockNetworkParameters().withCordappsForAllNodes(ImmutableList.of(
                TestCordapp.findCordapp("com.axa"))));
        a = network.createPartyNode(null);
        b = network.createPartyNode(null);
        c = network.createPartyNode(null);
        // For real nodes this happens automatically, but we have to manually register the flow for tests.
        for (StartedMockNode node : ImmutableList.of(a, b)) {
            node.registerInitiatedFlow(RockPaperScissorsFlows.AcknowledgeChallengeFlow.class);
        }
        network.runNetwork();
        System.out.println("startup network "+network);
    }

    @After
    public void tearDown() {
        System.out.println("tearDown network "+network);
        if (network!=null) network.stopNodes();
    }

    @Test
    public void happyTest() throws Exception  {
        RockPaperScissorsFlows.IssueFlow issueFlow = new RockPaperScissorsFlows.IssueFlow (b.getInfo().getLegalIdentities().get(0), c.getInfo().getLegalIdentities().get(0),"rock");
        CordaFuture<UniqueIdentifier> future = a.startFlow(issueFlow);
        network.runNetwork();
        UniqueIdentifier uniqueIdentifier = future.get();
        System.out.println("happyTest uniqueIdentifier "+uniqueIdentifier);
        for (StartedMockNode node : ImmutableList.of(a, b)) {
            /*SignedTransaction stx=node.getServices().getValidatedTransactions().getTransaction(uniqueIdentifier);
            System.out.println("Signed Transaction for node "+node+" "+stx);
            assertEquals(signedTx, stx);*/
            System.out.println("happyTest "+node);
        }
        System.out.println("Now we'll try accepting the challenge");
        RockPaperScissorsFlows.AcceptChallengeFlow acceptFlow = new RockPaperScissorsFlows.AcceptChallengeFlow (uniqueIdentifier+"", b.getInfo().getLegalIdentities().get(0), c.getInfo().getLegalIdentities().get(0),"scissors");
        CordaFuture<SignedTransaction> futureAccept = b.startFlow(acceptFlow);
        network.runNetwork();
        //uniqueIdentifier = futureAccept.get();
        //System.out.println("..."+uniqueIdentifier);
        Vault.Page<LinearState> states = a.getServices().getVaultService().queryBy(LinearState.class);
        List<StateAndRef<LinearState>> linearStates = states.getStates();
        for (StateAndRef<LinearState> linearState : linearStates) {
            System.out.println("STATE node a "+stateToString(states.getStatesMetadata(), linearState));
        }
        states = b.getServices().getVaultService().queryBy(LinearState.class);
        linearStates = states.getStates();
        for (StateAndRef<LinearState> linearState : linearStates) {
            System.out.println("STATE node b "+stateToString(states.getStatesMetadata(), linearState));
        }
        states = c.getServices().getVaultService().queryBy(LinearState.class);
        linearStates = states.getStates();
        for (StateAndRef<LinearState> linearState : linearStates) {
            System.out.println("STATE node c "+stateToString(states.getStatesMetadata(), linearState));
        }
    }
    private String stateToString(List<StateMetadata> metadatas, StateAndRef<LinearState> state) {
        String string="class:"+state.getState().getData().getClass()+", ";
        string+="linearId:"+state.getState().getData().getLinearId();
        for (StateMetadata metadata : metadatas) {
            if (metadata.getRef().getTxhash()==state.getRef().getTxhash()) {
                string+="status:"+metadata.getStatus();
            }
        }
        return string;
    }
}
