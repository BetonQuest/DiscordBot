package org.betonquest.discordbot.modules.support;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.ThreadChannel;
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

        if (!(event.getChannelType().equals(ChannelType.GUILD_PUBLIC_THREAD) || event.getChannelType().equals(ChannelType.GUILD_PRIVATE_THREAD))
                || ((ThreadChannel) event.getChannel()).getParentChannel().getIdLong() != supportChannel.getIdLong()) {
            event.reply("This command is only supported in threads under the channel " + supportChannel.getAsMention() + "!")
                    .setEphemeral(true).queue();
            return;
        }

        event.reply("Closing the Ticket").queue();
    }
}
