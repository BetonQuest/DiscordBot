package org.betonquest.discordbot.modules.support;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.attribute.IThreadContainer;
import net.dv8tion.jda.api.entities.channel.concrete.ForumChannel;
import net.dv8tion.jda.api.entities.channel.concrete.ThreadChannel;
import net.dv8tion.jda.api.events.session.ShutdownEvent;
import net.dv8tion.jda.api.exceptions.ErrorResponseException;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.betonquest.discordbot.config.BetonBotConfig;
import org.betonquest.discordbot.modules.ForumTagHolder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.OffsetDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * This Class will check for inactive Support Posts and close them after a configured delay.
 */
@SuppressWarnings("PMD.DoNotUseThreads")
public class ThreadAutoCloseScheduler extends ListenerAdapter implements Runnable {

    /**
     * The Scheduler used to periodically run this process.
     */
    private final ScheduledExecutorService executorService;

    /**
     * The {@link BetonBotConfig} instance.
     */
    private final BetonBotConfig config;

    /**
     * The {@link ForumChannel}s in which Posts should be automatically closed.
     */
    private final List<ForumChannel> supportForums;

    /**
     * Creates and starts a new {@link ThreadAutoCloseScheduler}
     *
     * @param api    the {@link JDA} instance
     * @param config the {@link BetonBotConfig} instance
     * @param guild  the {@link Guild} in which the support channels are located
     */
    public ThreadAutoCloseScheduler(final JDA api, final BetonBotConfig config, final Guild guild) {
        super();
        this.executorService = Executors.newScheduledThreadPool(1);
        this.config = config;

        supportForums = config.supportChannelIDs.stream()
                .map(id -> guild.getChannelById(ForumChannel.class, id))
                .filter(Objects::nonNull)
                .toList();

        final int checkInterval = config.supportAutoCloseCheckInterval;
        executorService.scheduleAtFixedRate(this, 0, checkInterval, TimeUnit.MINUTES);
        api.addEventListener(this);
    }

    @Override
    public void onShutdown(@NotNull final ShutdownEvent event) {
        executorService.shutdown();
    }

    /**
     * When an object implementing interface {@code Runnable} is used
     * to create a thread, starting the thread causes the object's
     * {@code run} method to be called in that separately executing
     * thread.
     * <p>
     * The general contract of the method {@code run} is that it may
     * take any action whatsoever.
     *
     * @see Thread#run()
     */
    @Override
    public void run() {
        final OffsetDateTime timeout = OffsetDateTime.now().minusMinutes(config.supportAutoCloseTimeout);
        supportForums.stream()
                .map(IThreadContainer::getThreadChannels)
                .flatMap(Collection::stream)
                .filter(channel -> !channel.isArchived()
                        && ForumTagHolder.isSolved(channel.getAppliedTags(), config)
                        && isLastForeignMessageTimedOut(channel, timeout)
                ).forEach(channel -> channel.getManager().setArchived(true).queue());
    }

    /**
     * Gets the last message not send by the bot itself.
     * Then it checks if the message was sent before the given timeout.
     *
     * @param channel the {@link ThreadChannel} to check
     * @param timeout the timeout
     * @return true if the last foreign message was sent before the timeout
     */
    private boolean isLastForeignMessageTimedOut(final ThreadChannel channel, final OffsetDateTime timeout) {
        try {
            final Message lastMessage = channel.retrieveMessageById(channel.getLatestMessageId()).complete();
            final Message lastForeignMessage = getLastForeignMessage(channel, lastMessage);
            return Objects.isNull(lastForeignMessage) || lastForeignMessage.getTimeCreated().isBefore(timeout);
        } catch (final ErrorResponseException e) {
            return true;
        }
    }

    /**
     * Checks if the given message was sent from the bot itself.
     * If this is the case the function will recursively call itself until it receives a foreign message.
     *
     * @param channel the channel whose messages will be loaded
     * @param message the last message
     * @return the last message not sent from the bot itself
     */
    @Nullable
    private Message getLastForeignMessage(final ThreadChannel channel, @Nullable final Message message) {
        if (Objects.isNull(message)) {
            return null;
        }

        if (!message.getAuthor().equals(channel.getJDA().getSelfUser())) {
            return message;
        }

        final Message messageBefore = channel.getHistoryBefore(message, 1)
                .complete()
                .getRetrievedHistory()
                .stream()
                .findAny()
                .orElse(null);
        return getLastForeignMessage(channel, messageBefore);
    }
}
