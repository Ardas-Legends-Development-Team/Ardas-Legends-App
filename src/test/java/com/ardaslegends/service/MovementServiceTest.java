package com.ardaslegends.service;

import com.ardaslegends.domain.*;
import com.ardaslegends.repository.war.army.ArmyRepository;
import com.ardaslegends.repository.MovementRepository;
import com.ardaslegends.repository.player.PlayerRepository;
import com.ardaslegends.repository.region.RegionRepository;
import com.ardaslegends.service.dto.army.MoveArmyDto;
import com.ardaslegends.service.dto.player.DiscordIdDto;
import com.ardaslegends.service.dto.player.rpchar.MoveRpCharDto;
import com.ardaslegends.service.exceptions.logic.player.PlayerServiceException;
import com.ardaslegends.service.exceptions.ServiceException;
import com.ardaslegends.service.exceptions.logic.army.ArmyServiceException;
import com.ardaslegends.service.exceptions.logic.movement.MovementServiceException;
import com.ardaslegends.service.utils.ServiceUtils;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@Slf4j
public class MovementServiceTest {

    private MovementRepository mockMovementRepository;
    private RegionRepository mockRegionRepository;
    private ArmyRepository mockArmyRepository;
    private ArmyService mockArmyService;
    private PlayerRepository mockPlayerRepository;
    private PlayerService mockPlayerService;
    private Pathfinder mockPathfinder;
    private RpCharService mockRpCharService;

    private MovementService movementService;

    private Faction faction;
    private Region region1;
    private Region region2;
    private RPChar rpchar;
    private Player player;
    private UnitType unitType;
    private Unit unit;
    private Army army;
    private PathElement pathElement1;
    private PathElement pathElement2;
    private List<PathElement> path;
    private Movement movement;
    private ClaimBuild claimBuild;

    @BeforeEach
    void setup() {
        mockMovementRepository = mock(MovementRepository.class);
        mockRegionRepository = mock(RegionRepository.class);
        mockPlayerRepository = mock(PlayerRepository.class);
        mockPlayerService = mock(PlayerService.class);
        mockArmyRepository = mock(ArmyRepository.class);
        mockArmyService = mock(ArmyService.class);
        mockPathfinder = mock(Pathfinder.class);
        mockRpCharService = mock(RpCharService.class);
        movementService = new MovementService(mockMovementRepository, mockRegionRepository, mockArmyRepository, mockArmyService, mockPlayerRepository, mockPlayerService, mockPathfinder, mockRpCharService);

        region1 = Region.builder().id("90").regionType(RegionType.LAND).build();
        region2 = Region.builder().id("91").regionType(RegionType.LAND).build();
        unitType = UnitType.builder().unitName("Gondor Archer").tokenCost(1.5).build();
        unit = Unit.builder().unitType(unitType).army(army).amountAlive(5).count(10).build();
        faction = Faction.builder().name("Gondor").allies(new ArrayList<>()).foodStockpile(10).build();
        claimBuild = ClaimBuild.builder().name("Nimheria").siege("Ram, Trebuchet, Tower").region(region1).ownedBy(faction).specialBuildings(List.of(SpecialBuilding.HOUSE_OF_HEALING)).stationedArmies(List.of()).build();
        rpchar = RPChar.builder().name("Belegorn").isHealing(false).currentRegion(region1).build();
        player = Player.builder().discordID("1234").faction(faction).build();
        player.addActiveRpChar(rpchar);
        army = Army.builder().name("Knights of Gondor").armyType(ArmyType.ARMY).faction(faction).freeTokens(30 - unit.getCount() * unitType.getTokenCost()).currentRegion(region1).boundTo(player.getActiveCharacter().get()).stationedAt(claimBuild).sieges(new ArrayList<>()).createdAt(OffsetDateTime.now().minusDays(3)).build();
        pathElement1 = PathElement.builder().region(region1).baseCost(region1.getCost()).actualCost(region1.getCost()).build();
        pathElement2 = PathElement.builder().region(region2).baseCost(region2.getCost()).actualCost(region2.getCost()).build();
        path = List.of(pathElement1, pathElement2);
        movement =  Movement.builder().isCharMovement(false).isCurrentlyActive(true).army(army).path(path).build();

        when(mockPlayerService.getPlayerByDiscordId(player.getDiscordID())).thenReturn(player);
        when(mockArmyRepository.findArmyByName(army.getName())).thenReturn(Optional.of(army));
        when(mockArmyService.getArmyByName(army.getName())).thenReturn(army);
        when(mockArmyRepository.save(army)).thenReturn(army);
        when(mockMovementRepository.findMovementByArmyAndIsCurrentlyActiveTrue(army)).thenReturn(Optional.of(movement));
        when(mockRegionRepository.findById(region1.getId())).thenReturn(Optional.of(region1));
        when(mockRegionRepository.findById(region2.getId())).thenReturn(Optional.of(region2));
        when(mockPathfinder.findShortestWay(any(),any(),any(),anyBoolean())).thenReturn(movement.getPath());
    }

    @Test
    void ensureMoveRpCharWorks() {
        log.debug("Testing if createRpCharMovement works with valid values!");

        // Assign
        log.trace("Initializing player, rpchar and regions");
        Region fromRegion = Region.builder().id("90").build();
        Region toRegion = Region.builder().id("92").build();
        RPChar rpChar = RPChar.builder().name("Belegorn Arnorion").isHealing(false).currentRegion(fromRegion).build();
        Player player = Player.builder().discordID("1234").ign("Lüktrönic").uuid("huehue").build();
        player.addActiveRpChar(rpChar);
        log.trace("Initializing Dto");
        MoveRpCharDto dto = new MoveRpCharDto("1234", toRegion.getId());

        log.trace("Mocking methods");
        when(mockPlayerRepository.findByDiscordID("1234")).thenReturn(Optional.of(player));
        when(mockRegionRepository.findById(toRegion.getId())).thenReturn(Optional.of(toRegion));
        when(mockPathfinder.findShortestWay(fromRegion, toRegion, player, true)).thenReturn(path);
        when(mockMovementRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        //Act
        Movement createdMovement = movementService.createRpCharMovement(dto);

        //Assert
        log.debug("Starting asserts");
        assertThat(createdMovement.getRpChar()).isEqualTo(player.getActiveCharacter().get());
        assertThat(createdMovement.getIsCharMovement()).isTrue();
        assertThat(createdMovement.getStartTime().toLocalDate()).isEqualTo(LocalDate.now());
        assertThat(createdMovement.getEndTime().toLocalDate()).isEqualTo(LocalDate.now().plusDays(ServiceUtils.getTotalPathCost(path)/24));
        assertThat(createdMovement.getArmy()).isNull();

        log.info("Test passed: createRpCharMovement works with valid values!");
    }

    @Test
    void ensureMoveRpCharThrowsIAEWhenNoPlayerFound() {
        log.debug("Testing if createRpCharMovement throws IllegalArgumentException when no player is found!");

        // Assign
        log.trace("Initializing player, rpchar and regions");
        Region toRegion = Region.builder().id("92").build();

        log.trace("Initializing Dto");
        MoveRpCharDto dto = new MoveRpCharDto("1234", toRegion.getId());

        log.trace("Mocking methods");
        when(mockPlayerRepository.findByDiscordID("1234")).thenReturn(Optional.empty());

        //Act
        var exception = assertThrows(IllegalArgumentException.class, () -> movementService.createRpCharMovement(dto));

        //Assert
        assertThat(exception.getMessage()).contains("No player found");

        log.info("Test passed: createRpCharMovement throws IllegalArgumentException when no player is found!");
    }
    @Test
    void ensureMoveRpCharThrowsSEWhenNoRpChar() {
        log.debug("Testing if createRpCharMovement throws IllegalArgumentException when player has no Rp Char!");

        // Assign
        log.trace("Initializing player, rpchar and regions");
        Region fromRegion = Region.builder().id("90").build();
        Region toRegion = Region.builder().id("92").build();
        Player player = Player.builder().discordID("1234").ign("Lüktrönic").uuid("huehue").rpChars(new HashSet<>(1)).build();

        log.trace("Initializing Dto");
        MoveRpCharDto dto = new MoveRpCharDto("1234", toRegion.getId());

        log.trace("Mocking methods");
        when(mockPlayerRepository.findByDiscordID("1234")).thenReturn(Optional.of(player));

        //Act
        var exception = assertThrows(PlayerServiceException.class, () -> movementService.createRpCharMovement(dto));

        //Assert
        assertThat(exception.getMessage()).isEqualTo(PlayerServiceException.noRpChar().getMessage());

        log.info("Test passed: createRpCharMovement throws IllegalArgumentException when player has no Rp Char!");
    }


    @Test
    void ensureMoveRpCharThrowsIAEWhenEndRegionDoesNotExists() {
        log.debug("Testing if createRpCharMovement throws IllegalArgumentException when end Region does not exist!");

        // Assign
        log.trace("Initializing player, rpchar and regions");
        Region fromRegion = Region.builder().id("90").build();
        Region toRegion = Region.builder().id("92").build();
        RPChar rpChar = RPChar.builder().name("Belegorn Arnorion").isHealing(false).currentRegion(fromRegion).build();
        Player player = Player.builder().discordID("1234").ign("Lüktrönic").uuid("huehue").build();
        player.addActiveRpChar(rpChar);

        log.trace("Initializing Dto");
        MoveRpCharDto dto = new MoveRpCharDto("1234", toRegion.getId());

        log.trace("Mocking methods");
        when(mockPlayerRepository.findByDiscordID("1234")).thenReturn(Optional.of(player));
        when(mockRegionRepository.findById(toRegion.getId())).thenReturn(Optional.empty());

        //Act
        var exception = assertThrows(IllegalArgumentException.class, () -> movementService.createRpCharMovement(dto));

        //Assert
        assertThat(exception.getMessage()).isEqualTo("The region %s does not exist!".formatted(dto.toRegion()));

        log.info("Test passed: createRpCharMovement throws IllegalArgumentException when player has no Rp Char!");
    }

    @Test
    void ensureMoveRpCharThrowsSEWhenBoundToArmy() {
        log.debug("Testing if createRpCharMovement throws ServiceException when Char is bound to Army!");

        // Assign
        log.trace("Initializing player, rpchar and regions");
        Region fromRegion = Region.builder().id("90").build();
        Region toRegion = Region.builder().id("92").build();
        RPChar rpChar = RPChar.builder().name("Belegorn Arnorion").currentRegion(fromRegion).build();
        Player player = Player.builder().discordID("1234").ign("Lüktrönic").uuid("huehue").build();
        player.addActiveRpChar(rpChar);
        Army army = Army.builder().name("Army of Gondor").boundTo(player.getActiveCharacter().get()).currentRegion(fromRegion).build();
        rpChar.setBoundTo(army);

        log.trace("Initializing Dto");
        MoveRpCharDto dto = new MoveRpCharDto("1234", toRegion.getId());

        log.trace("Mocking methods");
        when(mockPlayerRepository.findByDiscordID("1234")).thenReturn(Optional.of(player));
        when(mockRegionRepository.findById(toRegion.getId())).thenReturn(Optional.empty());

        //Act
        var exception = assertThrows(ServiceException.class, () -> movementService.createRpCharMovement(dto));

        //Assert
        log.debug("Asserting that createRpCharMovement throws ServiceException");
        assertThat(exception.getMessage()).isEqualTo(ServiceException.cannotMoveRpCharBoundToArmy(rpChar, army).getMessage());

        log.info("Test passed: createRpCharMovement throws ServiceException when Char is bound to Army!");
    }

    @Test
    void ensureMoveRpCharThrowsSEWhenAlreadyInRegion() {
        log.debug("Testing if createRpCharMovement throws ServiceException when Char is already in destination region!");

        // Assign
        log.trace("Initializing player, rpchar and regions");
        Region fromRegion = Region.builder().id("90").build();
        RPChar rpChar = RPChar.builder().name("Belegorn Arnorion").currentRegion(fromRegion).build();
        Player player = Player.builder().discordID("1234").ign("Lüktrönic").uuid("huehue").build();
        player.addActiveRpChar(rpChar);

        log.trace("Initializing Dto");
        MoveRpCharDto dto = new MoveRpCharDto("1234", fromRegion.getId());

        log.trace("Mocking methods");
        when(mockPlayerRepository.findByDiscordID("1234")).thenReturn(Optional.of(player));
        when(mockRegionRepository.findById(fromRegion.getId())).thenReturn(Optional.of(fromRegion));

        //Act
        var exception = assertThrows(ServiceException.class, () -> movementService.createRpCharMovement(dto));

        //Assert
        log.debug("Asserting that createRpCharMovement throws ServiceException");
        assertThat(exception.getMessage()).isEqualTo(ServiceException.cannotMoveRpCharAlreadyInRegion(rpChar, fromRegion).getMessage());

        log.info("Test passed: createRpCharMovement throws ServiceException when Char is already in destination region!");
    }

    @Test
    void ensureMoveRpCharThrowsSEWhenCharIsHealing() {
        log.debug("Testing if createRpCharMovement throws ServiceException when Char is healing!");

        // Assign
        log.trace("Initializing Dto");
        MoveRpCharDto dto = new MoveRpCharDto(player.getDiscordID(), region2.getId());
        rpchar.setIsHealing(true);

        log.trace("Mocking methods");
        when(mockPlayerRepository.findByDiscordID(player.getDiscordID())).thenReturn(Optional.of(player));
        when(mockRegionRepository.findById(region2.getId())).thenReturn(Optional.of(region2));

        //Act
        var exception = assertThrows(MovementServiceException.class, () -> movementService.createRpCharMovement(dto));

        //Assert
        log.debug("Asserting that createRpCharMovement throws ServiceException");
        assertThat(exception.getMessage()).isEqualTo(MovementServiceException.cannotMoveCharIsHealing(rpchar.getName()).getMessage());

        log.info("Test passed: createRpCharMovement throws ServiceException when Char is healing!");
    }

    @Test
    void ensureMoveRpCharThrowsSEWhenCharAlreadyMoving() {
        log.debug("Testing if createRpCharMovement throws ServiceException when Char is already in a movement!");

        // Assign
        log.trace("Initializing player, rpchar and regions");
        Region fromRegion = Region.builder().id("90").build();
        Region toRegion = Region.builder().id("92").build();
        RPChar rpChar = RPChar.builder().name("Belegorn Arnorion").isHealing(false).currentRegion(fromRegion).build();
        Player player = Player.builder().discordID("1234").ign("Lüktrönic").uuid("huehue").build();
        player.addActiveRpChar(rpChar);
        Movement movement = Movement.builder().isCharMovement(true)
                .startTime(OffsetDateTime.now()).endTime(OffsetDateTime.now()).isCurrentlyActive(true).rpChar(player.getActiveCharacter().get()).build();

        log.trace("Initializing Dto");
        MoveRpCharDto dto = new MoveRpCharDto("1234", toRegion.getId());

        log.trace("Mocking methods");
        when(mockPlayerRepository.findByDiscordID("1234")).thenReturn(Optional.of(player));
        when(mockRegionRepository.findById(toRegion.getId())).thenReturn(Optional.of(toRegion));
        when(mockMovementRepository.findMovementsByRpChar(player.getActiveCharacter().get())).thenReturn(List.of(movement));

        //Act
        var exception = assertThrows(ServiceException.class, () -> movementService.createRpCharMovement(dto));

        //Assert
        log.debug("Asserting that createRpCharMovement throws ServiceException");
        assertThat(exception.getMessage()).isEqualTo(ServiceException.cannotMoveRpCharAlreadyMoving(rpChar).getMessage());

        log.info("Test passed: createRpCharMovement throws ServiceException when Char is already in a movement!");
    }

    @Test
    void ensureCancelRpCharMovementWorks() {
        log.debug("Testing if cancelRpCharMovement works with valid values!");

        // Assign
        log.trace("Initializing player, rpchar and regions");
        Region fromRegion = Region.builder().id("91").build();
        RPChar rpChar = RPChar.builder().name("Belegorn Arnorion").currentRegion(fromRegion).build();
        Player player = Player.builder().discordID("1234").ign("Lüktrönic").uuid("huehue").build();
        player.addActiveRpChar(rpChar);
        Movement movement = Movement.builder().isCharMovement(true).rpChar(player.getActiveCharacter().get()).path(path).isCurrentlyActive(true).build();

        log.trace("Initializing Dto");
        DiscordIdDto dto = new DiscordIdDto("1234");

        log.trace("Mocking methods");
        when(mockPlayerRepository.findByDiscordID("1234")).thenReturn(Optional.of(player));
        when(mockMovementRepository.findMovementByRpCharAndIsCurrentlyActiveTrue(player.getActiveCharacter().get())).thenReturn(Optional.of(movement));
        when(mockMovementRepository.save(movement)).thenReturn(movement);

        //Act
        Movement newMovement = movementService.cancelRpCharMovement(dto);

        //Assert
        log.debug("Starting asserts");
        assertThat(newMovement).isEqualTo(movement);
        assertThat(movement.getIsCurrentlyActive()).isFalse();

        log.info("Test passed: cancelRpCharMovement works with valid values!");
    }

    @Test
    void ensureCancelRpCharMovementThrowsIAEWhenNoPlayer() {
        log.debug("Testing if cancelRpCharMovement throws ServiceException when no player is found!");

        // Assign
        log.trace("Initializing player, rpchar and regions");
        DiscordIdDto dto = new DiscordIdDto("1234");

        log.trace("Mocking methods");
        when(mockPlayerRepository.findByDiscordID("1234")).thenReturn(Optional.empty());

        //Act
        var exception = assertThrows(IllegalArgumentException.class, () -> movementService.cancelRpCharMovement(dto));

        //Assert
        log.debug("Starting asserts");
        assertThat(exception.getMessage()).contains("No player found");

        log.info("Test passed: cancelRpCharMovement throws ServiceException when no player is found!");
    }

    @Test
    void ensureCancelRpCharMovementThrowsSEWhenNoRpChar() {
        log.debug("Testing if cancelRpCharMovement throws ServiceException when player has no rp char!");

        // Assign
        log.trace("Initializing player, rpchar and regions");
        Player player = Player.builder().discordID("1234").ign("Lüktrönic").uuid("huehue").rpChars(new HashSet<>(1)).build();
        DiscordIdDto dto = new DiscordIdDto("1234");

        log.trace("Mocking methods");
        when(mockPlayerRepository.findByDiscordID("1234")).thenReturn(Optional.of(player));

        //Act
        var exception = assertThrows(PlayerServiceException.class, () -> movementService.cancelRpCharMovement(dto));

        //Assert
        log.debug("Starting asserts");
        assertThat(exception.getMessage()).isEqualTo(PlayerServiceException.noRpChar().getMessage());

        log.info("Test passed: cancelRpCharMovement throws ServiceException when player has no rp char!");
    }

    @Test
    void ensureCancelRpCharMovementThrowsSEWhenNoMovement() {
        log.debug("Testing if cancelRpCharMovement throws ServiceException when no active movement is found!");

        // Assign
        log.trace("Initializing player, rpchar and regions");
        Region fromRegion = Region.builder().id("91").build();
        RPChar rpChar = RPChar.builder().name("Belegorn Arnorion").currentRegion(fromRegion).build();
        Player player = Player.builder().discordID("1234").ign("Lüktrönic").uuid("huehue").build();
        player.addActiveRpChar(rpChar);

        log.trace("Initializing Dto");
        DiscordIdDto dto = new DiscordIdDto("1234");

        log.trace("Mocking methods");
        when(mockPlayerRepository.findByDiscordID("1234")).thenReturn(Optional.of(player));
        when(mockMovementRepository.findMovementsByRpChar(player.getActiveCharacter().get())).thenReturn(List.of());

        //Act
        var exception = assertThrows(ServiceException.class, () -> movementService.cancelRpCharMovement(dto));

        //Assert
        log.debug("Starting asserts");
        assertThat(exception.getMessage()).isEqualTo(MovementServiceException.noActiveMovementChar(rpChar.getName()).getMessage());

        log.info("Test passed: cancelRpCharMovement throws ServiceException when no active movement is found!");
    }

    @Test
    void ensureCreateArmyMovementWorksProperly() {
        log.debug("Testing if createArmyMovement works properly given the correct values");

        MoveArmyDto dto = new MoveArmyDto(player.getDiscordID(), army.getName(), region2.getId());
        army.setBoundTo(player.getActiveCharacter().get());
        when(mockMovementRepository.findMovementByArmyAndIsCurrentlyActiveTrue(army)).thenReturn(Optional.empty());

        log.debug("Calling createArmyMovement, expecting no errors");
        var result = movementService.createArmyMovement(dto);

        assertThat(result).isNotNull();
        assertThat(result.getArmy()).isEqualTo(army);
        assertThat(result.getRpChar()).isEqualTo(player.getActiveCharacter().get());
        assertThat(result.getIsCurrentlyActive()).isTrue();
        assertThat(result.getIsCharMovement()).isFalse();
        assertThat(result.getPath()).isEqualTo(movement.getPath());

        log.info("Test passed: createArmyMovement works properly with correct values");
    }
    @Test
    void ensureCreateArmyMovementThrowsSeWhenPlayerIsNotRegistered() {
        log.debug("Testing if createArmyMovement throws Se when region does not exist");

        MoveArmyDto dto = new MoveArmyDto(player.getDiscordID(), army.getName(), "S2");

        when(mockRegionRepository.findById(dto.toRegion())).thenReturn(Optional.empty());

        log.debug("Calling createArmyMovement, expecting Se");
        var result = assertThrows(ServiceException.class, () -> movementService.createArmyMovement(dto));

        assertThat(result.getMessage()).contains("Desired destination region 'S2'");
        log.info("Test passed: createArmyMovement throws Se when region does not exist");
    }

    @Test
    void ensureCreateArmyMovementThrowsSeWhenArmyAndDestinationRegionsAreTheSame() {
        log.debug("Testing if createArmyMovement throws Se when destination region and current army region are the same");

        MoveArmyDto dto = new MoveArmyDto(player.getDiscordID(), army.getName(), region1.getId());

        log.debug("Calling createArmyMovement, expecting Se");
        var result = assertThrows(ArmyServiceException.class, () -> movementService.createArmyMovement(dto));

        assertThat(result.getMessage()).contains("is already in the desired region");
        log.info("Test passed: createArmyMovement throws Se when current army region and desired region are the same");
    }

    @Test
    void ensureCreateArmyMovementThrowsSEWhenArmyCreatedLessThan24hAgo() {
        log.debug("Testing if createArmyMovement throws ASE when army was created less than 24h ago!");

        army.setCreatedAt(OffsetDateTime.now().minusMinutes(59).minusHours(23));
        log.debug("Now: [{}]", OffsetDateTime.now());
        log.debug("Created: [{}]", army.getCreatedAt());


        MoveArmyDto dto = new MoveArmyDto(player.getDiscordID(), army.getName(), region2.getId());
        when(mockMovementRepository.findMovementByArmyAndIsCurrentlyActiveTrue(army)).thenReturn(Optional.empty());

        log.debug("Calling createArmyMovement, expecting Se");
        var result = assertThrows(ArmyServiceException.class, () -> movementService.createArmyMovement(dto));

        assertThat(result.getMessage()).isEqualTo(ArmyServiceException.cannotMoveArmyWasCreatedRecently(army.getName(), 1).getMessage());
        log.info("Test passed: createArmyMovement throws ASE when army was created less than 24h ago!");
    }

    @Test
    void ensureCreateArmyMovementThrowsSeWhenArmyIsAlreadyMoving() {
        log.debug("Testing if createArmyMovement throws Se when specified army is already moving");

        MoveArmyDto dto = new MoveArmyDto(player.getDiscordID(), army.getName(), region2.getId());
        when(mockMovementRepository.findMovementByArmyAndIsCurrentlyActiveTrue(army)).thenReturn(Optional.of(new Movement()));

        log.debug("Calling createArmyMovement, expecting Se");
        var result = assertThrows(ArmyServiceException.class, () -> movementService.createArmyMovement(dto));

        assertThat(result.getMessage()).contains("because it is already in a movement!");
        log.info("Test passed: createArmyMovement throws Se when Army is already in a movement");
    }

    @Test
    void ensureCreateArmyMovementThrowsSeWhenPlayerIsNotALlowedToPerformMovement() {
        log.debug("Testing if createArmyMovement throws Se when player does not have permission to move");

        MoveArmyDto dto = new MoveArmyDto(player.getDiscordID(), army.getName(), region2.getId());
        army.setBoundTo(null);
        when(mockMovementRepository.findMovementByArmyAndIsCurrentlyActiveTrue(army)).thenReturn(Optional.empty());

        log.debug("Calling createArmyMovement, expecting Se");
        var result = assertThrows(ArmyServiceException.class, () -> movementService.createArmyMovement(dto));

        assertThat(result.getMessage()).contains("No permission to perform this action");
        log.info("Test passed: createArmyMovement throws Se when player is not allowed to move");
    }
    @Test
    void ensureCancelArmyMovementWorks() {
        log.debug("Testing if cancelArmyMovement works with proper data!");

        //Assign
        log.trace("Initializing data");
        String armyName = "Knights of Gondor";
        Faction faction = Faction.builder().name("Gondor").build();
        Army army = Army.builder().name(armyName).faction(faction).build();
        Movement movement = Movement.builder().isCharMovement(false).army(army).path(path).isCurrentlyActive(true).build();
        RPChar rpchar = RPChar.builder().name("Belegorn").boundTo(army).build();
        Player player = Player.builder().ign("Luktronic").discordID("1234").faction(faction).build();
        player.addActiveRpChar(rpchar);
        player.getActiveCharacter().get().setBoundTo(army);
        army.setBoundTo(player.getActiveCharacter().get());
        MoveArmyDto dto = new MoveArmyDto("1234", armyName, null);

        when(mockArmyService.getArmyByName(armyName)).thenReturn(army);
        when(mockPlayerService.getPlayerByDiscordId("1234")).thenReturn(player);
        when(mockMovementRepository.findMovementByArmyAndIsCurrentlyActiveTrue(army)).thenReturn(Optional.of(movement));

        //Act
        log.trace("Calling cancelArmyMovement");
        movementService.cancelArmyMovement(dto);

        //Assert
        log.trace("Asserting");
        assertThat(movement.getIsCurrentlyActive()).isFalse();

        log.info("Test passed: cancelArmyMovement works with proper data!");
    }

    @Test
    void ensureCancelArmyMovementThrowsSEWhenArmyNotFound() {
        log.debug("Testing if cancelArmyMovement throws Service Exception when Army not found!");

        //Assign
        log.trace("Initializing data");
        String armyName = "Knights of Gondor";
        Army army = Army.builder().name(armyName).build();
        MoveArmyDto dto = new MoveArmyDto("1234", armyName, null);

        when(mockArmyService.getArmyByName(armyName)).thenThrow(ArmyServiceException.noArmyWithName("Army or Company", armyName));

        //Act / Assert
        log.trace("Calling cancelArmyMovement and asserting it throws ArmyServiceException");
        var exception = assertThrows(ArmyServiceException.class, () -> movementService.cancelArmyMovement(dto));

        //Assert
        log.trace("Asserting");
        assertThat(exception.getMessage()).isEqualTo(ArmyServiceException.noArmyWithName("Army or Company", armyName).getMessage());

        log.info("Test passed: cancelArmyMovement throws Service Exception when Army not found!");
    }



    @Test
    void ensureCancelArmyMovementThrowsSEWhenPlayerNotAllowed() {
        log.debug("Testing if cancelArmyMovement throws MovementServiceException when the player is not faction leader!");

        //Assign
        log.trace("Initializing data");
        String armyName = "Knights of Gondor";
        Faction gondor = Faction.builder().name("Gondor").build();
        Army army = Army.builder().name(armyName).faction(gondor).build();
        RPChar rpchar = RPChar.builder().name("Belegorn").build();
        Player player = Player.builder().faction(gondor).ign("Luktronic").discordID("1234").build();
        player.addActiveRpChar(rpchar);
        MoveArmyDto dto = new MoveArmyDto("1234", armyName, null);

        when(mockPlayerService.getPlayerByDiscordId("1234")).thenReturn(player);
        when(mockArmyService.getArmyByName(armyName)).thenReturn(army);

        //Act / Assert
        log.trace("Calling cancelArmyMovement and asserting it throws MovementServiceException");
        var exception = assertThrows(MovementServiceException.class, () -> movementService.cancelArmyMovement(dto));

        //Assert
        log.trace("Asserting");
        assertThat(exception.getMessage()).isEqualTo(MovementServiceException.notAllowedToCancelMove().getMessage());

        log.info("Test passed: cancelArmyMovement throws ArmyServiceException when the player is not allowed to move!");
    }

    @Test
    void ensureCancelArmyMovementThrowsSEWhenNoMovement() {
        log.debug("Testing if cancelArmyMovement throws Service Exception when no Movement is found!");

        //Assign
        log.trace("Initializing data");
        String armyName = "Knights of Gondor";
        Faction gondor = Faction.builder().name("Gondor").build();
        Army army = Army.builder().name(armyName).faction(gondor).build();
        RPChar rpchar = RPChar.builder().name("Belegorn").build();
        Player player = Player.builder().faction(gondor).ign("Luktronic").discordID("1234").build();
        player.addActiveRpChar(rpchar);
        gondor.setLeader(player);
        MoveArmyDto dto = new MoveArmyDto("1234", armyName, null);

        when(mockArmyService.getArmyByName(armyName)).thenReturn(army);
        when(mockPlayerService.getPlayerByDiscordId("1234")).thenReturn(player);
        when(mockMovementRepository.findMovementByArmyAndIsCurrentlyActiveTrue(army)).thenReturn(Optional.empty());

        //Act / Assert
        log.trace("Calling cancelArmyMovement and asserting it throws ArmyServiceException");
        var exception = assertThrows(MovementServiceException.class, () -> movementService.cancelArmyMovement(dto));

        //Assert
        log.trace("Asserting");
        assertThat(exception.getMessage()).isEqualTo(MovementServiceException.noActiveMovementArmy(armyName).getMessage());

        log.info("Test passed: cancelArmyMovement throws Service Exception when no Movement is found!");
    }
}
