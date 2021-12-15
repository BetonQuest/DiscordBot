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
    private final TextChannel supportChannel;

    public CloseCommand(final JDA api, final BetonBotConfig config) {
        supportChannel = config.getSupportChannel();
        if (supportChannel == null) {
            LOGGER.warn("No text support channel with the id '" + config.supportChannelID + "' was found!");
            return;
        }
        api.upsertCommand("close", "Close a support thread").queue();
        api.addEventListener(this);
    }

    @Override
    public void onSlashCommand(final SlashCommandEvent event) {
        if (!event.getName().equals("close")) {
            return;
        }
        if (event.getChannel().getIdLong() != supportChannel.getIdLong()) {
            event.reply("This command is only supported in " + supportChannel.getAsMention() + " and it's threads!")
                    .setEphemeral(true).queue();
            return;
        }

        event.reply("Closing the Ticket").queue();
    }
}
