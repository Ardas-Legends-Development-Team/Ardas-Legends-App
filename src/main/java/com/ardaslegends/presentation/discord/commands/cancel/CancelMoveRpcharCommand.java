package com.ardaslegends.presentation.discord.commands.cancel;

import com.ardaslegends.domain.Movement;
import com.ardaslegends.domain.RPChar;
import com.ardaslegends.presentation.discord.commands.ALCommandExecutor;
import com.ardaslegends.presentation.discord.commands.ALMessageResponse;
import com.ardaslegends.presentation.discord.config.BotProperties;
import com.ardaslegends.presentation.discord.utils.ALColor;
import com.ardaslegends.presentation.discord.utils.Thumbnails;
import com.ardaslegends.service.MovementService;
import com.ardaslegends.service.dto.player.DiscordIdDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.entity.user.User;
import org.javacord.api.interaction.SlashCommandInteraction;
import org.javacord.api.interaction.SlashCommandInteractionOption;

import java.util.List;

@RequiredArgsConstructor

@Slf4j
public class CancelMoveRpcharCommand implements ALCommandExecutor {

    private final MovementService movementService;

    @Override
    public ALMessageResponse execute(SlashCommandInteraction interaction, List<SlashCommandInteractionOption> options, BotProperties properties) {
        log.debug("Executing /cancel move rpchar request");

        User user = interaction.getUser();
        log.debug("User: discord name [{}] - id [{}]", user.getName(), user.getIdAsString());

        log.trace("Building dto");
        DiscordIdDto dto = new DiscordIdDto(user.getIdAsString());
        log.debug("Built dto with data [{}]", dto);

        log.trace("Calling movementService");
        Movement movement = discordServiceExecution(dto, movementService::cancelRpCharMovement, "Error while cancelling RpChar Movement");

        RPChar rpChar = movement.getRpChar();

        log.debug("Building response Embed");
        return new ALMessageResponse(null, new EmbedBuilder()
                .setTitle("Cancelled RpChar Movement")
                .setDescription("The character %s - %s stopped their movement towards region %s.\nThe character is now in region %s.".formatted(rpChar.getName(), rpChar.getTitle(), movement.getDestinationRegionId(), rpChar.getCurrentRegion().getId()))
                .setColor(ALColor.GREEN)
                .addInlineField("Character", rpChar.getName())
                .addInlineField("Player", rpChar.getOwner().getIgn())
                .addInlineField("User", user.getMentionTag())
                .addField("Current Region", rpChar.getCurrentRegion().getId())
                .setThumbnail(Thumbnails.MOVE_CHARACTER.getUrl())
                .setTimestampToNow());
    }
}
