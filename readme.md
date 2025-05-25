# Custom Recipe Loader Plugin

[![Version](https://img.shields.io/badge/version-1.1.0-blue.svg)](https://github.com/YourUsername/Recipes/releases)
[![Minecraft](https://img.shields.io/badge/minecraft-1.21.5+-green.svg)](https://minecraft.net)
[![License](https://img.shields.io/badge/license-MIT-yellow.svg)](LICENSE)

A powerful and flexible Minecraft plugin that enables server administrators to create and manage custom recipes directly in-game. Add unique crafting experiences to your server without touching a single line of code!

## ✨ Features

### 🔧 Recipe Types
- **Shaped Crafting** - Traditional crafting table recipes with specific patterns
- **Shapeless Crafting** - Mix ingredients in any arrangement
- **Cooking Recipes** - Custom furnace, smoker, blast furnace, and campfire recipes
- **Stonecutting** - Precision cutting recipes for the stonecutter
- **Villager Trading** - Custom merchant trades for enhanced gameplay

### 🎮 Easy Management
- **In-Game Commands** - No need to edit files manually
- **Live Reloading** - Apply changes without server restarts
- **Persistent Storage** - All recipes saved automatically
- **Permission System** - Control who can manage recipes

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
- `pattern` - 9-character grid pattern, use underscores to represent whitespaces (e.g., `"ABC DEF G__"`)
- `char` - Pattern character (A-Z)
- `ingredients` - Materials for that character (comma-separated for multiple options)

**Examples:**

```bash
# Golden Apple Recipe (surround apple with gold)
/cr add shaped GOLDEN_APPLE 1 GGG GAG GGG G GOLD_INGOT A APPLE

# Custom Sword Recipe
/cr add shaped DIAMOND_SWORD 1  _D_  _D_  _S_  D DIAMOND S STICK

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

# Recycling recipe
/cr add shapeless IRON_INGOT 9 IRON_BLOCK 1

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
# Fast diamond smelting
/cr add cooking DIAMOND 1 2.0 furnace,blasting 100 COAL_BLOCK

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
# Convert blocks efficiently
/cr add stonecutting DIAMOND 4 DIAMOND_BLOCK

# Alternative crafting method
/cr add stonecutting STICK 8 OAK_LOG,BIRCH_LOG,SPRUCE_LOG

# Resource conversion
/cr add stonecutting QUARTZ 6 QUARTZ_BLOCK
```

### 💰 Villager Trading

Add custom trades to villager professions.

**Command Format:**
```bash
/cr add merchant <result> <count> <max_uses> <gives_xp> <villager_xp> <price_multiplier> <profession> <ingredient1> <amount1> [<ingredient2> <amount2>]
```

**Parameters:**
- `max_uses` - Trades before restocking (1-999)
- `gives_xp` - Whether player gets XP (`true`/`false`)
- `villager_xp` - XP for villager (affects level)
- `price_multiplier` - Demand adjustment (0.0-1.0)
- `profession` - Villager type (see profession list below, comma-separated for multiple)
  
**Villager Professions:**
`FARMER`, `LIBRARIAN`, `BUTCHER`, `NITWIT`, `NONE`, `ARMORER`, `WEAPONSMITH`, `TOOLSMITH`, `LEATHERWORKER`, `MASON`, `FISHERMAN`, `CLERIC`, `FLETCHER`, `SHEPERD`, `CARTOGGRAPHER`

**Examples:**

```bash
# Expensive diamond trade
/cr add merchant DIAMOND 1 3 true 15 0.1 LIBRARIAN EMERALD 10 BOOK 5

# Bulk resource trade
/cr add merchant IRON_INGOT 16 10 false 5 0.05 TOOLSMITH,WEAPONSMITH EMERALD 8

# Special enchanted item trade
/cr add merchant ENCHANTED_GOLDEN_APPLE 1 1 true 50 0.2 CLERIC EMERALD 32 GOLD_BLOCK 4
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
    "type": "shaped",
    "result": {
      "item": "GOLDEN_APPLE",
      "count": 1
    },
    "pattern": [
      "GGG",
      "GAG", 
      "GGG"
    ],
    "ingredients": {
      "G": "GOLD_INGOT",
      "A": "APPLE"
    }
  },
  {
    "type": "shapeless",
    "result": {
      "item": "DIAMOND",
      "count": 1
    },
    "ingredients": [
      {
        "item": "COAL_BLOCK",
        "count": 2
      }
    ]
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

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

---
