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
    private final Party challenged;
    private final Party escrow;
    private UniqueIdentifier uniqueIdentifier;

    @Override
    public List<AbstractParty> getParticipants() {
        return Arrays.asList(challenged, escrow);
    }
    @ConstructorForDeserialization
    public RockPaperScissorsAcceptedState(String challengedChoice, Party challenged, Party escrow, UniqueIdentifier uniqueIdentifier) {
        this.challengedChoice = challengedChoice;
        this.challenged = challenged;
        this.escrow=escrow;
        System.out.println("RockPaperScissorsAcceptedState constructor "+uniqueIdentifier);
        this.uniqueIdentifier=uniqueIdentifier;
    }

    public String getChallengedChoice() {
        return challengedChoice;
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