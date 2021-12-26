package org.betonquest.discordbot;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import org.betonquest.discordbot.config.BetonBotConfig;
import org.betonquest.discordbot.modules.support.CloseCommand;
import org.betonquest.discordbot.modules.support.NewThreadListener;
import org.betonquest.discordbot.modules.welcome.WelcomeMessageListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.security.auth.login.LoginException;
import java.io.IOException;
import java.nio.file.Paths;

/**
 * This is the main class of the Discord Bot.
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
     * Starts the Discord bot.
     *
     * @param args The args from the vm start
     */
    public static void main(final String[] args) {
        final BetonBotConfig config;
        final JDA api;
        try {
            config = new BetonBotConfig(Paths.get("config.yml"));
            if (config.token == null) {
                LOGGER.error("You need to set the token in the 'config.yml'");
                return;
            }
            api = JDABuilder.createDefault(config.token)
                    .enableIntents(GatewayIntent.GUILD_MEMBERS)
                    .setMemberCachePolicy(MemberCachePolicy.ALL)
                    .build();

        } catch (final IOException e) {
            LOGGER.error("Could not read the config file 'config.yml'! Reason: ", e);
            return;
        } catch (final LoginException e) {
            LOGGER.error("Could not connect to Discord server! Reason: ", e);
            return;
        }

        try {
            api.awaitReady();
        } catch (final InterruptedException e) {
            LOGGER.error("Waited for state Ready, but there was an exception! Exception: ", e);
            return;
        }
        config.init(api);
        config.getGuild().loadMembers().get();

        new WelcomeMessageListener(api, config);
        new CloseCommand(api, config);
        new NewThreadListener(api, config);
    }
}
