package org.betonquest.discordbot.modules.support;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.channel.concrete.ThreadChannel;
import net.dv8tion.jda.api.events.channel.update.ChannelUpdateAppliedTagsEvent;
import net.dv8tion.jda.api.events.channel.update.ChannelUpdateArchivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.betonquest.discordbot.config.BetonBotConfig;
import org.betonquest.discordbot.modules.ForumTagHolder;
import org.jetbrains.annotations.NotNull;

/**
 * This listener sorts and adds tags when Users change them.
 * It also closes Forum Posts when they are solved and inactive.
 */
public class ThreadUpdateListener extends ListenerAdapter {

    /**
     * The {@link BetonBotConfig} instance.
     */
    private final BetonBotConfig config;

    /**
     * Create a new {@link ThreadUpdateListener}
     *
     * @param api    the {@link JDA} instance
     * @param config the {@link BetonBotConfig} instance
     */
    public ThreadUpdateListener(final JDA api, final BetonBotConfig config) {
        super();
        this.config = config;
        api.addEventListener(this);
    }

    @Override
    public void onChannelUpdateAppliedTags(@NotNull final ChannelUpdateAppliedTagsEvent event) {
        if (!(event.getChannel() instanceof ThreadChannel) || isNotSupportChannel(event.getChannel().asThreadChannel())) {
            return;
        }
        final ThreadChannel channel = event.getChannel().asThreadChannel();
        if (channel.isArchived()) {
            return;
        }
        final ForumTagHolder tagHolder = new ForumTagHolder(channel);

        tagHolder.apply(config.supportTagsOrder);
    }

    @Override
    public void onChannelUpdateArchived(@NotNull final ChannelUpdateArchivedEvent event) {
        if (!(event.getChannel() instanceof ThreadChannel) || isNotSupportChannel(event.getChannel().asThreadChannel())) {
            return;
        }
        final ThreadChannel channel = event.getChannel().asThreadChannel();

        if (channel.isArchived() && !ForumTagHolder.isSolved(channel.getAppliedTags(), config)) {
            channel.getManager().setArchived(false).queue();
        } else if (!channel.isArchived()) {
            new ForumTagHolder(channel)
                    .remove(config.supportTagsSolved)
                    .apply(config.supportTagsOrder);
        }
    }

    /**
     * Checks if the given Threads Parent Channel is <b>not</b> a SupportChannel.
     *
     * @param channel The Thread to check
     * @return true if not a support channel, otherwise false.
     */
    private boolean isNotSupportChannel(final ThreadChannel channel) {
        return !config.supportChannelIDs.contains(channel.getParentChannel().getIdLong());
    }
}
