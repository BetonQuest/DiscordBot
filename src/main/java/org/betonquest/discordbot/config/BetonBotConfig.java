package org.betonquest.discordbot.config;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class BetonBotConfig {
    private static final Logger logger = LogManager.getLogger();

    public final String token;
    public final String welcomeEmoji;

    public BetonBotConfig(final String configFile) throws IOException {
        final DumperOptions options = new DumperOptions();
        options.setIndent(4);
        options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);

        final Yaml yaml = new Yaml(options);
        final File file = new File(configFile);
        final Map<String, Object> config;
        if (file.exists()) {
            config = yaml.load(new FileInputStream(file));
        } else {
            config = new HashMap<>();
        }

        token = getOrCreate("Token", "", config);
        welcomeEmoji = getOrCreate("WelcomeEmoji", "U+1F44B", config);

        logger.error(file.getAbsolutePath());
        yaml.dump(config, new FileWriter(file));
    }

    @SuppressWarnings("unchecked")
    private <T> T getOrCreate(final String key, final T defaultValue, final Map<String, Object> config) {
        if (config.containsKey(key)) {
            final Object value = config.get(key);
            if (value != null) {
                try {
                    return (T) value;
                } catch (final Exception e) {
                    logger.warn("Could not cast Config Entry '" + key + "'. Use default one.", e);
                }
            }
        }
        config.put(key, defaultValue);
        return defaultValue;
    }
}
