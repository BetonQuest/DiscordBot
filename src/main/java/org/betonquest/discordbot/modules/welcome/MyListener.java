package org.betonquest.discordbot.modules.welcome;

import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.betonquest.discordbot.config.BetonBotConfig;

public class MyListener extends ListenerAdapter {
    BetonBotConfig config;

    public MyListener(final BetonBotConfig config) {
        this.config = config;
    }

    @Override
    public void onMessageReceived(final MessageReceivedEvent event) {
        if (event.getAuthor().isBot()) {
            return;
        }
        final Message message = event.getMessage();
        final String content = message.getContentRaw();
        if (content.equals("!ping")) {
            message.addReaction(config.welcomeEmoji).complete();
        }
    }
}
