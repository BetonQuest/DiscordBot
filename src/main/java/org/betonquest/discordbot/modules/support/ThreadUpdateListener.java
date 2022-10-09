package org.betonquest.discordbot.modules.support;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.channel.concrete.ThreadChannel;
import net.dv8tion.jda.api.entities.channel.forums.ForumTag;
import net.dv8tion.jda.api.events.channel.update.ChannelUpdateAppliedTagsEvent;
import net.dv8tion.jda.api.events.channel.update.ChannelUpdateArchivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.betonquest.discordbot.config.BetonBotConfig;
import org.betonquest.discordbot.modules.ForumTagHolder;
import org.jetbrains.annotations.NotNull;

import java.util.List;

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
        final ThreadChannel channel = event.getChannel().asThreadChannel();

        final ForumTagHolder tagHolder = new ForumTagHolder(channel);

        if (isSolved(event.getAddedTags())) {
            tagHolder.remove(config.supportTagUnsolved);
        }

        tagHolder.apply(config.supportTagOrder);

        channel.getManager().setAutoArchiveDuration(ThreadChannel.AutoArchiveDuration.TIME_1_HOUR).queue();
    }

    @Override
    public void onChannelUpdateArchived(@NotNull final ChannelUpdateArchivedEvent event) {
        if (!(event.getChannel() instanceof ThreadChannel) || !config.supportChannelIDs.contains(event.getChannel().getIdLong())) {
            return;
        }

        final ThreadChannel channel = event.getChannel().asThreadChannel();
        if (isSolved(channel.getAppliedTags())) {
            channel.getManager().setArchived(true).queue();
        }
    }

    /**
     * Checks for the "solved" tag from config in the given {@link List} of {@link ForumTag}s.
     *
     * @param channelTags the TagList
     * @return true if the {@link List} contains the "solved" tag, otherwise false
     */
    private boolean isSolved(final List<ForumTag> channelTags) {
        return channelTags.stream().anyMatch(tag -> config.supportTagSolved.equals(tag.getIdLong()));
    }
}
