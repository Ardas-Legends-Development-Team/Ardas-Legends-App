package com.ardaslegends.service.exceptions.logic.faction;

import com.ardaslegends.service.exceptions.logic.LogicException;

public class FactionServiceException extends LogicException {

    public static final String NO_FACTION_WITH_NAME_FOUND_AND_ALL = "Could not find Faction with name '%s'.\nAvailable factions are: %s";
    public static final String NO_FACTION_WITH_NAME_FOUND = "Could not find Faction with name '%s'.\nAvailable factions are: %s";
    public static final String FACTION_LEADER_MUST_BE_OF_SAME_FACTION = "The faction leader must be of the same faction";

    public static final String NEGATIVE_STOCKPILE_ADD_NOT_SUPPORTED = "You are trying to add a negative amount to the stockpile. Please use /stockpile remove to lower food stockpiles";
    public static final String NEGATIVE_STOCKPILE_SUBTRACT_NOT_SUPPORTED = "You are trying to subtract a negative amount to the stockpile. Please use /stockpile add to increase food stockpiles";
    public static final String NOT_ENOUGH_FOOD_IN_STOCKPILE_TO_COVER_COST = "There is not enough food in the stockpile of '%s' \n'%d' Stacks are in stockpile, cannot subtract/pay '%d' Stacks because the amount would fall below zero!";
    public static final String NO_FACTION_LEADER = "The faction %s does not have a faction leader!";
    public static final String ROLE_ID_ALREADY_USED = "The role id [%s] is already used by faction %s";

    public static FactionServiceException noFactionLeader(String factionName) {return new FactionServiceException(NO_FACTION_LEADER.formatted(factionName)); }
    public static FactionServiceException noFactionLeader(String factionName, String extraMessage) {return new FactionServiceException(NO_FACTION_LEADER.formatted(factionName) + "\n" + extraMessage); }
    public static FactionServiceException notEnoughFoodInStockpile(String faction, int amountInStockpile, int amountToRemove) {return new FactionServiceException(NOT_ENOUGH_FOOD_IN_STOCKPILE_TO_COVER_COST.formatted(faction, amountInStockpile, amountToRemove));}
    public static FactionServiceException factionLeaderMustBeOfSameFaction() {return new FactionServiceException(FACTION_LEADER_MUST_BE_OF_SAME_FACTION);}
    public static FactionServiceException negativeStockpileAddNotSupported() {return new FactionServiceException((NEGATIVE_STOCKPILE_ADD_NOT_SUPPORTED));}
    public static FactionServiceException negativeStockpileSubtractNotSupported() {return new FactionServiceException((NEGATIVE_STOCKPILE_SUBTRACT_NOT_SUPPORTED));}
    public static FactionServiceException roleIdAlreadyUsed(long id, String factionName) {return new FactionServiceException(ROLE_ID_ALREADY_USED.formatted(id, factionName)) ;}

    public static FactionServiceException noFactionWithNameFoundAndAll(String factionName, String allFactions) { return new FactionServiceException(NO_FACTION_WITH_NAME_FOUND_AND_ALL.formatted(factionName, allFactions)); }
    public static FactionServiceException noFactionWithNameFoundAndAll(String factionName) { return new FactionServiceException(NO_FACTION_WITH_NAME_FOUND.formatted(factionName)); }

    protected FactionServiceException(String message, Throwable rootCause) {
        super(message, rootCause);
    }

    protected FactionServiceException(String message) {
        super(message);
    }
}
