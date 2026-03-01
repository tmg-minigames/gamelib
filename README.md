# GameLib

A comprehensive library for Minecraft plugin development, providing game management, player tracking, GUI systems, inventory utilities, and more. Designed to simplify and speed up the creation of advanced minigames and server features.

**Required by several [TMG minigames](https://github.com/orgs/tmg-minigames/repositories).**

## Usage

- GameLib is a library plugin. TMG's minigames will automatically use it if present.
- You do **not** need to configure GameLib directly.
- If a plugin requires GameLib, make sure it is present and up to date in your `plugins` folder.

## Features

- **GameManager**: Complete game lifecycle with prepare phase, grace period, timers, world borders, boss bars, and custom events
- **PlayerActions**: Player state management (health, hunger, XP, effects, inventories, advancements)
- **Compass Tracking**: Player tracking compasses with interactive GUI for target selection
- **Inventory System**: Kits, conditional item templates, item sets, and custom slot layouts
- **GUI System**: Interactive inventory GUIs with click events and automatic cleanup
- **Timer**: Countdown, loop, and delayed execution timers
- **Score**: Simplified scoreboard management
- **Chat**: Messaging, action bars, color codes, and formatting utilities
- **BlockFill**: Fill regions between two locations with materials
- **Logger**: Unified logging (info, warn, error)
- **Utilities**: Chunk force-loading, item creation, spectator mode helpers

## Notes

- GameLib is designed to be used as a dependency by other plugins. It does not provide gameplay features on its own.
- Make sure to keep GameLib updated if you use plugins that depend on it.
- For examples of plugins using GameLib, see [TMG minigames](https://github.com/orgs/tmg-minigames/repositories).

## Development

If you are a developer and want to use GameLib in your plugin, you can include it as a dependency in your build system (tutorial [here](https://github.com/tmg-minigames/gamelib/blob/main/IMPORT.md))

## Issues

If you find any issues or have suggestions for improvements, please report them on the [issues page](https://github.com/tmg-minigames/gamelib/issues).
