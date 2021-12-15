package org.betonquest.discordbot.modules.support;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.betonquest.discordbot.config.BetonBotConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CloseCommand extends ListenerAdapter {
    /**
     * Logger instance.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(CloseCommand.class);

    public CloseCommand(final JDA api, final BetonBotConfig config) {
        final TextChannel channel = api.getTextChannelById(config.supportChannelID);
        if (channel == null) {
            LOGGER.warn("No text support channel with the id '" + config.supportChannelID + "' was found!");
            return;
        }
        channel.getGuild().upsertCommand("close", "Close a support thread").queue();
        api.addEventListener(this);
    }

    @Override
    public void onSlashCommand(final SlashCommandEvent event) {
        if (!event.getName().equals("close")) {
            return;
        }
        event.reply("Closing the Ticket").queue();
    }
}
