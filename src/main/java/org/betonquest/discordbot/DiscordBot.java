package org.betonquest.discordbot;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import org.betonquest.discordbot.config.BetonBotConfig;
import org.betonquest.discordbot.modules.welcome.WelcomeMessageListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.security.auth.login.LoginException;
import java.io.IOException;

/**
 * This is the main class of the discord Bot.
 */
public final class DiscordBot {
    /**
     * Logger instance.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(DiscordBot.class);

    /**
     * Empty constructor.
     */
    private DiscordBot() {
    }

    /**
     * Starts the discord bot.
     *
     * @param args The args from the vm start
     */
    public static void main(final String[] args) {
        final BetonBotConfig config;
        final JDA api;
        try {
            config = new BetonBotConfig("config.yml");
            if (config.token.isEmpty()) {
                LOGGER.error("You need to set the token in the 'config.yml'");
                return;
            }
            api = JDABuilder.createDefault(config.token).build();

        } catch (final IOException e) {
            LOGGER.error("Could not read the config file 'config.yml'! Reason: ", e);
            return;
        } catch (final LoginException e) {
            LOGGER.error("Could not connect to Discord server! Reason: ", e);
            return;
        }

        new WelcomeMessageListener(api, config);
    }
}
