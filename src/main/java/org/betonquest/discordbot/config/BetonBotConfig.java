package org.betonquest.discordbot.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
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

        token = getOrCreate("Token", "", config);
        welcomeEmoji = getOrCreate("WelcomeEmoji", "U+1F44B", config);

        yaml.dump(config, Files.newBufferedWriter(configPath));
    }

    @SuppressWarnings({"unchecked", "PMD.AvoidCatchingGenericException"})
    private <T> T getOrCreate(final String key, final T defaultValue, final Map<String, Object> config) {
        if (config.containsKey(key)) {
            final Object value = config.get(key);
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
}
