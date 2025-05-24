package org.betonquest.discordbot.config;

import com.google.common.collect.Lists;
import org.jetbrains.annotations.Nullable;
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
    @Nullable
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
    @Nullable
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
    public final Long supportTagsSolved;

    /**
     * The Tag-ID to apply to Support Posts by default if no tag is set.
     */
    public final Long supportTagsDefault;

    /**
     * An Order by which ForumTags are sorted when applied to Support Posts.
     */
    public final List<Long> supportTagsOrder;

    /**
     * A List of Tag-IDs to keep when changing a Post from Unsolved to Solved.
     */
    public final List<Long> supportTagsToKeep;

    /**
     * The Interval the Bot checks for Support Posts that should be Closed.
     */
    public final int supportAutoCloseCheckInterval;

    /**
     * The timeout after which the Bot automatically closes a Support Post.
     */
    public final int supportAutoCloseTimeout;

    /**
     * A ordered List of Roles contained in the Promotion Ladder.
     */
    public final List<Long> promotionRanks;

    /**
     * The message to show when a User was promoted to a new rank.
     */
    public final ConfigEmbedBuilder promotionEmbed;

    /**
     * A List of Roles that can bypass checks in the Promotion System.
     */
    public final List<Long> promotionBypassRoles;

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
        supportTagsSolved = getOrCreate("Support.Tags.Solved", -1L, config);
        supportTagsDefault = getOrCreate("Support.Tags.Default", -1L, config);
        supportTagsToKeep = getOrCreate("Support.Tags.Keep", Lists.newArrayList(-1L), config);
        supportTagsOrder = getOrCreate("Support.Tags.Order", Lists.newArrayList(-1L), config);
        supportSolvedEmbed = getOrCreateEmbed("Support.SolvedMessage", config);
        supportAutoCloseCheckInterval = getOrCreate("Support.AutoCloseCheckInterval", 20, config);
        supportAutoCloseTimeout = getOrCreate("Support.AutoCloseTimeout", 15, config);
        promotionRanks = getOrCreate("Promotion.Ranks", Lists.newArrayList(-1L), config);
        promotionEmbed = getOrCreateEmbed("Promotion.PromotionMessage", config);
        promotionBypassRoles = getOrCreate("Promotion.BypassRoles", Lists.newArrayList(-1L), config);

        if (updateCommands) {
            config.put("UpdateCommands", false);
        }
        yaml.dump(config, Files.newBufferedWriter(configPath));
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
                getOrCreate(key, ConfigEmbedBuilder.getDefaultConfigEmbed(), config), key);
    }

    @Nullable
    private String checkEmpty(final String string) {
        return string == null ? null : string.isEmpty() ? null : string;
    }
}
