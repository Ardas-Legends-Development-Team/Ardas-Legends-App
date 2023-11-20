package com.ardaslegends.repository.war;

import com.ardaslegends.domain.Faction;
import com.ardaslegends.domain.war.War;
import com.ardaslegends.domain.war.WarParticipant;

import java.util.Set;

public interface WarRepositoryCustom {
    War findWarByAggressorsAndDefenders(Set<WarParticipant> aggressors, Set<WarParticipant> defenders);

}