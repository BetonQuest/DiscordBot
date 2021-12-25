# DiscordBot
A helper bot for the BetonQuest community Discord.

Requirements can be found on our [Notion page](https://betonquest.notion.site/BetonQuest-Discord-Bot-96d3fa5c28174494a8123005622be075).

# Technology
We use [JDA (Java Discord API)](https://github.com/DV8FromTheWorld/JDA) as the Backend API for this bot.

# Features
- Welcome message get a reaction from the bot
  - Con be configured with `WelcomeEmoji`
- `/close` command
  - can be configured:
    - which roles do have permissions
    - which channels are effected
    - an emoji to mark closed threads
    - a message as an embed, when a thread is closed
    - a `Support.SubscriptionRoleID` role that will be removed when the thread was
      - the person who needed help will not be removed, even if he has the role
- new support threads
  - an embed message, that will be sent in the thread
  - the `Support.SubscriptionRoleID` will be automatically invited with a ping
