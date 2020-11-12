package com.axa;

import java.util.Arrays;
import java.util.List;

import net.corda.core.contracts.BelongsToContract;
import net.corda.core.contracts.ContractState;
import net.corda.core.contracts.LinearState;
import net.corda.core.contracts.UniqueIdentifier;
import net.corda.core.identity.AbstractParty;
import net.corda.core.identity.Party;
import net.corda.core.serialization.ConstructorForDeserialization;

@BelongsToContract(RockPaperScissorsContract.class)
public class RockPaperScissorsSettledState implements LinearState {
    private final Party challenger;
    private final String challengerChoice;
    private final Party challenged;
    private final String challengedChoice;
    private final Party escrow;
    private final Party winner;
    private UniqueIdentifier uniqueIdentifier;

    @Override
    public List<AbstractParty> getParticipants() {
        return Arrays.asList(challenger, challenged, escrow);
    }
    @ConstructorForDeserialization
    public RockPaperScissorsSettledState(Party challenger, String challengerChoice, Party challenged, String challengedChoice, Party escrow, Party winner, UniqueIdentifier uniqueIdentifier) {
        this.challenger = challenger;
        this.challengerChoice = challengerChoice;
        this.challenged = challenged;
        this.challengedChoice = challengedChoice;
        this.escrow=escrow;
        this.winner=winner;
        this.uniqueIdentifier=uniqueIdentifier;
    }
    public RockPaperScissorsSettledState(Party challenger, String challengerChoice, Party challenged, String challengedChoice, Party escrow, Party winner) {
        this.challenger = challenger;
        this.challengerChoice = challengerChoice;
        this.challenged = challenged;
        this.challengedChoice = challengedChoice;
        this.escrow=escrow;
        this.winner=winner;
        this.uniqueIdentifier=new UniqueIdentifier();
    }


    @Override
    public UniqueIdentifier getLinearId() {
        return this.uniqueIdentifier;
    }

    public Party getChallenger() {
        return challenger;
    }

    public String getChallengerChoice() {
        return challengerChoice;
    }

    public Party getChallenged() {
        return challenged;
    }

    public String getChallengedChoice() {
        return challengedChoice;
    }

    public Party getEscrow() {
        return escrow;
    }

    public Party getWinner() {
        return winner;
    }

}