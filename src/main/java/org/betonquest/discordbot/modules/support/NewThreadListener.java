package org.betonquest.discordbot.modules.support;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.ThreadChannel;
import net.dv8tion.jda.api.events.channel.ChannelCreateEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.betonquest.discordbot.config.BetonBotConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
        final Role role = config.getGuild().getRoleById(config.supportSubscriptionRoleID);
        final String roleMention = role == null ? "<role not found>" : role.getAsMention();

        channel.getHistoryFromBeginning(1).queue(history -> {
            final Message firstMessage = history.getRetrievedHistory().get(0);
            final Member member = firstMessage.getReferencedMessage() == null ? firstMessage.getMember() : firstMessage.getReferencedMessage().getMember();
            final String memberMention = member == null ? "" : member.getAsMention();

            channel.sendMessage("Hey " + memberMention).queue();
            channel.sendMessageEmbeds(config.supportNewEmbed.getEmbed()).queue();
            channel.sendMessage("Hey " + roleMention + ", someone needs your help!").queue();
        }, fail -> {
            channel.sendMessageEmbeds(config.supportNewEmbed.getEmbed()).queue();
            channel.sendMessage("Hey " + roleMention + ", someone needs your help!").queue();
        });
    }
}
