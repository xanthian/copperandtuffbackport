# Copper & Tuff Backport — Forge / NeoForge 1.20.1

Backports the 1.21.1 copper and tuff blocks and the selected Copper Age additions: bars, chests, chains, lanterns, torches, nuggets and weathering lightning rods. Includes the nugget/ingot and name tag recipes. Armour, tools, copper golems and trial chambers are excluded.

All content is enabled. Existing block and item IDs are preserved. Copper nugget ingredients accept `forge:nuggets/copper`, `c:copper_nuggets` and `c:nuggets/copper`; this mod's nugget remains registered.

## Build and run

Use Java 25 to run the Gradle wrapper and install Java 17 for the compiler and Minecraft runtime. The project produces one Java 17 mod jar supporting both Forge and the legacy NeoForge release for Minecraft 1.20.1.

```sh
./gradlew build
./gradlew runClient
./gradlew runServer
```

Forge is the default development target. To build or run against NeoForge:

```sh
./gradlew -Ploader=neoforge build
./gradlew -Ploader=neoforge runClient
```

On Windows, use `gradlew.bat`. The installable jar is `build/libs/CopperandTuffBackport-Forge-NeoForge-1.3.0.jar`; the sources jar is for development only. Minecraft itself uses Java 17.

## Validation

```sh
./gradlew build runGameTestServer runClientSmoke
./gradlew -Ploader=neoforge build runGameTestServer runClientSmoke
```

The 28 GameTests cover registrations, recipes and unlocks, shared nugget ingredients, loot, creative tab ordering, copper interactions, lightning rods, doors, bulbs and chests. The client smoke check renders the added blocks and items. Test mods and rendering probes are excluded from the release jar. CI builds and runs the GameTests on both loaders on Linux and Windows.

The PowerShell scripts in `scripts/` additionally audit resources and recipes against locally supplied official Minecraft client jars. Their parameters identify which vanilla version each audit needs.

## Port details

Registration uses Forge registry events. Axe scraping and wax removal use Forge's tool modification event. The remaining mixins cover mob door interaction, chest sounds, vanilla lightning rod weathering, lightning interactions and waxing. The same source and metadata support Forge 47.4.29 and NeoForge 47.1.106.
