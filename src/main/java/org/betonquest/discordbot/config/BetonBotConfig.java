package org.betonquest.discordbot.config;

import com.google.common.collect.Lists;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * This is a configuration to load settings from a file.
 */
@SuppressWarnings("PMD.DataClass")
public class BetonBotConfig {
    /**
     * Logger instance.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(BetonBotConfig.class);

    /**
     * The token to connect to Discord.
     */
    public final String token;
    /**
     * The guild id for the target Discord server.
     */
    public final Long guildID;
    /**
     * Should the commands be registered again.
     */
    public final boolean updateCommands;
    /**
     * The emoji to react on discords welcome message.
     */
    public final String welcomeEmoji;
    /**
     * The ids of the support channels.
     */
    public final List<Long> supportChannelIDs;
    /**
     * The message to show, when a thread was marked as solved.
     */
    public final ConfigEmbedBuilder supportSolvedEmbed;
    /**
     * The Tag-ID to apply to solved Support Posts.
     */
    public final Long supportTagSolved;
    /**
     * The Tag-ID to apply to unsolved Support Posts.
     */
    public final Long supportTagUnsolved;
    /**
     * A List of Tag-IDs to keep when changing a Post from Unsolved to Solved.
     */
    public final List<Long> supportTagsToKeep;
    /**
     * The {@link Guild} of the Discord managed by this bot.
     */
    private Guild guild;

    /**
     * @param configPath the path of the config file
     * @throws IOException is thrown, when reading or writing the file coursed problems.
     */
    public BetonBotConfig(final Path configPath) throws IOException {
        final Yaml yaml = getYaml();
        final Map<String, Object> config = getConfig(yaml, configPath);

        token = checkEmpty(getOrCreate("Token", "", config));
        guildID = getOrCreate("GuildID", -1L, config);
        updateCommands = getOrCreate("UpdateCommands", true, config);
        welcomeEmoji = checkEmpty(String.valueOf(getOrCreate("WelcomeEmoji", "U+1F44B", config)));
        supportChannelIDs = getOrCreate("Support.ChannelIDs", Lists.newArrayList(-1L), config);
        supportTagSolved = getOrCreate("Support.Tags.Solved", -1L, config);
        supportTagUnsolved = getOrCreate("Support.Tags.Unsolved", -1L, config);
        supportTagsToKeep = getOrCreate("Support.Tags.Keep", Lists.newArrayList(-1L), config);
        supportSolvedEmbed = getOrCreateEmbed("Support.SolvedMessage", config);

        if (updateCommands) {
            config.put("UpdateCommands", false);
        }
        yaml.dump(config, Files.newBufferedWriter(configPath));
    }

    /**
     * Init things that need a {@link JDA} instance
     *
     * @param api the {@link JDA} instance
     */
    public void init(final JDA api) {
        guild = api.getGuildById(guildID);
        if (guild == null) {
            LOGGER.warn("No guild with the id '" + guildID + "' was found!");
        }
    }

    private Yaml getYaml() {
        final DumperOptions options = new DumperOptions();
        options.setIndent(4);
        options.setIndicatorIndent(2);
        options.setWidth(120);
        options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
        return new Yaml(options);
    }

    private Map<String, Object> getConfig(final Yaml yaml, final Path configPath) throws IOException {
        if (Files.exists(configPath)) {
            return yaml.load(Files.newInputStream(configPath));
        } else {
            return new LinkedHashMap<>();
        }
    }

    @SuppressWarnings({"unchecked", "PMD.AvoidCatchingGenericException"})
    private <T> T getOrCreate(final String key, final T defaultValue, final Map<String, Object> config) {
        final int splitIndex = key.indexOf('.');
        final String firstKey = splitIndex == -1 ? key : key.substring(0, splitIndex);
        final String restKey = splitIndex == -1 ? null : key.substring(splitIndex + 1);
        if (restKey == null) {
            if (config.containsKey(firstKey)) {
                final Object value = config.get(firstKey);
                if (value != null) {
                    try {
                        return (T) value;
                    } catch (final Exception e) {
                        LOGGER.warn("Could not cast Config Entry '" + key + "'. Using default one.", e);
                    }
                }
            }
            config.put(key, defaultValue);
            return defaultValue;
        }
        return getOrCreateSubConfig(defaultValue, config, firstKey, restKey);
    }

    @SuppressWarnings("unchecked")
    private <T> T getOrCreateSubConfig(final T defaultValue, final Map<String, Object> config, final String firstKey, final String restKey) {
        final Map<String, Object> subConfig;
        if (config.containsKey(firstKey)) {
            subConfig = (Map<String, Object>) config.get(firstKey);
        } else {
            subConfig = new LinkedHashMap<>();
            config.put(firstKey, subConfig);
        }
        return getOrCreate(restKey, defaultValue, subConfig);
    }

    private ConfigEmbedBuilder getOrCreateEmbed(final String key, final Map<String, Object> config) {
        return new ConfigEmbedBuilder(
                getOrCreate(key, ConfigEmbedBuilder.getDefaultConfigEmbed(), config)
                , key);
    }

    private String checkEmpty(final String string) {
        return string == null ? null : string.isEmpty() ? null : string;
    }

    /**
     * Get the {@link Guild} of this bot.
     *
     * @return the {@link Guild}
     */
    public Guild getGuild() {
        return guild;
    }
}
