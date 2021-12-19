package org.betonquest.discordbot.config;

import com.google.common.collect.Lists;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
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
     * The token to connect to discord.
     */
    public final String token;
    /**
     * The guild id for the target discord server.
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
     * The role, that can manage support threads.
     */
    public final List<Long> supportRoleIDs;
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
     * The {@link Guild} of the discord managed by this bot.
     */
    private Guild guild;

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
            config = new LinkedHashMap<>();
        }

        token = checkEmpty(getOrCreate("Token", "", config));
        guildID = getOrCreate("GuildID", -1L, config);
        updateCommands = getOrCreate("UpdateCommands", true, config);
        welcomeEmoji = checkEmpty(getOrCreate("WelcomeEmoji", "U+1F44B", config));
        supportRoleIDs = getOrCreate("Support.RoleIDs", Lists.newArrayList(-1L), config);
        supportChannelIDs = getOrCreate("Support.ChannelIDs", Lists.newArrayList(-1L), config);
        supportClosedEmoji = checkEmpty(getOrCreate("Support.ClosedEmoji", "U+2705", config));
        supportClosedEmbed = getOrCreateEmbed("Support.ClosedMessage", config);

        if (updateCommands) {
            config.put("UpdateCommands", false);
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
            subConfig = new LinkedHashMap<>();
            config.put(firstKey, subConfig);
        }
        return getOrCreate(restKey, defaultValue, subConfig);
    }

    private MessageEmbed getOrCreateEmbed(final String key, final Map<String, Object> config) {
        return new ConfigEmbedBuilder(
                getOrCreate(key, ConfigEmbedBuilder.getDefaultConfigEmbed(), config)
                , key).getEmbed();
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
        guild = api.getGuildById(guildID);
        if (guild == null) {
            LOGGER.warn("No guild with the id '" + guildID + "' was found!");
            return;
        }
        for (final Long supportChannelID : supportChannelIDs) {
            final TextChannel textChannel = guild.getTextChannelById(supportChannelID);
            if (textChannel == null) {
                LOGGER.warn("No text support channel with the id '" + supportChannelIDs + "' was found!");
            } else {
                supportChannels.add(textChannel);
                LOGGER.info("Added support channel :" + textChannel.getName());
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
     * Get the {@link Guild} of this bot.
     *
     * @return the {@link Guild}
     */
    public Guild getGuild() {
        return guild;
    }
}
