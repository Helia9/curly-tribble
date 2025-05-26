# Epimap

Epimap is a Fabric mod for Minecraft 1.21.3 that provides a real-time, interactive minimap and chunk analysis tool. Built with Kotlin and leveraging the Fabric API, Epimap helps players visualize the world around them, analyze chunk data, and interact with a custom map screen.

## Features

- **Interactive Minimap:**  
  View a live map of your surroundings, rendered with block colors and player orientation.
- **Chunk Data Analysis:**  
  Analyze and save chunk top-layer and height data for advanced visualization.
- **Optimized Rendering:**  
  Efficient chunk caching and color calculation for smooth performance.
- **Customizable keybinds**
  Customizable keybind for opening the map and panning the map with keyboard keys.

## Controls

- **Open Map:** Press `K` (default) to open the custom map screen.
- **Move Map View:** Use arrow keys (`↑`, `↓`, `←`, `→`) to pan the map or use the mouse `RMB` to drag around the map.
- **Zoom:** Scroll mouse wheel while on the map screen.

## Building

To build the mod from source:

```sh
./gradlew build
```

The compiled JAR will be in `build/libs/`.

## Project Structure

- `src/main/kotlin/mod/epimap/Epimap.kt` — Main mod initializer.
- `src/client/kotlin/mod/epimap/EpimapClient.kt` — Client-side initializer and event registration.
- `src/client/kotlin/mod/epimap/render.kt` — Custom map screen rendering logic.
- `src/client/kotlin/mod/epimap/keybindListener.kt` — Keybinding registration and handling.
- `src/client/kotlin/mod/epimap/analyzeChunk.kt` — Chunk analysis utilities.
- `src/client/kotlin/mod/epimap/chunkSaver.kt` — Chunk data saving utilities.
- `src/client/kotlin/mod/epimap/chunkLoader.kt` — Chunk data loading utilities.
- `src/client/kotlin/mod/epimap/getColor.kt` — Block color calculation.

## Configuration

- Keybindings can be changed in Minecraft's Controls menu.
- Map data is stored per world/server in the Fabric config directory.

## Credits

- Built with [Fabric API](https://fabricmc.net/) and [Fabric Language Kotlin](https://github.com/FabricMC/fabric-language-kotlin).
- JamesEpitech, Helia9, JurassicX