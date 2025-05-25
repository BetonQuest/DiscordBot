package org.betonquest.discordbot.modules.promotion;

import net.dv8tion.jda.api.entities.Member;
import org.betonquest.discordbot.config.BetonBotConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * A cache to manage user promotions with a cooldown mechanism.
 * It stores the last promotion time for each user and checks if they can be promoted again.
 */
public class PromotionCache {
    /**
     * Logger instance.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(PromotionCache.class);

    /**
     * The YAML parser instance used for reading and writing the cache.
     */
    private final Yaml yaml;

    /**
     * The path to the cache file where promotion data is stored.
     */
    private final Path cachePath;

    /**
     * A map that caches the last promotion time for each user.
     */
    private final Map<Long, Long> promotions;

    /**
     * The cooldown period in seconds before a user can be promoted again.
     */
    private final int promotionCooldown;

    /**
     * Creates a new PromotionCache instance.
     *
     * @param cachePath the path to the cache file
     * @param config    the BetonBotConfig instance containing the promotion cooldown
     * @throws IOException if an I/O error occurs while reading or writing the cache file
     */
    public PromotionCache(final Path cachePath, final BetonBotConfig config) throws IOException {
        this.cachePath = cachePath;
        this.yaml = getYaml();
        this.promotions = new LinkedHashMap<>();
        for (final Map.Entry<Object, Object> entry : getConfig(yaml, cachePath).entrySet()) {
            if (entry.getKey() instanceof final Number user && entry.getValue() instanceof final Number time) {
                promotions.put(user.longValue(), time.longValue());
            } else {
                LOGGER.warn("Invalid entry in promotion cache: {} -> {}", entry.getKey(), entry.getValue());
            }
        }
        this.promotionCooldown = config.promotionCooldown;
    }

    /**
     * Checks if a user is promotable and updates the cache if so.
     *
     * @param member the member to check
     * @return true if the user can be promoted, false otherwise
     */
    public boolean isPromotable(final Member member) {
        final long userID = member.getIdLong();
        final long currentTime = Instant.now().getEpochSecond();
        final Long lastTime = promotions.getOrDefault(userID, 0L);
        if (lastTime + promotionCooldown < currentTime) {
            promotions.put(userID, currentTime);
            try {
                yaml.dump(promotions, Files.newBufferedWriter(cachePath));
            } catch (final IOException e) {
                LOGGER.warn("Error while writing promotion cache.", e);
            }
            return true;
        } else {
            return false;
        }
    }

    /**
     * Gets the time stamp of the next possible promotion for a user.
     *
     * @param member the member to check
     * @return the time in seconds until the next promotion can occur
     */
    public long getTimeOfNextPromotion(final Member member) {
        final long userID = member.getIdLong();
        final Long lastTime = promotions.getOrDefault(userID, 0L);
        if (lastTime == 0) {
            return Instant.now().getEpochSecond();
        }
        return lastTime + promotionCooldown;
    }

    private Yaml getYaml() {
        final DumperOptions options = new DumperOptions();
        options.setIndent(4);
        options.setIndicatorIndent(2);
        options.setWidth(120);
        options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
        return new Yaml(options);
    }

    private Map<Object, Object> getConfig(final Yaml yaml, final Path cachePath) throws IOException {
        if (Files.exists(cachePath)) {
            return yaml.load(Files.newInputStream(cachePath));
        } else {
            return new LinkedHashMap<>();
        }
    }
}
