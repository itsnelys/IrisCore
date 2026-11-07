# IrisCore

IrisCore is the shared foundation of the Iris Minecraft suite. It provides the HUD, indicators, key bindings, themes, options, menus, chat and logging utilities, plus the player's compatible last-held-item back rendering.

Specialized features — recipes, catalogue, inventory search and tooltips — live in the companion project [IrisVision](https://github.com/itsnelys/IrisVision).

### Features

- Extensible Iris options hub designed for multiple modules.
- UI themes, HUD, armor, arrow and tool-durability indicators.
- Configurable key bindings in Minecraft controls.
- Optional diagnostic chat messages and structured logs.
- Previously held compatible item rendered on the player's back: weapons, tools, bow, crossbow, shield, trident and mace. It updates when the item leaves the inventory.
- Contextual back-item visibility: visually hidden while swimming, using an elytra, invisible, or in first person.
- Configure each visibility rule in `config/iris/options.json` with `hideBackItemsInFirstPerson`, `hideBackItemsWhileSwimming`, `hideBackItemsWhileFallFlying`, and `hideBackItemsWhenInvisible`.
- Additional options disabled by default: `hideBackItemsWhileCrouching`, `hideBackItemsWhileRiding`, `hideBackItemsInSpectator`, `hideBackItemsWhileSleeping`, and `hideBackItemsWithCape`.
- Modded items: add their ids to `backItemWhitelist` in `config/iris/options.json`, for example `["modid:greatsword", "modid:warhammer"]`.
- Safe migration of legacy `irisv` settings to `iris`.

### Development

From the repository root:

```powershell
.\gradlew.bat runClient
.\gradlew.bat test
.\gradlew.bat build
```

`runClient` runs IrisCore and loads IrisVision when the sibling `../IrisVision` repository is available. Development saves and settings remain in `run/`.

### Installation

Use Minecraft 1.21.1, NeoForge 21.1.248 and Java 21. Build the project and place the generated JAR in the instance's `mods` directory. Install IrisVision as well for recipes and tooltips.

## License

Copyright © 2026 itsnelys. See [LICENSE](LICENSE).
