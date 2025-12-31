package org.betonquest.discordbot;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import org.betonquest.discordbot.config.BetonBotConfig;
import org.betonquest.discordbot.modules.promotion.PromoteCommand;
import org.betonquest.discordbot.modules.promotion.PromotionCache;
import org.betonquest.discordbot.modules.support.NewThreadListener;
import org.betonquest.discordbot.modules.support.SolveCommand;
import org.betonquest.discordbot.modules.support.ThreadAutoCloseScheduler;
import org.betonquest.discordbot.modules.support.ThreadUpdateListener;
import org.betonquest.discordbot.modules.welcome.WelcomeMessageListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
        LOGGER.info("Starting Discord Bot ...");
        final BetonBotConfig config;
        final JDA api;
        try {
            config = new BetonBotConfig(Paths.get("config.yml"));
            if (config.token == null) {
                LOGGER.error("You need to set the token in the 'config.yml'");
                return;
            }
            api = JDABuilder.createDefault(config.token)
                    .enableIntents(GatewayIntent.GUILD_MEMBERS, GatewayIntent.MESSAGE_CONTENT)
                    .setMemberCachePolicy(MemberCachePolicy.ALL)
                    .build();
        } catch (final IOException e) {
            LOGGER.error("Could not read the config file 'config.yml'! Reason: ", e);
            return;
        }

        try {
            api.awaitReady();
        } catch (final InterruptedException e) {
            LOGGER.error("Waited for state Ready, but there was an exception! Exception: ", e);
            return;
        }
        final Guild guild = api.getGuildById(config.guildID);
        if (guild == null) {
            LOGGER.error("No guild with the id '{}' was found!", config.guildID);
            return;
        }
        guild.loadMembers().get();

        try {
            new WelcomeMessageListener(api, config.welcomeEmoji);
        } catch (final IllegalArgumentException e) {
            LOGGER.info(e.getMessage(), e);
        } catch (final IllegalStateException e) {
            LOGGER.error(e.getMessage(), e);
        }
        final SolveCommand solveCommand = new SolveCommand(api, config, "solve", "Mark a support thread as solved.",
                () -> config.supportSolvedEmbed);
        final SolveCommand closeCommand = new SolveCommand(api, config, "close", "Mark a support thread as closed.",
                () -> config.supportClosedEmbed);
        new NewThreadListener(api, config);
        new ThreadUpdateListener(api, config);

        new ThreadAutoCloseScheduler(api, config, guild);

        final PromoteCommand promoteCommand;
        try {
            final PromotionCache promotionCache = new PromotionCache(Paths.get("promotionCache.yml"), config);
            promoteCommand = new PromoteCommand(api, config, promotionCache);
        } catch (final IOException e) {
            LOGGER.error("Could not read the promotion cache file 'promotionCache.yml'! Reason: ", e);
            return;
        }

        if (config.updateCommands) {
            api.updateCommands().addCommands(
                    solveCommand.getSlashCommandData(),
                    closeCommand.getSlashCommandData(),
                    promoteCommand.getSlashCommandData()
            ).queue(commands -> LOGGER.info("Updated commands!"));
        }
        LOGGER.info("DiscordBot is ready!");
    }
}
