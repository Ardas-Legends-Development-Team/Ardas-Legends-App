package com.ardaslegends.data.presentation.discord.commands.bind;


import com.ardaslegends.data.presentation.discord.exception.BotException;
import com.ardaslegends.data.presentation.discord.commands.ALCommandExecutor;
import com.ardaslegends.data.service.exceptions.ServiceException;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.interaction.SlashCommandInteraction;

public class BindArmyOrCompanyCommand implements ALCommandExecutor {
    @Override
    public EmbedBuilder execute(SlashCommandInteraction interaction) {

        try {
            throw new NullPointerException("Value was null");
        }
        catch (ServiceException exception) {
            throw new BotException("Error while binding army or company", exception);
        }

//        return new EmbedBuilder()
//                .setTitle("Kek pls Work")
//                .setDescription("User that executed:" + interaction.getUser().getDiscriminatedName())
//                .setColor(Color.GREEN)
//                .setTimestampToNow();
    }
}