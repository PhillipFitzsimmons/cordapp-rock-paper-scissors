package com.axa;

import java.util.Arrays;
import java.util.List;

import net.corda.core.contracts.BelongsToContract;
import net.corda.core.contracts.ContractState;
import net.corda.core.contracts.LinearState;
import net.corda.core.contracts.UniqueIdentifier;
import net.corda.core.identity.AbstractParty;
import net.corda.core.identity.Party;

@BelongsToContract(RockPaperScissorsContract.class)
public class RockPaperScissorsIssuedState implements LinearState {
    private final String challengerChoice;
    private final Party challenger;
    private final Party challenged;
    private final Party escrow;
    private UniqueIdentifier uniqueIdentifier;

    @Override
    public List<AbstractParty> getParticipants() {
        return Arrays.asList(challenger, escrow);
    }

    public RockPaperScissorsIssuedState(String challengerChoice, Party challenger, Party challenged, Party escrow, UniqueIdentifier uniqueIdentifier) {
        this.challengerChoice = challengerChoice;
        this.challenger = challenger;
        this.challenged = challenged;
        this.escrow=escrow;
        this.uniqueIdentifier=uniqueIdentifier;
    }
    public RockPaperScissorsIssuedState(String challengerChoice, Party challenger, Party challenged, Party escrow) {
        this.challengerChoice = challengerChoice;
        this.challenger = challenger;
        this.challenged = challenged;
        this.escrow=escrow;
        this.uniqueIdentifier=new UniqueIdentifier();
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
    public Party getEscrow() {
        return escrow;
    }

    @Override
    public UniqueIdentifier getLinearId() {
        return this.uniqueIdentifier;
    }

}