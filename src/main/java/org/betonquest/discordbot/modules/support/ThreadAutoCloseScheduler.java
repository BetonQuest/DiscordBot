package org.betonquest.discordbot.modules.support;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.channel.attribute.IThreadContainer;
import net.dv8tion.jda.api.entities.channel.concrete.ForumChannel;
import net.dv8tion.jda.api.events.session.ShutdownEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.betonquest.discordbot.config.BetonBotConfig;
import org.betonquest.discordbot.modules.ForumTagHolder;
import org.jetbrains.annotations.NotNull;

import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
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
     */
    public ThreadAutoCloseScheduler(final JDA api, final BetonBotConfig config) {
        super();
        this.executorService = Executors.newScheduledThreadPool(1);
        this.config = config;

        supportForums = config.supportChannelIDs.stream()
                .map(id -> config.getGuild().getChannelById(ForumChannel.class, id))
                .filter(Objects::nonNull)
                .toList();

        final int checkInterval = config.supportAutoCloseCheckInterval;
        executorService.scheduleAtFixedRate(this, checkInterval, checkInterval, TimeUnit.MINUTES);
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
        final OffsetDateTime timeout = OffsetDateTime.now().minus(config.supportAutoCloseTimeout, ChronoUnit.MINUTES);
        supportForums.stream()
                .map(IThreadContainer::getThreadChannels)
                .flatMap(Collection::stream)
                .filter(channel -> !channel.isArchived()
                        && ForumTagHolder.isSolved(channel.getAppliedTags(), config)
                        && channel.getTimeArchiveInfoLastModified().isBefore(timeout)
                ).forEach(channel -> channel.getManager().setArchived(true).queue());
    }
}
