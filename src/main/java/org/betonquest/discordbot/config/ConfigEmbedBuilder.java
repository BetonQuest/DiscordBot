package org.betonquest.discordbot.config;

import com.google.common.cache.CacheBuilder;
import com.google.common.collect.Lists;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Triple;
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
     * All variables that should be parsed.
     */
    private final List<Variable> variables;
    /**
     * The embeds' color.
     */
    private final String color;
    /**
     * The embeds' title.
     */
    private final String title;
    /**
     * The embeds' titleUrl.
     */
    private final String titleUrl;
    /**
     * The embeds' author.
     */
    private final String author;
    /**
     * The embeds' authorUrl.
     */
    private final String authorUrl;
    /**
     * The embeds' authorIconUrl.
     */
    private final String authorIconUrl;
    /**
     * The embeds' imageUrl.
     */
    private final String imageUrl;
    /**
     * The embeds' thumbnailUrl.
     */
    private final String thumbnailUrl;
    /**
     * The embeds' description.
     */
    private final String description;
    /**
     * The embeds' fields.
     */
    private final List<Triple<String, String, String>> fields;
    /**
     * The embeds' footer.
     */
    private final String footer;
    /**
     * The embeds' footerIconUrl.
     */
    private final String footerIconUrl;

    /**
     * The path of this embed in the config. This is used for better error messages.
     */
    private final String fullPath;

    /**
     * Builds a {@link MessageEmbed} from a configuration {@link Map},
     * that can than be obtained with {@link ConfigEmbedBuilder#getEmbed()}.
     *
     * @param embedData the raw config part, that contains the {@link MessageEmbed} configuration
     * @param fullPath  The path, where the config part is located, to log better error messages
     */
    @SuppressWarnings("unchecked")
    public ConfigEmbedBuilder(final Map<String, Object> embedData, final String fullPath) {
        this.fullPath = fullPath;
        variables = new ArrayList<>();

        color = (String) embedData.getOrDefault("Color", null);
        title = (String) embedData.getOrDefault("Title", null);
        titleUrl = (String) embedData.getOrDefault("TitleUrl", null);
        author = (String) embedData.getOrDefault("Author", null);
        authorUrl = (String) embedData.getOrDefault("AuthorUrl", null);
        authorIconUrl = (String) embedData.getOrDefault("AuthorIconUrl", null);
        imageUrl = (String) embedData.getOrDefault("ImageUrl", null);
        thumbnailUrl = (String) embedData.getOrDefault("ThumbnailUrl", null);
        description = getMultiLineString(embedData, "Description");

        fields = new ArrayList<>();
        for (final Map<String, Object> field : (List<Map<String, Object>>) embedData.getOrDefault("Fields", new ArrayList<>())) {
            final String fieldName = (String) field.getOrDefault("Name", null);
            final String fieldValueString = getMultiLineString(field, null);
            final String fieldInline = (String) field.getOrDefault("Inline", null);
            fields.add(Triple.of(fieldName, fieldValueString, fieldInline));
        }

        footer = getMultiLineString(embedData, "Footer");
        footerIconUrl = (String) embedData.getOrDefault("FooterIconUrl", null);
    }

    private ConfigEmbedBuilder(final String fullPath, final List<Variable> variables, final String color,
                               final String title, final String titleUrl, final String author, final String authorUrl,
                               final String authorIconUrl, final String imageUrl, final String thumbnailUrl,
                               final String description, final List<Triple<String, String, String>> fields,
                               final String footer, final String footerIconUrl) {
        this.fullPath = fullPath;
        this.variables = new ArrayList<>();
        for (final Variable variable : variables) {
            this.variables.add(new Variable(variable.placeholder, variable.value));
        }
        this.color = color;
        this.title = title;
        this.titleUrl = titleUrl;
        this.author = author;
        this.authorUrl = authorUrl;
        this.authorIconUrl = authorIconUrl;
        this.imageUrl = imageUrl;
        this.thumbnailUrl = thumbnailUrl;
        this.description = description;
        this.fields = new ArrayList<>();
        for (final Triple<String, String, String> field : fields) {
            this.fields.add(Triple.of(field.getLeft(), field.getMiddle(), field.getRight()));
        }
        this.footer = footer;
        this.footerIconUrl = footerIconUrl;
    }

    /**
     * Create a {@link Map} that has a template for a {@link MessageEmbed},
     * that could be created with the {@link ConfigEmbedBuilder}.
     *
     * @return the template {@link Map}
     */
    public static Map<String, Object> getDefaultConfigEmbed() {
        final Map<String, Object> embed = new LinkedHashMap<>();
        embed.put("Color", String.valueOf(Color.GREEN.getRGB()));
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
        field1.put("Inline", "false");
        final Map<String, Object> field2 = new LinkedHashMap<>();
        field2.put("Inline", "false");
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

    /**
     * This method first copies the current {@link ConfigEmbedBuilder}.
     * Then it adds the variable, that is resolved on the method {@link ConfigEmbedBuilder#getEmbed()}.
     * The new created copy is than returned for chaining.
     *
     * @param placeholder the placeholder that represent the variable
     * @param value       the value of the placeholder
     * @return the {@link ConfigEmbedBuilder} with the variable
     */
    public ConfigEmbedBuilder variable(final String placeholder, final String value) {
        final ConfigEmbedBuilder clone = new ConfigEmbedBuilder(fullPath, variables, color, title, titleUrl, author,
                authorUrl, authorIconUrl, imageUrl, thumbnailUrl, description, fields, footer, footerIconUrl);
        clone.variables.add(new Variable(placeholder, value));
        return clone;
    }

    /**
     * Resolves all variables in all fields of an embed and creates it.
     *
     * @return the created {@link MessageEmbed}
     */
    public MessageEmbed getEmbed() {
        final EmbedBuilder embed = new EmbedBuilder();
        embed.setColor(Integer.parseInt(resolveVariables(color)));
        embed.setTitle(resolveVariables(title), resolveVariables(titleUrl));
        embed.setAuthor(resolveVariables(author), resolveVariables(authorUrl), resolveVariables(authorIconUrl));
        embed.setImage(resolveVariables(imageUrl));
        embed.setThumbnail(resolveVariables(thumbnailUrl));
        embed.setDescription(resolveVariables(description));
        for (final Triple<String, String, String> field : fields) {
            final String fieldName = resolveVariables(field.getLeft());
            final String fieldValueString = resolveVariables(field.getMiddle());
            final boolean fieldInline = Boolean.parseBoolean(resolveVariables(field.getRight()));
            if (fieldName == null && fieldValueString == null) {
                embed.addBlankField(fieldInline);
            } else {
                embed.addField(fieldName, fieldValueString, fieldInline);
            }
        }
        embed.setFooter(resolveVariables(footer), resolveVariables(footerIconUrl));

        if (embed.isEmpty()) {
            LOGGER.warn("Your embed message in the config at path '" + fullPath + "' is empty!");
        }
        if (!embed.isValidLength()) {
            LOGGER.warn("Your embed message in the config at path '" + fullPath + "' is too long!");
        }

        return embed.build();
    }

    /**
     * This resolves all variables in a message.
     *
     * @param input the input string
     * @return the replaced string
     */
    public String resolveVariables(final String input) {
        if (input == null) {
            return null;
        }
        String processed = input;
        for (final Variable variable : variables) {
            processed = variable.resolve(processed);
        }
        return processed;
    }

    /**
     * This is a variable that should be replaced in a given string.
     */
    private static class Variable {
        /**
         * The character that indicates a variable placeholder.
         * This character needs to be before and after the actual placeholder.
         */
        private static final String VARIABLE_INDICATOR = "%";

        /**
         * The placeholder to replace
         */
        private final String placeholder;
        /**
         * The value of the placeholder.
         */
        private final String value;

        /**
         * Create a new variable.
         *
         * @param placeholder the placeholder to replace
         * @param value       the value to replace
         */
        public Variable(final String placeholder, final String value) {
            this.placeholder = placeholder;
            this.value = value;
        }

        /**
         * Resolved the variable in a given String.
         *
         * @param input the input string
         * @return the replaces string
         */
        public String resolve(final String input) {
            return input.replaceAll(VARIABLE_INDICATOR + placeholder + VARIABLE_INDICATOR, value);
        }
    }
}
