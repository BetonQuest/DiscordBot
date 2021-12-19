package org.betonquest.discordbot.config;

import com.google.common.cache.CacheBuilder;
import com.google.common.collect.Lists;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.Color;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * This class can create a {@link MessageEmbed} from the war configuration obtained by the {@link BetonBotConfig}.
 */
public class ConfigEmbedBuilder {
    /**
     * Logger instance.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(CacheBuilder.class);

    /**
     * The created {@link MessageEmbed} instance.
     */
    private final MessageEmbed embed;

    /**
     * Builds a {@link MessageEmbed} from a configuration {@link Map},
     * that can than be obtained with {@link ConfigEmbedBuilder#getEmbed()}.
     *
     * @param embedData the raw config part, that contains the {@link MessageEmbed} configuration
     * @param fullPath  The path, where the config part is located, to log better error messages
     */
    public ConfigEmbedBuilder(final Map<String, Object> embedData, final String fullPath) {
        final EmbedBuilder builder = new EmbedBuilder();

        final int color = (int) embedData.getOrDefault("Color", null);
        builder.setColor(color);

        final String title = (String) embedData.getOrDefault("Title", null);
        final String titleUrl = (String) embedData.getOrDefault("TitleUrl", null);
        builder.setTitle(title, titleUrl);

        final String author = (String) embedData.getOrDefault("Author", null);
        final String authorUrl = (String) embedData.getOrDefault("AuthorUrl", null);
        final String authorIconUrl = (String) embedData.getOrDefault("AuthorIconUrl", null);
        builder.setAuthor(author, authorUrl, authorIconUrl);

        final String imageUrl = (String) embedData.getOrDefault("ImageUrl", null);
        builder.setImage(imageUrl);

        final String thumbnailUrl = (String) embedData.getOrDefault("ThumbnailUrl", null);
        builder.setThumbnail(thumbnailUrl);

        final String description = getMultiLineString(embedData, "Description");
        builder.setDescription(description);

        @SuppressWarnings("unchecked") final List<Map<String, Object>> fields = (List<Map<String, Object>>) embedData.getOrDefault("Fields", null);
        if (fields != null) {
            for (final Map<String, Object> field : fields) {
                final String fieldName = (String) field.getOrDefault("Name", null);
                final String fieldValueString = getMultiLineString(field, "Value");
                final Boolean fieldInline = (Boolean) field.getOrDefault("Inline", null);
                if (fieldName == null && fieldValueString == null && fieldInline != null) {
                    builder.addBlankField(fieldInline);
                } else {
                    builder.addField(fieldName, fieldValueString, fieldInline != null && fieldInline);
                }
            }
        }

        final String footer = getMultiLineString(embedData, "Footer");
        final String footerIconUrl = (String) embedData.getOrDefault("FooterIconUrl", null);
        builder.setFooter(footer, footerIconUrl);

        if (builder.isEmpty()) {
            LOGGER.warn("Your embed message in the config at path '" + fullPath + "' is empty!");
        }
        if (!builder.isValidLength()) {
            LOGGER.warn("Your embed message in the config at path '" + fullPath + "' is too long!");
        }
        embed = builder.build();
    }

    /**
     * Create a {@link Map} that has a template for a {@link MessageEmbed},
     * that could be created with the {@link ConfigEmbedBuilder}.
     *
     * @return the template {@link Map}
     */
    public static Map<String, Object> getDefaultConfigEmbed() {
        final Map<String, Object> embed = new LinkedHashMap<>();
        embed.put("Color", Color.GREEN.getRGB());
        embed.put("Title", "Custom Title");
        embed.put("TitleUrl", "https://Custom.Title/URL");
        embed.put("Author", "Custom Author");
        embed.put("AuthorUrl", "https://Custom.Author/URL");
        embed.put("AuthorIconUrl", "https://Custom.Author/Icon/URL");
        embed.put("ImageUrl", "https://Custom.Image/URL");
        embed.put("ThumbnailUrl", "https://Custom.Thumbnail/URL");
        embed.put("Description", Lists.newArrayList("Custom Description"));

        final Map<String, Object> field1 = new LinkedHashMap<>();
        field1.put("Name", "Custom Field");
        field1.put("Value", Lists.newArrayList("Custom Field Value"));
        field1.put("Inline", false);
        final Map<String, Object> field2 = new LinkedHashMap<>();
        field2.put("Inline", false);
        final List<Map<String, Object>> fields = new ArrayList<>();
        fields.add(field1);
        fields.add(field2);
        embed.put("Fields", fields);

        embed.put("Footer", Lists.newArrayList("Custom Footer"));
        embed.put("FooterIconUrl", "https://Custom.Footer/Icon/URL");

        return embed;
    }

    @SuppressWarnings("unchecked")
    private String getMultiLineString(final Map<String, Object> embedData, final String key) {
        final List<String> fieldValue = (List<String>) embedData.getOrDefault(key, null);
        return fieldValue == null ? null : StringUtils.join(fieldValue, "\n");
    }

    public MessageEmbed getEmbed() {
        return embed;
    }
}
