package com.ardaslegends.presentation.discord.commands.update.staff;

import com.ardaslegends.domain.RPChar;
import com.ardaslegends.presentation.discord.commands.ALMessageResponse;
import com.ardaslegends.presentation.discord.commands.ALStaffCommandExecutor;
import com.ardaslegends.presentation.discord.config.BotProperties;
import com.ardaslegends.presentation.discord.utils.ALColor;
import com.ardaslegends.service.PlayerService;
import com.ardaslegends.service.dto.player.rpchar.UpdateRpCharDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.entity.user.User;
import org.javacord.api.interaction.SlashCommandInteraction;
import org.javacord.api.interaction.SlashCommandInteractionOption;

import java.util.List;

@RequiredArgsConstructor

@Slf4j
public class UpdateRpcharNameCommand implements ALStaffCommandExecutor {


    private final PlayerService playerService;

    @Override
    public ALMessageResponse execute(SlashCommandInteraction interaction, List<SlashCommandInteractionOption> options, BotProperties properties) {
        log.debug("Executing /update rpchar name request");

        checkStaff(interaction, properties.getStaffRoleIds());

        log.debug("Getting options");
        User user = getUserOption("player", options);
        log.debug("User: discord name [{}] - id [{}]", user.getName(), user.getIdAsString());

        String name = getStringOption("new-name", options);
        log.debug("Name: [{}]", name);

        log.trace("Building dto");
        UpdateRpCharDto dto = new UpdateRpCharDto(user.getIdAsString(), name, null, null, null, null, null);
        log.debug("Built dto with data [{}]", dto);

        log.trace("Calling playerService");
        RPChar rpChar = discordServiceExecution(dto, playerService::updateCharacterName, "Error while updating RpChar name");

        log.debug("Building response Embed");
        return new ALMessageResponse(null, new EmbedBuilder()
                .setTitle("Updated RpChar name")
                .setDescription("Successfully changed name of Roleplay Character to %s".formatted(rpChar.getName()))
                .addInlineField("User", "%s".formatted(user.getMentionTag()))
                .addInlineField("New Name", "%s".formatted(rpChar.getName()))
                .setColor(ALColor.YELLOW)
                .setTimestampToNow());
    }

}
