package org.betonquest.discordbot.modules.welcome;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageType;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.betonquest.discordbot.config.BetonBotConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This listener adds a reaction to discords welcome message.
 */
public class WelcomeMessageListener extends ListenerAdapter {
    /**
     * Logger instance.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(WelcomeMessageListener.class);
    /**
     * The emoji to react with.
     */
    private final Emoji emoji;

    /**
     * Create a new {@link WelcomeMessageListener}
     *
     * @param api    the {@link JDA} instance
     * @param config the {@link BetonBotConfig} instance
     */
    public WelcomeMessageListener(final JDA api, final BetonBotConfig config) {
        super();
        this.emoji = getEmoji(config.welcomeEmoji);

        if (emoji != null) {
            api.addEventListener(this);
        }
    }

    @Override
    public void onMessageReceived(final MessageReceivedEvent event) {
        if (event.getAuthor().isBot()) {
            return;
        }
        if (event.getMessage().getType().equals(MessageType.GUILD_MEMBER_JOIN)) {
            final Message message = event.getMessage();
            message.addReaction(this.emoji).queue();
        }
    }

    private Emoji getEmoji(final String stringEmoji) {
        try {
            return Emoji.fromFormatted(stringEmoji);
        } catch (final IllegalArgumentException e) {
            LOGGER.warn("No welcome emoji was found or set! Reason: {}", e.getMessage());
            return null;
        }
    }
}
