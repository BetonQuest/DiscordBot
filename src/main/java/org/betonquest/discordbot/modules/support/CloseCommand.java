package org.betonquest.discordbot.modules.support;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.entities.Emoji;
import net.dv8tion.jda.api.entities.GuildChannel;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.ThreadChannel;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.betonquest.discordbot.config.BetonBotConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * A `close` command to close support threads in a parent channel.
 */
public class CloseCommand extends ListenerAdapter {
    /**
     * The command name.
     */
    public static final String COMMAND = "close";
    /**
     * Logger instance.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(CloseCommand.class);
    /**
     * A list of all support channel IDs.
     */
    private final List<Long> supportChannels;
    /**
     * The {@link MessageEmbed} if a support channel is closed.
     */
    private final MessageEmbed supportClosedEmbed;
    /**
     * The {@link Emoji} if a support channel is closed.
     */
    private Emoji supportClosedEmoji;

    /**
     * Create a new `close` command instance.
     *
     * @param api    The {@link JDA} instance
     * @param config The {@link BetonBotConfig} instance
     */
    public CloseCommand(final JDA api, final BetonBotConfig config) {
        super();
        supportChannels = config.getSupportChannelsIDs();
        supportClosedEmbed = config.supportClosedEmbed;
        if (supportChannels.isEmpty()) {
            return;
        }
        if (config.supportClosedEmbed == null) {
            LOGGER.warn("No support closed message was found or set!");
        }
        if (config.supportClosedEmoji == null) {
            LOGGER.warn("No support closed emoji was found or set!");
        } else {
            supportClosedEmoji = Emoji.fromUnicode(config.supportClosedEmoji);
        }
        if (config.registerCommands) {
            api.upsertCommand(COMMAND, "Close a support thread").queue();
        }
        api.addEventListener(this);
    }

    @Override
    public void onSlashCommand(final SlashCommandEvent event) {
        if (!COMMAND.equals(event.getName())) {
            return;
        }
        if (!(event.getChannelType().equals(ChannelType.GUILD_PUBLIC_THREAD) || event.getChannelType().equals(ChannelType.GUILD_PRIVATE_THREAD))
                || !supportChannels.contains(((ThreadChannel) event.getChannel()).getParentChannel().getIdLong())) {
            event.reply("This command is only supported in threads in a channel that is a support channel!")
                    .setEphemeral(true).queue();
            return;
        }
        close(event);
    }

    private void close(final SlashCommandEvent event) {
        final GuildChannel channel = (GuildChannel) event.getChannel();
        if (supportClosedEmoji != null && channel.getName().startsWith(supportClosedEmoji.getAsMention())) {
            event.reply("This thread was already closed!").setEphemeral(true).queue();
            return;
        }
        if (supportClosedEmoji != null) {
            channel.getManager().setName(supportClosedEmoji.getAsMention() + channel.getName()).queue();
        }
        final String emoji = supportClosedEmoji == null ? "" : supportClosedEmoji.getAsMention();
        if (supportClosedEmbed == null) {
            event.reply(emoji + "Thread closed").setEphemeral(true).queue();
        } else {
            event.replyEmbeds(supportClosedEmbed).complete();
        }
    }
}
