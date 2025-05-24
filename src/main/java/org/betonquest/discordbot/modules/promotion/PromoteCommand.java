package org.betonquest.discordbot.modules.promotion;

import java.util.*;

import org.betonquest.discordbot.config.*;
import org.slf4j.*;

import net.dv8tion.jda.api.*;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.interaction.command.*;
import net.dv8tion.jda.api.hooks.*;
import net.dv8tion.jda.api.interactions.commands.*;
import net.dv8tion.jda.api.interactions.commands.build.*;

/**
 * A `promote` command to promote users up in a ranking ladder
 */
public class PromoteCommand extends ListenerAdapter {
    /**
     * Logger instance.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(PromoteCommand.class);

    /**
     * The command name.
     */
    public static final String COMMAND = "promote";

    /**
     * The command option user.
     */
    public static final String USER_OPTION_NAME = "user";

    /**
     * The {@link BetonBotConfig} instance.
     */
    private final BetonBotConfig config;

    /**
     * Create a new `promote` command instance.
     *
     * @param api    The {@link JDA} instance
     * @param config The {@link BetonBotConfig} instance
     */
    public PromoteCommand(final JDA api, final BetonBotConfig config) {
        super();
        this.config = config;
        if (config.promotionRanks.isEmpty()) {
            LOGGER.warn("No support channels where found or set!");
            return;
        }
        if (config.promotionEmbed == null) {
            LOGGER.warn("No support closed message was found or set!");
        }

        if (config.updateCommands) {
            api.updateCommands().addCommands(
                    Commands.slash(COMMAND, "Promote a player up the ranking ladder.")
                            .setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.ADMINISTRATOR))
                            .addOption(OptionType.USER, USER_OPTION_NAME, "The User to promote", true)
            ).queue();
        }
        api.addEventListener(this);
    }

    @Override
    public void onSlashCommandInteraction(final SlashCommandInteractionEvent event) {
        if (!COMMAND.equals(event.getName())) {
            return;
        }

        final OptionMapping option = event.getOption(USER_OPTION_NAME);
        if (option == null) {
            event.reply("You need to specify a user to promote.").setEphemeral(true).queue();
            return;
        }

        final Member promotionTarget = option.getAsMember();
        if (promotionTarget == null) {
            event.reply("You need to specify a user to promote.").setEphemeral(true).queue();
            return;
        }

        final Member cmdExecutor = event.getInteraction().getMember();
        if (cmdExecutor == null) {
            LOGGER.error("The promote command was triggered without a member!");
            return;
        }

        final List<Role> executorRoles = cmdExecutor.getRoles();
        final boolean noBypassRole = executorRoles.stream().noneMatch(r ->
                config.promotionBypassRoles.contains(r.getIdLong()));

        final List<Role> promotionRolesOfExecutor = filterAndSortByPromotionLadder(executorRoles);
        if (noBypassRole && promotionRolesOfExecutor.isEmpty()) {
            event.reply("You need to have a promotable Rank yourself to use this command!")
                    .setEphemeral(true).queue();
            return;
        }

        final Long highestRoleOfExecutor = promotionRolesOfExecutor.getLast().getIdLong();
        if (noBypassRole && config.promotionRanks.getFirst().equals(highestRoleOfExecutor)) {
            event.reply("There are no roles below you in the Promotion Ladder. You cannot use this command.")
                    .setEphemeral(true).queue();
            return;
        }

        final List<Role> promotionRolesOfTarget = filterAndSortByPromotionLadder(promotionTarget.getRoles());

        final Long highestRoleIdOfTarget = promotionRolesOfTarget.isEmpty()
                ? -1
                : promotionRolesOfTarget.getLast().getIdLong();

        final int indexOfHighestExecutorRole = config.promotionRanks.indexOf(highestRoleOfExecutor);
        final int indexOfHighestTargetRole = config.promotionRanks.indexOf(highestRoleIdOfTarget);
        if (noBypassRole && indexOfHighestTargetRole >= indexOfHighestExecutorRole) {
            event.reply("The target user is already ranked higher or equally high ranked as you.\n" +
                            "You can only rank up to one role lower than yourself.")
                    .setEphemeral(true).queue();
            return;
        }

        final int indexOfNewRoleOfTarget = indexOfHighestTargetRole + 1;
        if (noBypassRole && indexOfNewRoleOfTarget >= indexOfHighestExecutorRole) {
            event.reply("You cannot promote other users up to your own rank.")
                    .setEphemeral(true).queue();
            return;
        }

        if (!noBypassRole && indexOfNewRoleOfTarget == config.promotionRanks.size()) {
            event.reply("The target user is already on the highest rank")
                    .setEphemeral(true).queue();
            return;
        }

        final Long newRoleIdOfTarget = config.promotionRanks.get(indexOfNewRoleOfTarget);
        addRoleToUser(event, newRoleIdOfTarget, promotionTarget, cmdExecutor);
    }

    private void addRoleToUser(final SlashCommandInteractionEvent event, final Long roleId,
                               final Member member, final Member cmdExecutor) {
        final Guild guild = member.getGuild();
        final Role newRole = guild.getRoleById(roleId);
        if (newRole == null) {
            LOGGER.error("The role id " + roleId + " does not exist in the Guild!");
            event.reply("The role id " + roleId + " does not exist in the Guild!").setEphemeral(true).queue();
            return;
        }
        LOGGER.info("Promoting Member %d to Role %d...".formatted(member.getIdLong(), roleId));
        guild.addRoleToMember(member, newRole).queue((nothing) -> {
            LOGGER.info("Successfully promoted Member %d to Role %d!".formatted(member.getIdLong(), roleId));
            final MessageEmbed embed = config.promotionEmbed
                    .variable("user", member.getAsMention())
                    .variable("newRole", newRole.getName())
                    .variable("promoter", cmdExecutor.getEffectiveName())
                    .getEmbed();
            event.replyEmbeds(embed).setEphemeral(false).queue();
        });
    }

    private List<Role> filterAndSortByPromotionLadder(final List<Role> roles) {
        return roles.stream()
                .filter(role -> config.promotionRanks.contains(role.getIdLong()))
                .sorted(Comparator.comparingInt(role -> config.promotionRanks.indexOf(role.getIdLong())))
                .toList();
    }
}
