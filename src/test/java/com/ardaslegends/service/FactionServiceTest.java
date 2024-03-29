package com.ardaslegends.service;

import com.ardaslegends.domain.Faction;
import com.ardaslegends.domain.Player;
import com.ardaslegends.domain.RPChar;
import com.ardaslegends.repository.faction.FactionRepository;
import com.ardaslegends.repository.player.PlayerRepository;
import com.ardaslegends.service.dto.UpdateFactionLeaderDto;
import com.ardaslegends.service.dto.faction.UpdateStockpileDto;
import com.ardaslegends.service.exceptions.logic.faction.FactionServiceException;
import com.ardaslegends.service.exceptions.logic.player.PlayerServiceException;
import com.ardaslegends.service.exceptions.ServiceException;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import jakarta.persistence.PersistenceException;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.mockito.Mockito.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@Slf4j
public class FactionServiceTest {

    private FactionRepository mockFactionRepository;

    private PlayerRepository mockPlayerRepository;
    private FactionService factionService;

    private Player player;
    private Faction faction;
    private UpdateFactionLeaderDto dto;
    @BeforeEach
    void setup() {
        mockFactionRepository = mock(FactionRepository.class);
        mockPlayerRepository = mock(PlayerRepository.class);
        factionService = Mockito.spy(new FactionService(mockFactionRepository, mockPlayerRepository));
        Mockito.doNothing().when(factionService).recordMessageInErrorChannel(any());

        faction = Faction.builder().name("Gondor").foodStockpile(10).build();
        player = Player.builder().discordID("1234").ign("mirak551").faction(faction).rpChars(new HashSet<>(Set.of(RPChar.builder().active(true).build()))).build();
        dto = new UpdateFactionLeaderDto(faction.getName(), player.getDiscordID());

        when(mockPlayerRepository.findByDiscordID(player.getDiscordID())).thenReturn(Optional.of(player));
        when(mockFactionRepository.findFactionByName(faction.getName())).thenReturn(Optional.of(faction));
        when(mockFactionRepository.save(faction)).thenReturn(faction);
    }

    @Test
    void ensureSetFactionLeaderWorksProperlyWithCorrectValues() {
        log.debug("Testing if setFactionLeader works properly with correct values ");

        log.debug("Calling factionService.setFactionLeader, expecting no errors");
        var result = factionService.setFactionLeader(dto);

        assertThat(result.getLeader()).isEqualTo(player);
        log.info("Test passed: setFactionLeader works correctly!");
    }
    @Test
    void ensureSetFactionLeaderThrowsSeWhenNoPlayerFound() {
        log.debug("Testing if setFactionLeader properly throws Se when no player found");

        when(mockPlayerRepository.findByDiscordID(player.getDiscordID())).thenReturn(Optional.empty());

        log.debug("Calling factionService.setFactionLeader, expecting Se");
        var result = assertThrows(PlayerServiceException.class, () -> factionService.setFactionLeader(dto));

        assertThat(result.getMessage()).isEqualTo(PlayerServiceException.noPlayerFound(player.getDiscordID()).getMessage());
        log.info("Test passed: setFactionLeader correctly throws Se when no player found");
    }

    @Test
    void ensureSetFactionLeaderThrowsSeWhenPlayerIsNotInTheSameFactionAsTheTargetFaction() {
        log.debug("Testing if setFactionLeader properly throws Se when player is not in the same faction as the target faction");

        player.setFaction(Faction.builder().name("mordor").build());

        log.debug("Calling factionService.setFactionLeader, expecting Se");
        var result = assertThrows(FactionServiceException.class, () -> factionService.setFactionLeader(dto));

        assertThat(result.getMessage()).isEqualTo(FactionServiceException.factionLeaderMustBeOfSameFaction().getMessage());
        log.info("Test passed: setFactionLeader correctly throws Se when player is not in the same faction as target faction");
    }
    @Test
    void ensureSetFactionLeaderThrowsSeWhenPlayerDoesNotHaveAnRpChar() {
        log.debug("Testing if setFactionLeader properly throws Se when player does not have an rpchar");

        player.setRpChars(new HashSet<>(1));

        log.debug("Calling factionService.setFactionLeader, expecting Se");
        var result = assertThrows(PlayerServiceException.class, () -> factionService.setFactionLeader(dto));

        assertThat(result.getMessage()).isEqualTo(PlayerServiceException.playerHasNoRpchar().getMessage());
        log.info("Test passed: setFactionLeader correctly throws Se when player does not have an rpchar");
    }

    @Test
    void ensureRemoveFactionLeaderWorksProperly() {
        log.debug("Testing if removeFactionLeader works properly");

        faction.setLeader(player);
        var result = factionService.removeFactionLeader(faction.getName());

        assertThat(result).isNotNull();
        assertThat(result).isEqualTo(player);
        log.info("Test passed: removeFactionLeader works properly");
    }

    @Test
    void ensureRemoveFactionLeaderThrowsSeWhenFactionHasNoFactionLeader() {
        log.debug("Testing if removeFactionLeader correctly throws Se when no faction leader present");

        log.debug("Calling factionService.removeFactionLeader, expecting Se");
        var result = assertThrows(FactionServiceException.class, () -> factionService.removeFactionLeader("Gondor"));

        assertThat(result.getMessage()).isEqualTo(FactionServiceException.noFactionLeader("Gondor", "Therefore the command cannot be executed.").getMessage());
        log.info("Test passed: removeFactionLeader correctly throws Se when faction has no leader");
    }

    @Test
    void ensureAddToStockpileWorksProperly() {
        log.debug("Testing if addToStockpile works properly with correct values");

        UpdateStockpileDto dto = new UpdateStockpileDto("Gondor", 10);

        var result = factionService.addToStockpile(dto);

        assertThat(result.getName()).isEqualTo(faction.getName());
        assertThat(result.getFoodStockpile()).isEqualTo(10 + dto.amount());

        log.info("Test passed: addToStockpile service works properly with correct values");
    }

    @Test
    void ensureRemoveFromStockpileWorksProperly() {
        log.debug("Testing if removeFromStockpile works properly with correct values");

        UpdateStockpileDto dto = new UpdateStockpileDto("Gondor", 10);

        var result = factionService.removeFromStockpile(dto);

        assertThat(result.getName()).isEqualTo(faction.getName());
        assertThat(result.getFoodStockpile()).isEqualTo(10 - dto.amount());

        log.info("Test passed: removeFromStockpile service works properly with correct values");
    }
    @Test
    void ensureGetByFactionNameWorksProperly() {
        // Assign
        String name = "Mordor";

        when(mockFactionRepository.findFactionByName(name)).thenReturn(Optional.of(Faction.builder().name(name).build()));

        // Act
        var result = factionService.getFactionByName(name);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo(name);
    }

    @Test
    void ensureGetByFactionNameThrowsIllegalArgumentExceptionWhenNameIsBlank() {
        // Assign
        String name = " ";

        // Assert
        var result = assertThrows(IllegalArgumentException.class, () -> factionService.getFactionByName(name));

        assertThat(result.getMessage()).isEqualTo("Faction name must not be blank!");

    }

    @Test
    void ensureGetByFactionNameThrowsServiceExceptionWhenDatabaseDown() {
            // Assign
            String name = "Mordor";

            PersistenceException pEx = new PersistenceException("Database down");

            when(mockFactionRepository.findFactionByName(name)).thenThrow(pEx);

            // Assert
            var result = assertThrows(ServiceException.class, () -> factionService.getFactionByName(name));

            assertThat(result.getCause()).isEqualTo(pEx);
    }

    @Test
    void ensureGetByFactionNameThrowsSeWhenFetchedFactionIsEmpty() {
        log.debug("Testing if getByFaction in FactionService throws Se when Fetched Faction is Empty");

        String name = "Mordor";

        when(mockFactionRepository.findFactionByName(name)).thenReturn(Optional.empty());
        when(mockFactionRepository.findAll()).thenReturn(List.of());
        // Assert
        var result = assertThrows(FactionServiceException.class, () -> factionService.getFactionByName(name));

        assertThat(result.getMessage()).isEqualTo(FactionServiceException.noFactionWithNameFoundAndAll(name, "").getMessage());
        log.info("Test passed: getFactionByName throws Se when no faction found in database");
    }

}
