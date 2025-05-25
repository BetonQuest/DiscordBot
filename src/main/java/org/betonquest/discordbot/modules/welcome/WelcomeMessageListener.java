package org.betonquest.discordbot.modules.welcome;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageType;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.Nullable;

/**
 * This listener adds a reaction to discords welcome message.
 */
public class WelcomeMessageListener extends ListenerAdapter {
    /**
     * The emoji to react with.
     */
    private final Emoji emoji;

    /**
     * Create a new {@link WelcomeMessageListener}
     *
     * @param api          the {@link JDA} instance
     * @param welcomeEmoji the welcome emoji to send to every new member
     * @throws IllegalArgumentException if the welcome emoji is null
     * @throws IllegalStateException    if the welcome emoji is not valid
     */
    public WelcomeMessageListener(final JDA api, @Nullable final String welcomeEmoji) {
        super();
        this.emoji = getEmoji(welcomeEmoji);
        api.addEventListener(this);
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

    private Emoji getEmoji(@Nullable final String stringEmoji) {
        if (stringEmoji == null) {
            throw new IllegalArgumentException("No welcome emoji was set!");
        }
        try {
            return Emoji.fromFormatted(stringEmoji);
        } catch (final IllegalArgumentException e) {
            throw new IllegalStateException("The welcome emoji '" + stringEmoji + "' is not valid!", e);
        }
    }
}
