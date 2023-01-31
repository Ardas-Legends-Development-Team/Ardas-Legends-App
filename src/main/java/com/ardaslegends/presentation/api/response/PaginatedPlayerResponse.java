package com.ardaslegends.presentation.api.response;

import com.ardaslegends.domain.Player;

public record PaginatedPlayerResponse(
        long id,
        String ign,
        String nameOfFaction,
        String nameOfCharacter,
        String titleOfCharacter
) {
    public PaginatedPlayerResponse(Player player) {
        this(
                player.getId(),
                player.getIgn(),
                player.getFaction().getName(),
                player.getRpChar() != null ?
                        player.getRpChar().getName()
                        : null,
                player.getRpChar() != null ?
                        player.getRpChar().getTitle()
                        : null
        );
    }
}