# Custom Recipe Loader Plugin

[![Version](https://img.shields.io/badge/version-1.2.0-blue.svg)](https://github.com/KreativeName1/Custom-Recipes-MC-Paper/releases)
[![Minecraft](https://img.shields.io/badge/minecraft-1.21.5+-green.svg)](https://minecraft.net)
[![License](https://img.shields.io/badge/license-MIT-yellow.svg)](LICENSE)

A powerful and flexible Minecraft plugin that enables server administrators to create and manage custom recipes directly in-game. Add unique crafting experiences to your server without touching a single line of code!

## ✨ Features

### 🔧 Recipe Types
- **Shaped Crafting** - Traditional crafting table recipes with specific patterns
- **Shapeless Crafting** - Mix ingredients in any arrangement
- **Cooking Recipes** - Custom furnace, smoker, blast furnace, and campfire recipes
- **Stonecutting** - Precision cutting recipes for the stonecutter

### 🎮 Easy Management
- **In-Game Commands** - No need to edit files manually
- **Live Reloading** - Apply changes without server restarts
- **Persistent Storage** - All recipes saved automatically
- **Permission System** - Control who can manage recipes

## 📦 Requirements
- **Minecraft Server**: Paper 1.21.5
- **Java**: Java 21 or higher
- **Permissions Plugin**: Optional but recommended for permission management
- **Server Access**: Ability to upload plugins to your server's plugins directory
- **Storage**: Minimal disk space (plugin is lightweight)

## Missing Features/Limitations/Bugs
- **Villager Trading**: Not implemented yet
- **Loading**: Reloading recipes may cause lag during the process
- **Custom items**: Currently only supports vanilla items and blocks
- **Recipe Editing**: Recipes can be added or removed, but not edited directly - you must remove and recreate
- **Support**: Currently only for Minecraft 1.21.5
- **Commands**: Output of Commands could be improved with better error handling and user feedback

## 🚀 Quick Start

### Installation

1. **Download** the latest release from the [releases page](https://github.com/KreativeName1/Custom-Recipes-MC-Paper/releases)
2. **Place** the JAR file in your server's `plugins/` directory
3. **Restart** your server

### Basic Usage

```bash
# Add a simple shapeless recipe
/cr add shapeless DIAMOND 1 COAL_BLOCK 9

# List all custom recipes
/cr list

# Remove a recipe by index
/cr remove 1
```

## 📖 Complete Command Reference

All commands use `/customrecipe` or the shorter alias `/cr`.

### 🔷 Shaped Recipes

Create recipes that require a specific crafting pattern.

**Command Format:**
```bash
/cr add shaped <result> <count> <pattern> <char> <ingredients> [<char> <ingredients>...]
```

**Parameters:**
- `result` - Item to craft (e.g., `DIAMOND_SWORD`)
- `count` - Quantity produced (1-64)
- `pattern` - 9-character grid pattern, use underscores to represent whitespaces (e.g., `"ABC DEF G__"`), Row 1 -> Top, Row 2 -> Middle, Row 3 -> Bottom
- `char` - Pattern character (A-Z)
- `ingredients` - Materials for that character (comma-separated for multiple options)

**Examples:**

```bash
# Create a name tag recipe
/cr add shaped NAME_TAG 1 __S _PS P__ S STRING P PAPER

# Custom netherite upgrade recipe
/cr add shaped NETHERITE_PICKAXE 1 NNN _D_ _S_ N NETHERITE_SCRAP D DIAMOND_PICKAXE S STICK

# Flexible Recipe (multiple ingredient options)
/cr add shaped BREAD 4 WWW W WHEAT,POTATO,CARROT
```

### 🔶 Shapeless Recipes

Create recipes where ingredient order doesn't matter.

**Command Format:**
```bash
/cr add shapeless <result> <count> <ingredient> <amount> [<ingredient> <amount>...]
```

**Examples:**

```bash
# Convert coal blocks to diamonds
/cr add shapeless DIAMOND 1 COAL_BLOCK 2 GOLD_INGOT 1

# Recycle end rods into glowstone
/cr add shapeless GLOWSTONE_DUST 3 END_ROD 1

# Multiple input options
/cr add shapeless GUNPOWDER 4 COAL,CHARCOAL 2 BONE_MEAL 1
```

### 🔥 Cooking Recipes

Add custom smelting, smoking, blasting, and campfire recipes.

**Command Format:**
```bash
/cr add cooking <result> <count> <experience> <types> <time> <ingredients>
```

**Parameters:**
- `types` - Cooking methods: `furnace`, `smoking`, `blasting`, `campfire` (comma-separated)
- `time` - Cooking time in ticks (20 ticks = 1 second)
- `experience` - XP reward (decimal allowed)

**Examples:**

```bash
# Create amethyst shards from calcite or tuff
/cr add cooking AMETHYST_SHARD 2 1.5 furnace,blasting 200,100 CALCITE,TUFF

# Custom food recipe
/cr add cooking COOKED_BEEF 2 1.5 smoking,campfire 300 ROTTEN_FLESH,SPIDER_EYE

# Efficient ore processing
/cr add cooking IRON_INGOT 2 0.7 blasting 80 IRON_ORE
```

### ⚒️ Stonecutting Recipes

Create precise cutting recipes for the stonecutter.

**Command Format:**
```bash
/cr add stonecutting <result> <count> <ingredient>
```

**Examples:**

```bash
# Create chains from iron ingots
/cr add stonecutting CHAIN 4 IRON_INGOT

# Make glass panes more efficiently
/cr add stonecutting GLASS_PANE 16 GLASS

# Convert amethyst blocks back to shards
/cr add stonecutting AMETHYST_SHARD 4 AMETHYST_BLOCK
```

### 🔧 Recipe Management

```bash
# List all recipes with indices
/cr list

# Remove recipe by index number
/cr remove <index>

# Reload all recipes from file
/cr reload

# Get help
/cr help
```

## 🔐 Permissions

Configure these permissions in your permission plugin:

| Permission | Description | Default |
|------------|-------------|---------|
| `recipes.add` | Add new recipes | OP |
| `recipes.remove` | Remove existing recipes | OP |
| `recipes.list` | View recipe list | OP |
| `recipes.reload` | Reload recipe file | OP |
| `recipes.*` | All recipe permissions | OP |

### Recipe File Format

The `recipes.json` file stores all custom recipes in a structured format:

```json
[
  {
    "type": "ShapedRecipe",
    "key": "recipe_shaped_name_tag_d3987500dd4e4f719a2ec4eaaba1ac19",
    "result": {
      "item": "NAME_TAG",
      "count": 1
    },
    "pattern": [
      "  S",
      " PS", 
      "P  "
    ],
    "replace": {
      "S": ["STRING"],
      "P": ["PAPER"]
    }
  },
  {
    "type": "ShapelessRecipe",
    "key": "recipe_shapeless_redstone_2b3f4a1c8e9d4f0b8a1c2d3e4f5a6b7c",
    "result": {
      "item": "REDSTONE",
      "count": 4
    },
    "ingredients": [
      {
        "choices": ["NETHER_WART"],
        "count": 1
      },
      {
        "choices": ["GLOWSTONE_DUST"],
        "count": 1
      }
    ]
  },
  {
    "type": "StonecuttingRecipe",
    "key": "recipe_stonecutting_chain_596b39ae483c49ab93b76e5ae0a867e9",
    "result": {
      "item": "CHAIN",
      "count": 4
    },
    "ingredient": {
      "item": "IRON_INGOT"
    }
  },
  {
    "type": "CookingRecipe",
    "key": "recipe_cooking_amethyst_shard_3c4d5e6f7a8b9c0d1e2f3a4b5c6d7e8f",
    "cookingTypes": [
      {
        "type": "BlastingRecipe",
        "cooking_time": 100
      },
      {
        "type": "FurnaceRecipe",
        "cooking_time": 200
      }
    ],
    "result": {
      "item": "AMETHYST_SHARD",
      "count": 2
    },
    "ingredient": [
      "CALCITE",
      "TUFF"
    ],
    "exp_reward": 1.5
  }
]
```

## 🎯 Pro Tips

### Recipe Design Best Practices

- **Balance is Key** - Make recipes challenging but fair
- **Test Thoroughly** - Try recipes in creative mode first
- **Consider Economy** - Don't break your server's balance
- **Use Alternatives** - Allow multiple ingredient options for flexibility


## 🐛 Troubleshooting

### Common Issues

**Recipes not appearing in-game:**
- Verify material names are correct
- Check server console for error messages
- Try `/cr reload` to refresh recipes
- Check if the JSON file is formatted correctly

**Invalid material name errors:**
- Use official Minecraft material names only
- Check [Spigot Material List](https://hub.spigotmc.org/javadocs/spigot/org/bukkit/Material.html)

**Recipe conflicts:**
- Custom recipes override vanilla ones
- Use unique patterns to avoid conflicts
- Check recipe index with `/cr list`

## 📞 Support & Contribution

### Getting Help
- Open an issue on [GitHub](https://github.com/KreativeName1/Custom-Recipes-MC-Paper/issues)

### Contributing
Contributions are welcome! Feel free to:
- Submit bug reports
- Suggest new features
- Create pull requests

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

---
