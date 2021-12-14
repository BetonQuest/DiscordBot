package org.betonquest.discordbot;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.betonquest.discordbot.config.BetonBotConfig;
import org.betonquest.discordbot.modules.welcome.MyListener;

import javax.security.auth.login.LoginException;
import java.io.IOException;

/**
 * This is the main class of the Discord Bot.
 */
public class DiscordBot {
    private static final Logger logger = LogManager.getLogger();

    public static void main(final String[] args) {
        final BetonBotConfig config;
        final JDA api;
        try {
            config = new BetonBotConfig("config.yml");
            if (config.token.isEmpty()) {
                logger.error("You need to set the token in the 'config.yml'");
                return;
            }
            api = JDABuilder.createDefault(config.token).build();

        } catch (final IOException e) {
            logger.error("Could not read the config file 'config.yml'! Reason: ", e);
            return;
        } catch (final LoginException e) {
            logger.error("Could not connect to Discord server! Reason: ", e);
            return;
        }
        api.addEventListener(new MyListener(config));
    }
}
