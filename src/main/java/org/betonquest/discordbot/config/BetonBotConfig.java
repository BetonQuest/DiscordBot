package org.betonquest.discordbot.config;

import net.dv8tion.jda.api.JDA;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
     * The emoji to react on discords welcome message.
     */
    public final String welcomeEmoji;
    /**
     * The id of the support channel
     */
    public final Long supportChannelID;
    /**
     * The Emoji to mark solved threads with
     */
    public final String supportSolvedEmoji;

    public final List<String> supportSolvedMessage;
    /**
     * The resolved supportChannelID
     */
    private TextChannel supportChannel;

    /**
     * @param configFile the path of the config file
     * @throws IOException is thrown, when reading or writing the file coursed problems.
     */
    public BetonBotConfig(final String configFile) throws IOException {
        final DumperOptions options = new DumperOptions();
        options.setIndent(2);
        options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);

        final Yaml yaml = new Yaml(options);
        final Path configPath = Paths.get(configFile);
        final Map<String, Object> config;

        if (Files.exists(configPath)) {
            config = yaml.load(Files.newInputStream(configPath));
        } else {
            config = new HashMap<>();
        }

        token = getOrCreate("Token", "", config);
        welcomeEmoji = getOrCreate("WelcomeEmoji", "U+1F44B", config);
        supportChannelID = getOrCreate("Support.ChannelID", -1L, config);
        supportSolvedEmoji = getOrCreate("Support.SolvedEmoji", "U+2705", config);
        final List<String> defaultMessage = new ArrayList<>();
        defaultMessage.add("This ticket was marked as solved.");
        defaultMessage.add("Please archive the thread if there are no additional questions, otherwise ping the responsible person(s).");
        supportSolvedMessage = getOrCreate("Support.SolvedMessage", defaultMessage, config);

        yaml.dump(config, Files.newBufferedWriter(configPath));
    }

    @SuppressWarnings({"unchecked", "PMD.AvoidCatchingGenericException"})
    private <T> T getOrCreate(final String key, final T defaultValue, final Map<String, Object> config) {
        final int splitIndex = key.indexOf('.');
        final String firstKey = splitIndex == -1 ? key : key.substring(0, splitIndex);
        final String restkey = splitIndex == -1 ? null : key.substring(splitIndex + 1);
        if (restkey == null) {
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
        final Map<String, Object> subConfig;
        if (config.containsKey(firstKey)) {
            subConfig = (Map<String, Object>) config.get(firstKey);
        } else {
            subConfig = new HashMap<>();
            config.put(firstKey, subConfig);
        }
        return getOrCreate(restkey, defaultValue, subConfig);
    }

    /**
     * Init things that need a {@link JDA} instance
     *
     * @param api the {@link JDA} instance
     */
    public void init(final JDA api) {
        supportChannel = api.getTextChannelById(supportChannelID);
        if (supportChannel == null) {
            LOGGER.warn("No text support channel with the id '" + supportChannelID + "' was found!");
        }
    }

    public TextChannel getSupportChannel() {
        return supportChannel;
    }
}
