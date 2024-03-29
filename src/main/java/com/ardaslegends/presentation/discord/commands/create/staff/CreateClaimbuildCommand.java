package com.ardaslegends.presentation.discord.commands.create.staff;

import com.ardaslegends.presentation.discord.commands.ALMessageResponse;
import com.ardaslegends.presentation.discord.commands.ALStaffCommandExecutor;
import com.ardaslegends.presentation.discord.config.BotProperties;
import com.ardaslegends.presentation.discord.utils.ALColor;
import com.ardaslegends.service.ClaimBuildService;
import com.ardaslegends.service.dto.claimbuild.CreateClaimBuildDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.interaction.SlashCommandInteraction;
import org.javacord.api.interaction.SlashCommandInteractionOption;

import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor

@Slf4j
public class CreateClaimbuildCommand implements ALStaffCommandExecutor {

    private final ClaimBuildService claimBuildService;
    @Override
    public ALMessageResponse execute(SlashCommandInteraction interaction, List<SlashCommandInteractionOption> options, BotProperties properties) {
        log.debug("Incoming /create claimbuild request, getting option-data");

        checkStaff(interaction, properties.getStaffRoleIds());

        log.debug("Fetching option-data");
        String name = getStringOption("cbname", options);
        log.trace("CreateClaimbuild: Name is [{}]", name);

        String region = getStringOption("region", options);
        log.trace("CreateClaimbuild: Region is [{}]", region);

        String type = getStringOption("type", options);
        log.trace("CreateClaimbuild: ClaimbuildType is [{}]", type);

        String faction = getStringOption("faction", options);
        log.trace("CreateClaimbuild: Faction is [{}]", faction);

        int x = getLongOption("x", options).intValue();
        log.trace("CreateClaimbuild: X-Coordinate is [{}]", x);

        int y = getLongOption("y", options).intValue();
        log.trace("CreateClaimbuild: Y-CoordinateName is [{}]", y);

        int z = getLongOption("z", options).intValue();
        log.trace("CreateClaimbuild: Z-Coordinate is [{}]", z);

        String traders = getStringOption("traders", options);
        log.trace("CreateClaimbuild: Traders are [{}]", traders);

        String sieges = getStringOption("sieges", options);
        log.trace("CreateClaimbuild: Sieges are [{}]", sieges);

        String numberOfHouses = getStringOption("number-of-houses", options);
        log.trace("CreateClaimbuild: number of houses is [{}]", numberOfHouses);

        String builtBy = getStringOption("built-by", options);
        log.trace("CreateClaimbuild: built by is [{}]", builtBy);

        String productionSites = getOptionalStringOption("production-sites", options).orElse("no");
        log.trace("CreateClaimbuild: Production Sites are  [{}]", productionSites);

        String specialBuildings = getOptionalStringOption("special-buildings", options).orElse("no");
        log.trace("CreateClaimbuild: Special Buildings are  [{}]", specialBuildings);


        log.debug("CreateClaimbuild: Building Dto");
        CreateClaimBuildDto dto = new CreateClaimBuildDto(name,region,type,faction,x,y,z,productionSites,specialBuildings,traders,sieges,numberOfHouses,builtBy);
        log.debug("Dto result [{}]", dto);

        log.debug("CreateClaimbuild: Calling createClaimbuild Service");
        var claimbuild = discordServiceExecution(dto,true, claimBuildService::createClaimbuild, "Error during Claimbuild Creation");
        log.debug("CreateClaimbuild: Result [{}]", claimbuild);
        log.debug(claimbuild.getProductionSites().toString());


        String prodString = createProductionSiteString(claimbuild.getProductionSites());
        String specialBuildingsString = createSpecialBuildingsString(claimbuild.getSpecialBuildings());
        String builtByString = claimbuild.getBuiltBy().stream().map(player -> player.getIgn()).collect(Collectors.joining(", "));

        return new ALMessageResponse(null, new EmbedBuilder()
                .setTitle("Claimbuild %s was successfully created!".formatted(claimbuild.getName()))
                .setColor(ALColor.YELLOW)
                .addInlineField("Name", claimbuild.getName())
                .addInlineField("Faction", claimbuild.getOwnedBy().getName())
                .addInlineField("Region", claimbuild.getRegion().getId())
                .addInlineField("Type", claimbuild.getType().getName())
                .addField("Production Sites", prodString)
                .addField("Special Buildings", specialBuildingsString)
                .addInlineField("Traders", claimbuild.getTraders())
                .addInlineField("Siege", claimbuild.getSiege())
                .addInlineField("Houses", claimbuild.getNumberOfHouses())
                .addInlineField("Coordinates", claimbuild.getCoordinates().toString())
                .addInlineField("Built by", builtByString)
                .setThumbnail(getFactionBanner(claimbuild.getOwnedBy().getName()))
                .setTimestampToNow());
    }
}
