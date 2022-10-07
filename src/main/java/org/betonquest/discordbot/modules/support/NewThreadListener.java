package org.betonquest.discordbot.modules.support;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.entities.channel.concrete.ThreadChannel;
import net.dv8tion.jda.api.entities.channel.forums.ForumTag;
import net.dv8tion.jda.api.entities.channel.forums.ForumTagSnowflake;
import net.dv8tion.jda.api.events.channel.ChannelCreateEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.managers.channel.concrete.ThreadChannelManager;
import org.betonquest.discordbot.config.BetonBotConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.stream.Stream;

/**
 * This listener adds a reaction to discords welcome message.
 */
public class NewThreadListener extends ListenerAdapter {
    /**
     * Logger instance.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(NewThreadListener.class);
    /**
     * The {@link BetonBotConfig} instance.
     */
    private final BetonBotConfig config;

    /**
     * Create a new {@link NewThreadListener}
     *
     * @param api    the {@link JDA} instance
     * @param config the {@link BetonBotConfig} instance
     */
    public NewThreadListener(final JDA api, final BetonBotConfig config) {
        super();
        this.config = config;
        if (config.supportChannelIDs.isEmpty()) {
            LOGGER.warn("No support channels where found or set!");
            return;
        }
        api.addEventListener(this);
    }

    @Override
    public void onChannelCreate(final ChannelCreateEvent event) {
        if (!(event.getChannelType().equals(ChannelType.GUILD_PUBLIC_THREAD) || event.getChannelType().equals(ChannelType.GUILD_PRIVATE_THREAD))
                || !config.supportChannelIDs.contains(((ThreadChannel) event.getChannel()).getParentChannel().getIdLong())
                || ((ThreadChannel) event.getChannel()).getSelfThreadMember() != null) {
            return;
        }
        final ThreadChannel channel = (ThreadChannel) event.getChannel();
        final ThreadChannelManager channelManager = channel.getManager();

        final Stream<ForumTagSnowflake> appliedTagsToKeep = channel.getAppliedTags()
                .stream()
                .map(ForumTag::getIdLong)
                .filter(tagId -> !tagId.equals(config.supportTagSolved))
                .map(ForumTagSnowflake::fromId);

        final ForumTagSnowflake[] finalTags = Stream.concat(
                        Stream.of(ForumTagSnowflake.fromId(config.supportTagUnsolved)),
                        appliedTagsToKeep)
                .toList()
                .toArray(new ForumTagSnowflake[0]);

        channelManager.setAppliedTags(finalTags).queue();
    }
}
