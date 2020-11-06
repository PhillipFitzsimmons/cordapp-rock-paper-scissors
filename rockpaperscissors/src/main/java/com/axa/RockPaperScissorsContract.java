package com.axa;

import net.corda.core.contracts.CommandData;
import net.corda.core.contracts.CommandWithParties;
import net.corda.core.contracts.Contract;
import net.corda.core.transactions.LedgerTransaction;

import static net.corda.core.contracts.ContractsDSL.requireSingleCommand;
import static net.corda.core.contracts.ContractsDSL.requireThat;

public class RockPaperScissorsContract implements Contract {
    public static final String ID = "com.axa.RockPaperScissorsContract";

    @Override
    public void verify(LedgerTransaction tx) throws IllegalArgumentException {
        // Single command for now, but I suspect we'll be revisiting this
        final CommandWithParties<Commands> command = requireSingleCommand(tx.getCommands(), Commands.class);
        final Commands commandData = command.getValue();
        if (commandData.equals(new Commands.Challenge())) {
            //TODO this seems dodgy.
            RockPaperScissorsChallengeState rockPaperScissorsChallengeState = tx.outputsOfType(RockPaperScissorsChallengeState.class).get(0);
            requireThat(require -> {
                require.using("Challenger choice cannot be empty",rockPaperScissorsChallengeState.getChallengerChoice()==null || rockPaperScissorsChallengeState.getChallengerChoice().equals(""));
                //TODO challenger is not this party
                //TODO challenged is this party
                return null;
            });
        }

    }

    public interface Commands extends CommandData {
        class Challenge implements Commands {}
    }

}