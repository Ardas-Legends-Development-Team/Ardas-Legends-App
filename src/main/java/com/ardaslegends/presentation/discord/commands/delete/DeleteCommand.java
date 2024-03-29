package com.ardaslegends.presentation.discord.commands.delete;

import com.ardaslegends.presentation.discord.commands.ALCommand;
import com.ardaslegends.presentation.discord.commands.ALCommandExecutor;
import com.ardaslegends.presentation.discord.commands.delete.staff.DeleteArmyOrCompanyCommand;
import com.ardaslegends.presentation.discord.commands.delete.staff.DeleteCharacterCommand;
import com.ardaslegends.presentation.discord.commands.delete.staff.DeleteClaimbuildCommand;
import com.ardaslegends.presentation.discord.commands.delete.staff.DeletePlayerCommand;
import com.ardaslegends.service.ArmyService;
import com.ardaslegends.service.ClaimBuildService;
import com.ardaslegends.service.PlayerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.javacord.api.DiscordApi;
import org.javacord.api.interaction.SlashCommand;
import org.javacord.api.interaction.SlashCommandBuilder;
import org.javacord.api.interaction.SlashCommandOptionBuilder;
import org.javacord.api.interaction.SlashCommandOptionType;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Map;

@Slf4j
@RequiredArgsConstructor

@Component
public class DeleteCommand implements ALCommand {

    private final DiscordApi api;
    private final ClaimBuildService claimBuildService;
    private final PlayerService playerService;
    private final ArmyService armyService;

    @Override
    public SlashCommandBuilder init(Map<String, ALCommandExecutor> commands) {
        log.debug("Initializing /delete commands");

        var command = SlashCommand.with("delete", "Deletes an Entity (RpChar, Player, Army, Claimbuild, etc)", Arrays.asList(
                        new SlashCommandOptionBuilder()
                                .setType(SlashCommandOptionType.SUB_COMMAND)
                                .setName("claimbuild")
                                .setDescription("Javacord Deletes a claimbuild")
                                .setOptions(Arrays.asList(
                                        new SlashCommandOptionBuilder()
                                                .setType(SlashCommandOptionType.STRING)
                                                .setName("claimbuild-name")
                                                .setDescription("The claimbuild that should be deleted")
                                                .setRequired(true)
                                                .build()
                                ))
                                .build(),
                        new SlashCommandOptionBuilder()
                                .setType(SlashCommandOptionType.SUB_COMMAND)
                                .setName("player")
                                .setDescription("Deletes a player entity, including their rpchar. Removes players from 'built by' in claimbuilds")
                                .setOptions(Arrays.asList(
                                        new SlashCommandOptionBuilder()
                                                .setType(SlashCommandOptionType.STRING)
                                                .setName("target-player-id")
                                                .setDescription("The discordId of the player that is to be deleted")
                                                .setRequired(true)
                                                .build()
                                ))
                                .build(),
                        new SlashCommandOptionBuilder()
                                .setType(SlashCommandOptionType.SUB_COMMAND)
                                .setName("character")
                                .setDescription("Javacord Deletes a roleplay character")
                                .setOptions(Arrays.asList(
                                        new SlashCommandOptionBuilder()
                                                .setType(SlashCommandOptionType.USER)
                                                .setName("target")
                                                .setDescription("The player who's character is to be deleted")
                                                .setRequired(true)
                                                .build()
                                ))
                                .build(),
                        new SlashCommandOptionBuilder()
                                .setType(SlashCommandOptionType.SUB_COMMAND)
                                .setName("army-or-company")
                                .setDescription("Javacord Deletes an army or company from the game")
                                .setOptions(Arrays.asList(
                                        new SlashCommandOptionBuilder()
                                                .setType(SlashCommandOptionType.STRING)
                                                .setName("army-or-company-name")
                                                .setDescription("The name of the army/company")
                                                .setRequired(true)
                                                .build()
                                ))
                                .build()
                ));

        commands.put("delete claimbuild", new DeleteClaimbuildCommand(claimBuildService)::execute);
        commands.put("delete player", new DeletePlayerCommand(playerService)::execute);
        commands.put("delete character", new DeleteCharacterCommand(playerService)::execute);
        commands.put("delete army-or-company", new DeleteArmyOrCompanyCommand(armyService)::execute);
        log.info("Finished initializing /delete command");
        return command;
    }
}
