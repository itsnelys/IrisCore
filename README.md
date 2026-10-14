# IrisCore

**Minecraft 1.21.1 · NeoForge 21.1.248 · Java 21**

[Français](#français) · [English](#english)

## Français

IrisCore est la base commune de la suite Iris pour Minecraft. Il fournit le HUD, les indicateurs, les raccourcis clavier, les thèmes, les options, les menus, les utilitaires de chat et de journalisation, ainsi que l'affichage du dernier outil compatible dans le dos du joueur.

Les fonctionnalités spécialisées — recettes, catalogue, recherche d'inventaire et tooltips — se trouvent dans le projet compagnon [IrisVision](https://github.com/itsnelys/IrisVision).

### Fonctionnalités

- Menu d'options Iris extensible pour accueillir plusieurs modules.
- Thèmes d'interface, HUD et indicateurs d'armure, de flèches et de durabilité.
- Raccourcis configurables dans les contrôles Minecraft.
- Messages de diagnostic facultatifs dans le chat et journaux structurés.
- Objet tenu précédemment rendu dans le dos : armes, outils, arc, arbalète, bouclier, trident et masse. L'affichage se met à jour quand l'objet est retiré de l'inventaire.
- Migration sûre des anciens réglages `irisv` vers `iris`.

### Développement

Depuis la racine du dépôt :

```powershell
.\gradlew.bat runClient
.\gradlew.bat test
.\gradlew.bat build
```

`runClient` lance IrisCore et charge IrisVision lorsque le dépôt frère `../IrisVision` est présent. Les sauvegardes et les options de développement restent dans `run/`.

### Installation

Utiliser Minecraft 1.21.1, NeoForge 21.1.248 et Java 21. Compiler le projet, puis placer le JAR généré dans le dossier `mods` de l'instance. Installer également IrisVision pour les recettes et les tooltips.

## English

IrisCore is the shared foundation of the Iris Minecraft suite. It provides the HUD, indicators, key bindings, themes, options, menus, chat and logging utilities, plus the player's compatible last-held-item back rendering.

Specialized features — recipes, catalogue, inventory search and tooltips — live in the companion project [IrisVision](https://github.com/itsnelys/IrisVision).

### Features

- Extensible Iris options hub designed for multiple modules.
- UI themes, HUD, armor, arrow and tool-durability indicators.
- Configurable key bindings in Minecraft controls.
- Optional diagnostic chat messages and structured logs.
- Previously held compatible item rendered on the player's back: weapons, tools, bow, crossbow, shield, trident and mace. It updates when the item leaves the inventory.
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
