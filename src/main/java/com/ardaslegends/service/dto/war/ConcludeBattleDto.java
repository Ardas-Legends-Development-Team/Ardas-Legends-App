package com.ardaslegends.service.dto.war;

import com.ardaslegends.service.dto.player.DiscordIdDto;

public record ConcludeBattleDto(
        String winnerFaction,
        SurvivingUnitsDto[] survivingUnits,
        DiscordIdDto[] playersKilled
) {
}
