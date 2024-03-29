package com.ardaslegends.presentation.discord.commands.update.staff;

import com.ardaslegends.domain.Player;
import com.ardaslegends.presentation.discord.commands.ALMessageResponse;
import com.ardaslegends.presentation.discord.commands.ALStaffCommandExecutor;
import com.ardaslegends.presentation.discord.config.BotProperties;
import com.ardaslegends.presentation.discord.utils.ALColor;
import com.ardaslegends.service.PlayerService;
import com.ardaslegends.service.dto.player.UpdatePlayerFactionDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.entity.user.User;
import org.javacord.api.interaction.SlashCommandInteraction;
import org.javacord.api.interaction.SlashCommandInteractionOption;

import java.util.List;

@RequiredArgsConstructor

@Slf4j
public class UpdatePlayerFactionCommand implements ALStaffCommandExecutor {

    private final PlayerService playerService;

    @Override
    public ALMessageResponse execute(SlashCommandInteraction interaction, List<SlashCommandInteractionOption> options, BotProperties properties) {
        log.debug("Executing /update player faction request");

        checkStaff(interaction, properties.getStaffRoleIds());

        log.debug("Getting options");
        String factionName = getStringOption("faction-name", options);
        log.debug("factionName: [{}]", factionName);
        User user = getUserOption("player", options);
        log.debug("User: discord name [{}] - id [{}]", user.getName(), user.getIdAsString());

        log.trace("Building dto");
        UpdatePlayerFactionDto dto = new UpdatePlayerFactionDto(user.getIdAsString(), factionName);
        log.debug("Built dto with data [{}]", dto);

        log.trace("Calling playerService");
        Player player = discordServiceExecution(dto, playerService::updatePlayerFaction, "Error while updating Player Faction");

        log.debug("Building response Embed");
        return new ALMessageResponse(null, new EmbedBuilder()
                .setTitle("Updated Player Faction")
                .setDescription("Player %s successfully changed to Faction %s!".formatted(player.getIgn(), player.getFaction().getName()))
                .setColor(ALColor.YELLOW)
                .addInlineField("User", "%s".formatted(user.getMentionTag()))
                .addInlineField("Ign", player.getIgn())
                .addInlineField("Faction", player.getFaction().getName())
                .setThumbnail(getFactionBanner(player.getFaction().getName()))
                .setTimestampToNow());
    }
}
