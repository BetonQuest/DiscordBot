package org.betonquest.discordbot.config;

import com.google.common.collect.Lists;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.ISnowflake;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.TextChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * This is a configuration to load settings from a file.
 */
public class BetonBotConfig {
    /**
     * Logger instance.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(BetonBotConfig.class);

    /**
     * The token to connect to discord.
     */
    public final String token;
    /**
     * Should the commands be registered again.
     */
    public final boolean registerCommands;
    /**
     * The emoji to react on discords welcome message.
     */
    public final String welcomeEmoji;
    /**
     * The ids of the support channels.
     */
    public final List<Long> supportChannelIDs;
    /**
     * The Emoji to mark closed threads with.
     */
    public final String supportClosedEmoji;
    /**
     * The message to show, when a thread was marked as closed.
     */
    public final MessageEmbed supportClosedEmbed;
    /**
     * The closed supportChannelIDs.
     */
    private final List<TextChannel> supportChannels;

    /**
     * @param configFile the path of the config file
     * @throws IOException is thrown, when reading or writing the file coursed problems.
     */
    public BetonBotConfig(final String configFile) throws IOException {
        final DumperOptions options = new DumperOptions();
        options.setIndent(4);
        options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);

        final Yaml yaml = new Yaml(options);
        final Path configPath = Paths.get(configFile);
        final Map<String, Object> config;

        if (Files.exists(configPath)) {
            config = yaml.load(Files.newInputStream(configPath));
        } else {
            config = new HashMap<>();
        }

        token = checkEmpty(getOrCreate("Token", "", config));
        registerCommands = getOrCreate("RegisterCommands", true, config);
        welcomeEmoji = checkEmpty(getOrCreate("WelcomeEmoji", "U+1F44B", config));
        supportChannelIDs = getOrCreate("Support.ChannelIDs", Lists.newArrayList(-1L), config);
        supportClosedEmoji = checkEmpty(getOrCreate("Support.ClosedEmoji", "U+2705", config));
        supportClosedEmbed = new ConfigEmbedBuilder(
                getOrCreate("Support.ClosedMessage", ConfigEmbedBuilder.getDefaultConfigEmbed(), config)
                , "Support.ClosedMessage").getEmbed();

        if (registerCommands) {
            config.put("RegisterCommands", false);
        }
        supportChannels = new ArrayList<>();
        yaml.dump(config, Files.newBufferedWriter(configPath));
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
                        LOGGER.warn("Could not cast Config Entry '" + key + "'. Use default one.", e);
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
            subConfig = new HashMap<>();
            config.put(firstKey, subConfig);
        }
        return getOrCreate(restKey, defaultValue, subConfig);
    }

    private String checkEmpty(final String string) {
        return string == null ? null : string.isEmpty() ? null : string;
    }

    /**
     * Init things that need a {@link JDA} instance
     *
     * @param api the {@link JDA} instance
     */
    public void init(final JDA api) {
        for (final Long supportChannelID : supportChannelIDs) {
            final TextChannel textChannel = api.getTextChannelById(supportChannelID);
            if (supportChannels == null) {
                LOGGER.warn("No text support channel with the id '" + supportChannelIDs + "' was found!");
            } else {
                supportChannels.add(textChannel);
            }
        }
    }

    /**
     * Get the list of all support channels.
     *
     * @return list of channels
     */
    public List<TextChannel> getSupportChannels() {
        return new ArrayList<>(Collections.unmodifiableList(supportChannels));
    }

    /**
     * Get the list of IDs of all support channels.
     *
     * @return list of channel IDs
     */
    public List<Long> getSupportChannelsIDs() {
        return supportChannels.stream().map(ISnowflake::getIdLong).collect(Collectors.toUnmodifiableList());
    }
}
