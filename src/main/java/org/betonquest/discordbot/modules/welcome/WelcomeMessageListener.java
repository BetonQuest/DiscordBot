package org.betonquest.discordbot.modules.welcome;

import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageType;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.betonquest.discordbot.config.BetonBotConfig;

/**
 * This listener adds a reaction to discords welcome message.
 */
public class WelcomeMessageListener extends ListenerAdapter {
    /**
     * The {@link BetonBotConfig} instance.
     */
    private final BetonBotConfig config;

    /**
     * Create a new {@link WelcomeMessageListener}
     *
     * @param config the {@link BetonBotConfig} instance
     */
    public WelcomeMessageListener(final BetonBotConfig config) {
        super();
        this.config = config;
    }

    @Override
    public void onMessageReceived(final MessageReceivedEvent event) {
        if (event.getAuthor().isBot()) {
            return;
        }
        if (event.getMessage().getType().equals(MessageType.GUILD_MEMBER_JOIN)) {
            final Message message = event.getMessage();
            message.addReaction(config.welcomeEmoji).complete();
        }
    }
}
