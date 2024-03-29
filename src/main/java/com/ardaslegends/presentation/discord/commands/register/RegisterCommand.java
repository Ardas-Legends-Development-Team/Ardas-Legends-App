package com.ardaslegends.presentation.discord.commands.register;

import com.ardaslegends.domain.Player;
import com.ardaslegends.presentation.discord.commands.ALCommand;
import com.ardaslegends.presentation.discord.commands.ALCommandExecutor;
import com.ardaslegends.presentation.discord.commands.ALMessageResponse;
import com.ardaslegends.presentation.discord.config.BotProperties;
import com.ardaslegends.presentation.discord.utils.ALColor;
import com.ardaslegends.service.PlayerService;
import com.ardaslegends.service.dto.player.CreatePlayerDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.javacord.api.DiscordApi;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.interaction.*;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

@RequiredArgsConstructor

@Slf4j
@Component
public class RegisterCommand implements ALCommand, ALCommandExecutor {

    private final DiscordApi api;
    private final PlayerService playerService;
    @Override
    public SlashCommandBuilder init(Map<String, ALCommandExecutor> commands) {
        log.debug("Initializing /register command");

        var command = SlashCommand.with("register", "JAVACORDDDDD Register in the roleplay system", Arrays.asList(
                new SlashCommandOptionBuilder()
                        .setType(SlashCommandOptionType.STRING)
                        .setName("ign")
                        .setDescription("Your minecraft in-game name (IGN)")
                        .setRequired(true)
                        .build(),
                new SlashCommandOptionBuilder()
                        .setType(SlashCommandOptionType.STRING)
                        .setName("faction-name")
                        .setDescription("The faction you want to join")
                        .setRequired(true)
                        .build()
        ));

        commands.put("register", this::execute);
        log.info("Finished initializing /register command");
        return command;
    }


    @Override
    public ALMessageResponse execute(SlashCommandInteraction interaction, List<SlashCommandInteractionOption> options, BotProperties properties) {
        log.debug("Incoming /register request");

        String ign = getStringOption("ign", options);
        String factionName = getStringOption("faction-name", options);
        String discordId = interaction.getUser().getIdAsString();


        log.trace("Building dto");
        CreatePlayerDto dto = new CreatePlayerDto(ign, discordId, factionName);
        log.debug("Built dto with data [{}]", dto);

        log.trace("Calling playerService.createPlayer");
        Player player = discordServiceExecution(dto, playerService::createPlayer, "Error while registering!");

        log.debug("Building response Embed");
        return new ALMessageResponse(null, new EmbedBuilder()
                .setTitle("Registered")
                .setDescription("You were successfully registered in the Bot's system!")
                .addField("Ign", player.getIgn(), true)
                .addField("Faction", player.getFaction().getName(), true)
                .setThumbnail(getFactionBanner(factionName))
                .setColor(ALColor.GREEN)
                .setTimestampToNow());
    }
}
