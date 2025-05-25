package org.betonquest.discordbot.modules.support;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.entities.channel.concrete.ThreadChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import org.betonquest.discordbot.config.BetonBotConfig;
import org.betonquest.discordbot.modules.ForumTagHolder;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A `solve` command to close support threads in a parent channel.
 */
public class SolveCommand extends ListenerAdapter {
    /**
     * The command name.
     */
    public static final String COMMAND = "solve";

    /**
     * Logger instance.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(SolveCommand.class);

    /**
     * The {@link BetonBotConfig} instance.
     */
    private final BetonBotConfig config;

    /**
     * Create a new `solve` command instance.
     *
     * @param api    The {@link JDA} instance
     * @param config The {@link BetonBotConfig} instance
     */
    public SolveCommand(final JDA api, final BetonBotConfig config) {
        super();
        this.config = config;
        if (config.supportChannelIDs.isEmpty()) {
            LOGGER.warn("No support channels where found or set!");
            return;
        }
        if (config.supportSolvedEmbed == null) {
            LOGGER.warn("No support closed message was found or set!");
        }
        api.addEventListener(this);
    }

    /**
     * Get the slash command data for this command.
     *
     * @return The slash command data
     */
    public @NotNull SlashCommandData getSlashCommandData() {
        return Commands.slash(COMMAND, "Mark a support thread as solved.")
                .setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.ADMINISTRATOR));
    }

    @Override
    public void onSlashCommandInteraction(final SlashCommandInteractionEvent event) {
        if (!COMMAND.equals(event.getName())) {
            return;
        }
        if (!(event.getChannelType().equals(ChannelType.GUILD_PUBLIC_THREAD) || event.getChannelType().equals(ChannelType.GUILD_PRIVATE_THREAD))
                || !config.supportChannelIDs.contains(((ThreadChannel) event.getChannel()).getParentChannel().getIdLong())) {
            event.reply("This command is only supported in threads in a channel that is a support channel!")
                    .setEphemeral(true)
                    .queue();
            return;
        }
        close(event);
    }

    private void close(final SlashCommandInteractionEvent event) {
        final ThreadChannel channel = (ThreadChannel) event.getChannel();
        if (config.supportSolvedEmbed == null) {
            event.reply("Post solved.").setEphemeral(true).queue();
        } else {
            event.replyEmbeds(config.supportSolvedEmbed.getEmbed()).queue();
        }

        new ForumTagHolder(channel)
                .add(config.supportTagsSolved)
                .apply(config.supportTagsOrder);
    }
}
