package com.ardaslegends.data.service.exceptions;

public class FactionServiceException extends ServiceException {

    public static final String NO_FACTION_WITH_NAME_FOUND = "No faction with name '%s' found in database!";
    public static final String FACTION_LEADER_MUST_BE_OF_SAME_FACTION = "The faction leader must be of the same faction";

    public static FactionServiceException factionLeaderMustBeOfSameFaction() {return new FactionServiceException(FACTION_LEADER_MUST_BE_OF_SAME_FACTION);}

    public static FactionServiceException noFactionWithNameFound(String factionName) { return new FactionServiceException(NO_FACTION_WITH_NAME_FOUND.formatted(factionName)); }

    protected FactionServiceException(String message, Throwable rootCause) {
        super(message, rootCause);
    }

    protected FactionServiceException(String message) {
        super(message);
    }
}
