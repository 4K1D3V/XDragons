# XDragons

**XDragons** is a Minecraft Spigot/Paper plugin for creating and managing custom Ender Dragons with configurable behaviors, waypoints, abilities, phases, and bossbars. Designed for Minecraft 1.21, it allows server administrators to spawn dragons with unique patrol routes, dynamic abilities (fireballs, thunderbolts, rush, melee), and health-based phases, all controlled via commands, a GUI, and configuration files.

## Features

- **Custom Dragons**: Create dragons with unique IDs, names, health, and bossbar settings.
- **Waypoints**: Define patrol routes for dragons to follow, looping through locations.
- **Abilities**:
    - Fireballs: Explosive, incendiary projectiles targeting players.
    - Thunderbolts: Lightning strikes on players.
    - Rush: Teleport closer to players for sudden approaches.
    - Melee Attack: Configurable damage when near players.
- **Phases**: Change abilities based on health thresholds (e.g., more aggressive at low health).
- **Bossbar**: Displays dragon name and health to nearby players, toggleable.
- **GUI**: "Dragon Editor" for managing waypoints (add/remove).
- **Configurable**: Customize movement speed, ability chances/ranges, and phases in `dragons.yml`.
- **Permissions**: Restrict commands with `xdragons.spawn` and `xdragons.admin`.
- **Performance**: Optimized with global tasks to minimize TPS impact.

## Installation

1. **Prerequisites**:
    - Minecraft server running Spigot or Paper 1.21.
    - Java 17 or higher.

2. **Download**:
    - Grab the latest `XDragons-1.0.0.jar` from [Releases](https://github.com/4K1D3V/XDragons/releases).

3. **Deploy**:
    - Place `XDragons-1.0.0.jar` in your server’s `plugins/` folder.
    - Start or restart the server to generate `config.yml` and `dragons.yml` in `plugins/XDragons/`.

4. **Verify**:
    - Check `latest.log` for `[XDragons] v1.0.0 Enabled!`.
    - Run `/xdragons` in-game to confirm the plugin is loaded.

## Usage

### Commands
All commands require player execution. Use `/xdragons` or alias `/xd`.

| Command | Description | Permission |
|---------|-------------|------------|
| `/xdragons create <ID>` | Creates a dragon with the given numeric ID. | `xdragons.admin` |
| `/xdragons edit <ID> <property> <value>` | Edits dragon properties (name, maxhealth, bossbar, waypoints). | `xdragons.admin` |
| `/xdragons delete <ID>` | Deletes a dragon and its data. | `xdragons.admin` |
| `/xdragons spawn <ID>` | Spawns a dragon at your location. | `xdragons.spawn` |
| `/xdragons gui <ID>` | Opens the "Dragon Editor" GUI for waypoint management. | `xdragons.admin` |
| `/xdragons select <ID>` | Selects a dragon for GUI interactions. | `xdragons.admin` |
| `/xdragons reload` | Reloads `config.yml` and `dragons.yml`. | `xdragons.admin` |

**Examples**:
- Create a dragon: `/xdragons create 1`
- Set name: `/xdragons edit 1 name FireDragon`
- Add waypoint: `/xdragons edit 1 waypoints add`
- Spawn: `/xdragons spawn 1`
- Open GUI: `/xdragons select 1` then `/xdragons gui 1`

### GUI
- **Access**: Run `/xdragons select <ID>` then `/xdragons gui <ID>`.
- **Features**:
    - **Set Waypoint** (Emerald): Adds your current location as a waypoint.
    - **Remove Last Waypoint** (Redstone): Deletes the most recent waypoint.
- **Usage**: Click items to manage waypoints, changes save instantly to `dragons.yml`.

## Configuration

### `config.yml`
Defines the plugin’s prefix for messages.

```yaml
prefix: "&4&l[XDragons] &f"
```

### `dragons.yml`
Stores dragon data, including waypoints, abilities, phases, and movement settings.

```yaml
dragons:
  1:
    name: "Legendary Dragon"
    max-health: 200
    show-bossbar: true
    waypoints:
      - "world,100,2,100"
      - "world,200,2,200"
      - "world,150,2,150"
      - "world,100,2,100"
    movement:
      speed: 0.8
      threshold: 3.0
      update-interval: 10
    attack:
      chance: 0.03
      range: 5.0
      damage: 4.0
    abilities:
      fireball:
        chance: 0.05
        range: 30.0
      thunderbolt:
        chance: 0.03
        range: 20.0
      rush:
        chance: 0.03
        range: 20.0
        distance: 10.0
    phases:
      high:
        health: 150
        abilities:
          fireball:
            chance: 0.03
            range: 25.0
          thunderbolt:
            chance: 0.02
            range: 15.0
          rush:
            chance: 0.02
            range: 15.0
            distance: 8.0
        attack:
          chance: 0.02
          range: 4.0
          damage: 3.0
      low:
        health: 50
        abilities:
          fireball:
            chance: 0.07
            range: 35.0
          thunderbolt:
            chance: 0.05
            range: 25.0
          rush:
            chance: 0.05
            range: 25.0
            distance: 12.0
        attack:
          chance: 0.05
          range: 6.0
          damage: 5.0
```

- **Waypoints**: List of `world,x,y,z` coordinates for dragon patrol.
- **Movement**:
    - `speed`: Blocks per tick (default: 0.8).
    - `threshold`: Distance to switch waypoints (default: 3.0).
    - `update-interval`: Ticks between movement updates (default: 10, 0.5s).
- **Attack**: Melee attack settings (chance, range, damage).
- **Abilities**: Configurable chances and ranges for fireball, thunderbolt, rush.
- **Phases**: Health-based behavior changes (e.g., more aggressive at low health).

## Permissions

| Permission | Description | Default |
|------------|-------------|---------|
| `xdragons.spawn` | Allows spawning dragons with `/xdragons spawn`. | op |
| `xdragons.admin` | Allows creating, editing, deleting, GUI access, and reloading. | op |

Use a permissions plugin (e.g., LuckPerms) to assign to players or groups.

## Building from Source

1. **Clone Repository**:
   ```bash
   git clone https://github.com/4K1D3V/XDragons.git
   cd XDragons
   ```

2. **Dependencies**:
    - Spigot 1.21 API (included in `pom.xml`).
    - Maven 3.6+.

3. **Compile**:
   ```bash
   mvn clean package
   ```
   Output: `target/XDragons-1.0.0.jar`.

4. **Deploy**:
    - Copy `target/XDragons-1.0.0.jar` to `plugins/`.
    - Ensure `config.yml` and `dragons.yml` are in `plugins/XDragons/`.

## Contributing

We welcome contributions! To contribute:

1. Fork the repository.
2. Create a feature branch (`git checkout -b feature/YourFeature`).
3. Commit changes (`git commit -m "Add YourFeature"`).
4. Push to your fork (`git push origin feature/YourFeature`).
5. Open a Pull Request.

Please:
- Follow the existing code style.
- Test changes on a 1.21 server.
- Report issues at [Issues](https://github.com/4K1D3V/XDragons/issues).

## Issues

Encounter a bug or have a suggestion? Create an issue at [Issues](https://github.com/4K1D3V/XDragons/issues) with:
- Plugin version (1.0.0).
- Server version (Spigot/Paper 1.21).
- Steps to reproduce.
- `latest.log` snippet if applicable.

## License

This project is licensed under the MIT License. See [LICENSE](LICENSE) for details.

## Credits

- **Authors**:
    - [KiteGG](https://github.com/4K1D3V)
    - [Enttbot](https://github.com/EnttbotX)