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
public class RockPaperScissorsAcceptedState implements LinearState {
    private final String challengedChoice;
    private final Party challenger;
    private final Party challenged;
    private final Party escrow;
    private UniqueIdentifier uniqueIdentifier;

    @Override
    public List<AbstractParty> getParticipants() {
        return Arrays.asList(challenger, escrow);
    }
    @ConstructorForDeserialization
    public RockPaperScissorsAcceptedState(String challengedChoice, Party challenger, Party challenged, Party escrow, UniqueIdentifier uniqueIdentifier) {
        this.challengedChoice = challengedChoice;
        this.challenger = challenger;
        this.challenged = challenged;
        this.escrow=escrow;
        System.out.println("RockPaperScissorsAcceptedState constructor "+uniqueIdentifier);
        this.uniqueIdentifier=uniqueIdentifier;
    }
    /*public RockPaperScissorsAcceptedState(String challengedChoice, Party challenger, Party challenged, Party escrow) {
        this.challengedChoice = challengedChoice;
        this.challenger = challenger;
        this.challenged = challenged;
        this.escrow=escrow;
        System.out.println("RockPaperScissorsAcceptedState constructor is being called for some reason");
        this.uniqueIdentifier=new UniqueIdentifier();
    }*/

    public String getChallengedChoice() {
        return challengedChoice;
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