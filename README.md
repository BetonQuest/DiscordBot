# DiscordBot

A helper bot for the BetonQuest Community Discord.

Requirements can be found on
our [Notion page](https://betonquest.notion.site/BetonQuest-Discord-Bot-96d3fa5c28174494a8123005622be075).

# Technology

We use [JDA (Java Discord API)](https://github.com/DV8FromTheWorld/JDA) as the Backend API for this bot.

# Features
- The Bot adds a reaction to join messages
- Support Forums
  - Can be solved by either using `/solve` or adding a "Solved" Tag manually
  - Automatically closes solved Posts. The Timeout is configurable.

# Configuration
- The emoji added to join messages.
  - For Custom Emojis: `<:name:id>`
  - For animated Custom Emojis: `< a:name:id>`
  - For default Emojis: The Unicode representation, e.g. `U+1F44B`


- Support Forums:
  - which channels are affected
  - `/solve` command
    - a message as an embed, when a post is marked as solved
  - The Timeout after which a post is automatically closed
  - The Interval the bot checks for posts to be closed
  - Tags
    - a tag to mark solved posts
    - tags to keep when a post is solved
    - a order by which tags are sorted

- Promotion System:
  - Ranks:
    - A List of roles handled in the promotion system
    - Sorted from lowest (top) to highest (down)
  - a message as an embed, when a user is promoted. Supports placeholders: "%user%", "%newRole%", "%promoter%"
  - Bypass-Roles:
    - A List of roles that can circumvent all permission checks
  - Promotion Cooldown:
    - A cooldown in seconds, after which a user can be promoted again
