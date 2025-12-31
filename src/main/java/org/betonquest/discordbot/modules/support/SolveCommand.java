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
import org.betonquest.discordbot.config.ConfigEmbedBuilder;
import org.betonquest.discordbot.modules.ForumTagHolder;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.Supplier;

/**
 * A `solve` command to close support threads in a parent channel.
 */
public class SolveCommand extends ListenerAdapter {
    /**
     * Logger instance.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(SolveCommand.class);

    /**
     * The command name.
     */
    public final String command;

    /**
     * The command description.
     */
    public final String description;

    /**
     * The embed supplier.
     */
    private final Supplier<ConfigEmbedBuilder> solveEmbedSupplier;

    /**
     * The {@link BetonBotConfig} instance.
     */
    private final BetonBotConfig config;

    /**
     * Create a new `solve` command instance.
     *
     * @param api                The {@link JDA} instance
     * @param config             The {@link BetonBotConfig} instance
     * @param command            The command name
     * @param description        The command description
     * @param solveEmbedSupplier The embed supplier
     */
    public SolveCommand(final JDA api, final BetonBotConfig config, final String command, final String description,
                        final Supplier<ConfigEmbedBuilder> solveEmbedSupplier) {
        super();
        this.config = config;
        this.command = command;
        this.description = description;
        this.solveEmbedSupplier = solveEmbedSupplier;
        if (config.supportChannelIDs.isEmpty()) {
            LOGGER.warn("No support channels where found or set!");
            return;
        }
        if (solveEmbedSupplier.get() == null) {
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
        return Commands.slash(command, description)
                .setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.ADMINISTRATOR));
    }

    @Override
    public void onSlashCommandInteraction(final SlashCommandInteractionEvent event) {
        if (!command.equals(event.getName())) {
            return;
        }
        if (!(event.getChannelType() == ChannelType.GUILD_PUBLIC_THREAD || event.getChannelType() == ChannelType.GUILD_PRIVATE_THREAD)
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
        if (solveEmbedSupplier.get() == null) {
            event.reply("Post solved.").setEphemeral(true).queue();
        } else {
            event.replyEmbeds(solveEmbedSupplier.get().getEmbed()).queue();
        }

        new ForumTagHolder(channel)
                .add(config.supportTagsSolved)
                .apply(config.supportTagsOrder);
    }
}
