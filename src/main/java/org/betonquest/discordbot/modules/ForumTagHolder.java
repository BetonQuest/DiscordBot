package org.betonquest.discordbot.modules;

import net.dv8tion.jda.api.entities.channel.concrete.ThreadChannel;
import net.dv8tion.jda.api.entities.channel.forums.ForumTag;
import net.dv8tion.jda.api.entities.channel.forums.ForumTagSnowflake;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Helper Class for managing forum tags.
 * The tags for the given {@link ThreadChannel} will be stored to this class.
 * It is then possible to add and remove tags, before they are applied to the channel again.
 */
public class ForumTagHolder {
    /**
     * The maximum allowed number of tags on a forum post.
     */
    private static final int MAX_TAGS_PER_POST = 5;
    /**
     * The {@link ThreadChannel} to manage tags for
     */
    private final ThreadChannel channel;
    /**
     * Tags saved as IDs
     */
    private final List<Long> tagIds;

    /**
     * Creates a new {@link ForumTagHolder} to manage the tags of a given {@link ThreadChannel}.
     *
     * @param channel The {@link ThreadChannel} to manage tags for
     */
    public ForumTagHolder(final ThreadChannel channel) {
        this.channel = channel;
        this.tagIds = channel.getAppliedTags()
                .stream()
                .map(ForumTag::getIdLong)
                .collect(Collectors.toList());
    }

    /**
     * Adds a tag.
     *
     * @param tagId The tags ID
     * @return this Object for chaining
     */
    public ForumTagHolder add(final Long tagId) {
        tagIds.add(tagId);
        return this;
    }

    /**
     * Removes a tag.
     *
     * @param tagId The tags ID to remove
     * @return this Object for chaining
     */
    public ForumTagHolder remove(final Long tagId) {
        tagIds.remove(tagId);
        return this;
    }

    /**
     * Removes all tags not contained in keep.
     *
     * @param keep the tag IDs to keep
     * @return this Object for chaining
     */
    public ForumTagHolder keepTags(final List<Long> keep) {
        final List<Long> removeIds = tagIds.stream()
                .filter(id -> !keep.contains(id))
                .toList();
        tagIds.removeAll(removeIds);
        return this;
    }

    /**
     * Applies the first five tags to the {@link ThreadChannel}.
     * Sorts the tags by a given order. Unspecified tags will be attached at the end of the list.
     * To disable sorting pass an empty {@link List}.
     * <p>
     * This is a terminal operation.
     *
     * @param sorting The sorting order, represented by TagIDs
     */
    public void apply(final List<Long> sorting) {
        final List<Long> tagIdsToApply;

        tagIdsToApply = new ArrayList<>(tagIds.size());
        for (final Long tagId : sorting) {
            if (tagIds.contains(tagId) && !tagIdsToApply.contains(tagId)) {
                tagIdsToApply.add(tagId);
            }
        }
        for (final Long tagId : tagIds) {
            if (!tagIdsToApply.contains(tagId)) {
                tagIdsToApply.add(tagId);
            }
        }

        List<ForumTagSnowflake> tagList = tagIdsToApply.stream()
                .map(ForumTagSnowflake::fromId)
                .toList();

        if (tagList.size() > MAX_TAGS_PER_POST) {
            tagList = tagList.subList(0, 5);
        }

        channel.getManager().setAppliedTags(tagList.toArray(new ForumTagSnowflake[0])).queue();
    }
}
