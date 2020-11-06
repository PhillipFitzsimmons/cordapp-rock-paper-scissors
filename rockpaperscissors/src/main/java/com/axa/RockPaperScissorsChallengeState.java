package com.axa;

import java.util.Arrays;
import java.util.List;

import net.corda.core.contracts.BelongsToContract;
import net.corda.core.contracts.ContractState;
import net.corda.core.identity.AbstractParty;
import net.corda.core.identity.Party;

@BelongsToContract(RockPaperScissorsContract.class)
public class RockPaperScissorsChallengeState implements ContractState {
    private final String challengerChoice;
    private final Party challenger;
    private final Party challenged;

    @Override
    public List<AbstractParty> getParticipants() {
        return Arrays.asList(challenger, challenged);
    }

    public RockPaperScissorsChallengeState(String challengerChoice, Party challenger, Party challenged) {
        this.challengerChoice = challengerChoice;
        this.challenger = challenger;
        this.challenged = challenged;
    }

    public String getChallengerChoice() {
        return challengerChoice;
    }

    public Party getChallenger() {
        return challenger;
    }

    public Party getChallenged() {
        return challenged;
    }

}