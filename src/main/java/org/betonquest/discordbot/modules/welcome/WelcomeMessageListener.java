package org.betonquest.discordbot.modules.welcome;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageType;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.betonquest.discordbot.config.BetonBotConfig;

/**
 * This listener adds a reaction to discords welcome message.
 */
public class WelcomeMessageListener extends ListenerAdapter {
    /**
     * Logger instance.
     */
    private static final Logger LOGGER = LogManager.getLogger();
    /**
     * The emoji to react with.
     */
    private final String emoji;

    /**
     * Create a new {@link WelcomeMessageListener}
     *
     * @param api    the {@link JDA} instance
     * @param config the {@link BetonBotConfig} instance
     */
    public WelcomeMessageListener(final JDA api, final BetonBotConfig config) {
        super();
        emoji = config.welcomeEmoji;
        if (emoji == null || emoji.isEmpty()) {
            LOGGER.warn("No welcome emoji was found or set!");
            return;
        }
        api.addEventListener(this);
    }

    @Override
    public void onMessageReceived(final MessageReceivedEvent event) {
        if (event.getAuthor().isBot()) {
            return;
        }
        if (event.getMessage().getType().equals(MessageType.GUILD_MEMBER_JOIN)) {
            final Message message = event.getMessage();
            message.addReaction(emoji).queue();
        }
    }
}
