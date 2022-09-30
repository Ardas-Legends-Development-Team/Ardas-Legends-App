package com.ardaslegends.data.presentation.discord.commands.info;

import com.ardaslegends.data.domain.Army;
import com.ardaslegends.data.domain.ArmyType;
import com.ardaslegends.data.domain.Faction;
import com.ardaslegends.data.presentation.discord.commands.ALCommandExecutor;
import com.ardaslegends.data.presentation.discord.config.BotProperties;
import com.ardaslegends.data.presentation.discord.utils.ALColor;
import com.ardaslegends.data.presentation.discord.utils.Thumbnails;
import com.ardaslegends.data.service.FactionService;
import com.ardaslegends.data.service.dto.army.StationDto;
import com.ardaslegends.data.service.utils.ServiceUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.entity.user.User;
import org.javacord.api.interaction.SlashCommandInteraction;
import org.javacord.api.interaction.SlashCommandInteractionOption;

import java.util.List;

@RequiredArgsConstructor

@Slf4j
public class InfoFactionStockpile implements ALCommandExecutor {

    private final FactionService factionService;

    @Override
    public EmbedBuilder execute(SlashCommandInteraction interaction, List<SlashCommandInteractionOption> options, BotProperties properties) {
        log.debug("Executing /info faction stockpile request");

        log.debug("Getting options");
        String factionName = getStringOption("faction-name", options);
        log.debug("faction-name: [{}]", factionName);

        log.trace("Calling armyService");
        Faction faction = discordServiceExecution(factionName, factionService::getFactionByName, "Error while getting Faction Stockpile");

        log.debug("Building response Embed");
        return new EmbedBuilder()
                .setTitle("Faction Stockpile")
                .setDescription("Current stockpile of Faction %s".formatted(faction.getName()))
                .setColor(ALColor.GREEN)
                .addInlineField("Faction", faction.getName())
                .addInlineField("Stockpile", "%d stacks".formatted(faction.getFoodStockpile()))
                .addField("Days of movement payable", "%d".formatted((int)Math.floor(faction.getFoodStockpile()/9.0)), false)
                .setThumbnail(getFactionBanner(faction.getName()))
                .setTimestampToNow();
    }
}
