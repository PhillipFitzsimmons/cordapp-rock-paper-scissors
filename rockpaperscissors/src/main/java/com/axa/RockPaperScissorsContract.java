package com.axa;

import net.corda.core.contracts.CommandData;
import net.corda.core.contracts.CommandWithParties;
import net.corda.core.contracts.Contract;
import net.corda.core.contracts.ContractState;
import net.corda.core.transactions.LedgerTransaction;

import static net.corda.core.contracts.ContractsDSL.requireSingleCommand;
import static net.corda.core.contracts.ContractsDSL.requireThat;

import java.security.PublicKey;

public class RockPaperScissorsContract implements Contract {
    public static final String ID = "com.axa.RockPaperScissorsContract";

    @Override
    public void verify(LedgerTransaction tx) throws IllegalArgumentException {
        ContractState state=tx.getOutput(0);
        final CommandWithParties<Commands> command = requireSingleCommand(tx.getCommands(), Commands.class);
        final Commands commandData = command.getValue();
        if (commandData instanceof Commands.Issue) {
            System.out.println("Verifying Issue "+state);
            RockPaperScissorsIssuedState rockPaperScissorsiIssuedState = tx.outputsOfType(RockPaperScissorsIssuedState.class).get(0);
            requireThat(require -> {
                require.using("Challenger choice cannot be empty:"+rockPaperScissorsiIssuedState.getChallengerChoice(),rockPaperScissorsiIssuedState.getChallengerChoice()!=null && !rockPaperScissorsiIssuedState.getChallengerChoice().equals(""));
                require.using("Challenger choice must rock, paper, or scissors", rockPaperScissorsiIssuedState.getChallengerChoice().equals("rock") || rockPaperScissorsiIssuedState.getChallengerChoice().equals("paper") || rockPaperScissorsiIssuedState.getChallengerChoice().equals("scissors"));
                require.using("Challenger and Challenged must be different parties",!rockPaperScissorsiIssuedState.getChallenger().getOwningKey().equals(rockPaperScissorsiIssuedState.getChallenged().getOwningKey()));
                require.using("Challenger and Escrow must be different parties",!rockPaperScissorsiIssuedState.getChallenger().getOwningKey().equals(rockPaperScissorsiIssuedState.getEscrow().getOwningKey()));
                require.using("Challenged and Escrow must be different parties",!rockPaperScissorsiIssuedState.getChallenged().getOwningKey().equals(rockPaperScissorsiIssuedState.getEscrow().getOwningKey()));
                //TODO can't I validate signatories at this stage? I don't want the challenger to be a signatory of the Issued game.
                return null;
            });
        } else if (commandData instanceof Commands.Challenge) {
            System.out.println("Verifying Challenge "+state);
            RockPaperScissorsChallengeState rockPaperScissorsChallengeState = tx.outputsOfType(RockPaperScissorsChallengeState.class).get(0);
            requireThat(require -> {
                require.using("Challenger and Challenged must be different parties",!rockPaperScissorsChallengeState.getChallenger().getOwningKey().equals(rockPaperScissorsChallengeState.getChallenged().getOwningKey()));
                require.using("Challenger and Escrow must be different parties",!rockPaperScissorsChallengeState.getChallenger().getOwningKey().equals(rockPaperScissorsChallengeState.getEscrow().getOwningKey()));
                require.using("Challenged and Escrow must be different parties",!rockPaperScissorsChallengeState.getChallenged().getOwningKey().equals(rockPaperScissorsChallengeState.getEscrow().getOwningKey()));
                return null;
            });
        } else if (commandData instanceof Commands.Accept) {
            System.out.println("Verifying Accept "+state);
            RockPaperScissorsAcceptedState rockPaperScissorsAcceptedState = tx.outputsOfType(RockPaperScissorsAcceptedState.class).get(0);
            requireThat(require -> {
                require.using("Challenged and Escrow must be different parties",!rockPaperScissorsAcceptedState.getChallenged().getOwningKey().equals(rockPaperScissorsAcceptedState.getEscrow().getOwningKey()));
                require.using("Challenged choice cannot be empty:"+rockPaperScissorsAcceptedState.getChallengedChoice(),rockPaperScissorsAcceptedState.getChallengedChoice()!=null && !rockPaperScissorsAcceptedState.getChallengedChoice().equals(""));
                require.using("Challenged choice must rock, paper, or scissors", rockPaperScissorsAcceptedState.getChallengedChoice().equals("rock") || rockPaperScissorsAcceptedState.getChallengedChoice().equals("paper") || rockPaperScissorsAcceptedState.getChallengedChoice().equals("scissors"));
                return null;
            });
        } else if (commandData instanceof Commands.Settle) {
            System.out.println("Verifying Settle "+state);
            RockPaperScissorsSettledState rockPaperScissorsSettledState = tx.outputsOfType(RockPaperScissorsSettledState.class).get(0);
            String winnerPublicKey=rockPaperScissorsSettledState.getWinner()!=null ? rockPaperScissorsSettledState.getWinner().getOwningKey().toString() : "dummy";
            System.out.println("STATE when command is Settle "+state+ " winnerPublicKey:"+winnerPublicKey);
            requireThat(require -> {
                require.using("Challenger and Challenged must be different parties",!rockPaperScissorsSettledState.getChallenger().getOwningKey().equals(rockPaperScissorsSettledState.getChallenged().getOwningKey()));
                require.using("Challenger and Escrow must be different parties",!rockPaperScissorsSettledState.getChallenger().getOwningKey().equals(rockPaperScissorsSettledState.getEscrow().getOwningKey()));
                require.using("Challenged and Escrow must be different parties",!rockPaperScissorsSettledState.getChallenged().getOwningKey().equals(rockPaperScissorsSettledState.getEscrow().getOwningKey()));
                require.using("Escrow cannot be winner",!winnerPublicKey.equals(rockPaperScissorsSettledState.getEscrow().getOwningKey().toString()));
                return null;
            });
        }

    }

    public interface Commands extends CommandData {
        class Issue implements Commands {}
        class Challenge implements Commands {}
        class Accept implements Commands {}
        class Settle implements Commands {}
    }

}