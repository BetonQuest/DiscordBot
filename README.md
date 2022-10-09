# DiscordBot
A helper bot for the BetonQuest Community Discord.

Requirements can be found on our [Notion page](https://betonquest.notion.site/BetonQuest-Discord-Bot-96d3fa5c28174494a8123005622be075).

# Technology
We use [JDA (Java Discord API)](https://github.com/DV8FromTheWorld/JDA) as the Backend API for this bot.

# Features
- The Bot adds a reaction to join messages
- Support Forums
  - A "Unsolved" Tag is automatically added
  - Can be solved by either using `/solve` or adding a "Solved" Tag manually
  - Automatically closes solved Posts after an hour

# Configuration
- The emoji added to join messages.
  - For Custom Emojis: `<:name:id>`
  - For animated Custom Emojis: `< a:name:id>`
  - For default Emojis: The Unicode representation, e.g. `U+1F44B`


- Support Forums:
  - which channels are affected
  - `/solve` command
    - a message as an embed, when a post is marked as solved
  - Tags
    - a tag to mark solved posts
    - a tag to mark unsolved posts
    - tags to keep when a post is solved
    - a order by which tags are sorted
