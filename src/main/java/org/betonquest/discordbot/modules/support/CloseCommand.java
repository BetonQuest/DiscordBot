package org.betonquest.discordbot.modules.support;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.entities.Emoji;
import net.dv8tion.jda.api.entities.GuildChannel;
import net.dv8tion.jda.api.entities.ThreadChannel;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.privileges.CommandPrivilege;
import org.betonquest.discordbot.config.BetonBotConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;
import java.util.stream.Collectors;

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
     * The {@link BetonBotConfig} instance.
     */
    private final BetonBotConfig config;
    /**
     * The {@link Emoji} if a support channel is closed.
     */
    @SuppressWarnings("PMD.ImmutableField")
    private Emoji supportClosedEmoji;

    /**
     * Create a new `close` command instance.
     *
     * @param api    The {@link JDA} instance
     * @param config The {@link BetonBotConfig} instance
     */
    public CloseCommand(final JDA api, final BetonBotConfig config) {
        super();
        this.config = config;
        if (config.supportChannelIDs.isEmpty()) {
            LOGGER.warn("No support channels where found or set!");
            return;
        }
        if (config.supportClosedEmbed == null) {
            LOGGER.warn("No support closed message was found or set!");
            return;
        }
        if (config.supportClosedEmoji == null) {
            LOGGER.warn("No support closed emoji was found or set!");
            return;
        }
        supportClosedEmoji = Emoji.fromUnicode(config.supportClosedEmoji);

        if (config.updateCommands) {
            api.upsertCommand(COMMAND, "Close a support thread").setDefaultEnabled(false).queue((createdCommand) -> {
                final Set<CommandPrivilege> collect = config.supportRoleIDs.stream().map(CommandPrivilege::enableRole).collect(Collectors.toSet());
                createdCommand.updatePrivileges(config.getGuild(), collect).queue();
            });
        }
        api.addEventListener(this);
    }

    @Override
    public void onSlashCommand(final SlashCommandEvent event) {
        if (!COMMAND.equals(event.getName())) {
            return;
        }
        if (!(event.getChannelType().equals(ChannelType.GUILD_PUBLIC_THREAD) || event.getChannelType().equals(ChannelType.GUILD_PRIVATE_THREAD))
                || !config.supportChannelIDs.contains(((ThreadChannel) event.getChannel()).getParentChannel().getIdLong())) {
            event.reply("This command is only supported in threads in a channel that is a support channel!")
                    .setEphemeral(true).queue();
            return;
        }
        close(event);
    }

    private void close(final SlashCommandEvent event) {
        final GuildChannel channel = (GuildChannel) event.getChannel();
        if (supportClosedEmoji != null && channel.getName().startsWith(supportClosedEmoji.getAsMention())) {
            event.reply("This thread is already closed!").setEphemeral(true).queue();
            return;
        }
        final String emoji = supportClosedEmoji == null ? "" : supportClosedEmoji.getAsMention();
        if (config.supportClosedEmbed == null) {
            event.reply(emoji + "Thread closed").setEphemeral(true).queue();
        } else {
            event.replyEmbeds(config.supportClosedEmbed).queue();
        }
        channel.getManager().setName(emoji + channel.getName()).queue();
    }
}
