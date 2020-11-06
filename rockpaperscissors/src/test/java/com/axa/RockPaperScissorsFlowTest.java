package com.axa;

import net.corda.testing.node.StartedMockNode;
import net.corda.testing.node.TestCordapp;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import net.corda.core.concurrent.CordaFuture;
import net.corda.core.transactions.SignedTransaction;
import net.corda.testing.node.MockNetwork;
import com.google.common.collect.ImmutableList;
import net.corda.testing.node.MockNetworkParameters;
import static org.junit.Assert.assertEquals;

public class RockPaperScissorsFlowTest {
    private MockNetwork network;
    private StartedMockNode a;
    private StartedMockNode b;
    @Before
    public void setup() {
        network = new MockNetwork(new MockNetworkParameters().withCordappsForAllNodes(ImmutableList.of(
                TestCordapp.findCordapp("com.axa"))));
        a = network.createPartyNode(null);
        b = network.createPartyNode(null);
        // For real nodes this happens automatically, but we have to manually register the flow for tests.
        for (StartedMockNode node : ImmutableList.of(a, b)) {
            node.registerInitiatedFlow(RockPaperScissorsFlows.AcceptChallengeFlow.class);
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
        RockPaperScissorsFlows.ChallengeFlow challengeFlow = new RockPaperScissorsFlows.ChallengeFlow (b.getInfo().getLegalIdentities().get(0), "rock");
        CordaFuture<SignedTransaction> future = a.startFlow(challengeFlow);
        network.runNetwork();
        SignedTransaction signedTx = future.get();
        for (StartedMockNode node : ImmutableList.of(a, b)) {
            SignedTransaction stx=node.getServices().getValidatedTransactions().getTransaction(signedTx.getId());
            System.out.println("Signed Transaction for node "+node+" "+stx);
            assertEquals(signedTx, stx);
        }
    }
}
