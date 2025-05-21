package org.KreativeName.recipes;

import org.KreativeName.recipes.classes.PluginRecipe;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Villager;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class RecipeCommands implements CommandExecutor, TabCompleter {
    private final JavaPlugin plugin;
    private final RecipeLoader recipeLoader;
    private final File recipeFile;
    private final Gson gson;

    public RecipeCommands(JavaPlugin plugin) {
        this.plugin = plugin;
        this.recipeLoader = new RecipeLoader(plugin);
        this.recipeFile = new File(plugin.getDataFolder(), "recipes.json");
        this.gson = new GsonBuilder().setPrettyPrinting().create();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            sendHelpMessage(sender);
            return true;
        }

        String subCommand = args[0].toLowerCase();

        switch (subCommand) {
            case "add":
                handleAddCommand(sender, args);
                break;
            case "remove":
                handleRemoveCommand(sender, args);
                break;
            case "list":
                handleListCommand(sender);
                break;
            case "reload":
                handleReloadCommand(sender);
                break;
            case "help":
                sendHelpMessage(sender);
                break;
            default:
                sender.sendMessage("§cUnknown subcommand. Use /cr help for a list of commands.");
                break;
        }

        return true;
    }

    private void handleAddCommand(CommandSender sender, String[] args) {
        if (!sender.hasPermission("recipes.add")) {
            sender.sendMessage("§cYou don't have permission to add recipes.");
            return;
        }

        if (args.length < 2) {
            sender.sendMessage("§cUsage: /cr add <recipe_type> [options...]");
            return;
        }

        String recipeType = args[1].toLowerCase();

        switch (recipeType) {
            case "shaped":
                handleAddShapedRecipe(sender, args);
                break;
            case "shapeless":
                handleAddShapelessRecipe(sender, args);
                break;
            case "cooking":
                handleAddCookingRecipe(sender, args);
                break;
            case "stonecutting":
                handleAddStonecuttingRecipe(sender, args);
                break;
            case "merchant":
                handleAddMerchantRecipe(sender, args);
                break;
            default:
                sender.sendMessage("§cUnknown recipe type. Available types: shaped, shapeless, cooking, stonecutting, merchant");
                break;
        }
    }

    private void handleAddShapedRecipe(CommandSender sender, String[] args) {
        // /recipe add shaped <result_item> <result_count> <pattern_line1> <pattern_line2> <pattern_line3> <key1> <material1> [<key2> <material2>...]
        if (args.length < 8) {
            sender.sendMessage("§cUsage: /cr add shaped <result_item> <result_count> <pattern_line1> <pattern_line2> <pattern_line3> <key1> <material1> [<key2> <material2>...]");
            return;
        }

        try {
            String resultItem = args[2].toUpperCase();
            Material.valueOf(resultItem); // Validate material exists

            int resultCount = Integer.parseInt(args[3]);
            if (resultCount <= 0 || resultCount > 64) {
                sender.sendMessage("§cResult count must be between 1 and 64.");
                return;
            }

            String patternLine1 = args[4];
            String patternLine2 = args[5];
            String patternLine3 = args[6];

            if (patternLine1.length() > 3 || patternLine2.length() > 3 || patternLine3.length() > 3) {
                sender.sendMessage("§cPattern lines cannot be longer than 3 characters.");
                return;
            }

            // Process key-material pairs
            Map<Character, List<String>> ingredients = new HashMap<>();
            for (int i = 7; i < args.length; i += 2) {
                if (i + 1 >= args.length) {
                    sender.sendMessage("§cEach key must have at least one material.");
                    return;
                }

                char key = args[i].charAt(0);
                String materialStr = args[i + 1];
                String[] materials = materialStr.split(",");

                // Validate all materials
                List<String> validMaterials = new ArrayList<>();
                for (String mat : materials) {
                    try {
                        String upperMat = mat.toUpperCase();
                        Material.valueOf(upperMat);
                        validMaterials.add(upperMat);
                    } catch (IllegalArgumentException e) {
                        sender.sendMessage("§cInvalid material: " + mat);
                        return;
                    }
                }

                ingredients.put(key, validMaterials);
            }

            // Validate that all keys in pattern are defined
            Set<Character> patternChars = new HashSet<>();
            for (char c : (patternLine1 + patternLine2 + patternLine3).toCharArray()) {
                if (c != ' ') {
                    patternChars.add(c);
                }
            }

            for (char c : patternChars) {
                if (!ingredients.containsKey(c)) {
                    sender.sendMessage("§cKey '" + c + "' is used in pattern but not defined.");
                    return;
                }
            }

            // Create and save shaped recipe
            JsonObject recipeJson = new JsonObject();
            recipeJson.addProperty("type", "ShapedRecipe");

            JsonObject resultJson = new JsonObject();
            resultJson.addProperty("item", resultItem.toLowerCase());
            resultJson.addProperty("count", resultCount);
            recipeJson.add("result", resultJson);

            JsonArray patternArray = new JsonArray();
            patternArray.add(patternLine1);
            patternArray.add(patternLine2);
            patternArray.add(patternLine3);
            recipeJson.add("pattern", patternArray);

            JsonObject replaceJson = new JsonObject();
            for (Map.Entry<Character, List<String>> entry : ingredients.entrySet()) {
                char key = entry.getKey();
                List<String> materials = entry.getValue();

                if (materials.size() == 1) {
                    replaceJson.addProperty(String.valueOf(key), materials.get(0).toLowerCase());
                } else {
                    JsonArray materialsArray = new JsonArray();
                    for (String material : materials) {
                        materialsArray.add(material.toLowerCase());
                    }
                    replaceJson.add(String.valueOf(key), materialsArray);
                }
            }
            recipeJson.add("replace", replaceJson);

            // Add recipe to the recipes.json file
            addRecipeToFile(recipeJson);

            sender.sendMessage("§aShaped recipe added successfully! Use /cr reload to apply changes.");

        } catch (IllegalArgumentException e) {
            sender.sendMessage("§cInvalid material name or number format.");
        } catch (Exception e) {
            sender.sendMessage("§cAn error occurred: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void handleAddShapelessRecipe(CommandSender sender, String[] args) {
        // /recipe add shapeless <result_item> <result_count> <ingredient1> <count1> [<ingredient2> <count2>...]
        if (args.length < 6 || args.length % 2 != 0) {
            sender.sendMessage("§cUsage: /cr add shapeless <result_item> <result_count> <ingredient1> <count1> [<ingredient2> <count2>...]");
            return;
        }

        try {
            String resultItem = args[2].toUpperCase();
            Material.valueOf(resultItem); // Validate material exists

            int resultCount = Integer.parseInt(args[3]);
            if (resultCount <= 0 || resultCount > 64) {
                sender.sendMessage("§cResult count must be between 1 and 64.");
                return;
            }

            // Process ingredients
            List<JsonObject> ingredients = new ArrayList<>();
            for (int i = 4; i < args.length; i += 2) {
                if (i + 1 >= args.length) {
                    sender.sendMessage("§cEach ingredient must have a count.");
                    return;
                }

                String ingredientStr = args[i];
                int count = Integer.parseInt(args[i + 1]);

                if (count <= 0) {
                    sender.sendMessage("§cIngredient count must be greater than 0.");
                    return;
                }

                String[] materials = ingredientStr.split(",");
                // Validate all materials
                for (String mat : materials) {
                    try {
                        Material.valueOf(mat.toUpperCase());
                    } catch (IllegalArgumentException e) {
                        sender.sendMessage("§cInvalid material: " + mat);
                        return;
                    }
                }

                JsonObject ingredientJson = new JsonObject();
                ingredientJson.addProperty("count", count);

                if (materials.length == 1) {
                    ingredientJson.addProperty("item", materials[0].toLowerCase());
                } else {
                    JsonArray choicesArray = new JsonArray();
                    for (String material : materials) {
                        choicesArray.add(material.toLowerCase());
                    }
                    ingredientJson.add("choices", choicesArray);
                }

                ingredients.add(ingredientJson);
            }

            // Create and save shapeless recipe
            JsonObject recipeJson = new JsonObject();
            recipeJson.addProperty("type", "ShapelessRecipe");

            JsonObject resultJson = new JsonObject();
            resultJson.addProperty("item", resultItem.toLowerCase());
            resultJson.addProperty("count", resultCount);
            recipeJson.add("result", resultJson);

            JsonArray ingredientsArray = new JsonArray();
            for (JsonObject ingredient : ingredients) {
                ingredientsArray.add(ingredient);
            }
            recipeJson.add("ingredients", ingredientsArray);

            // Add recipe to the recipes.json file
            addRecipeToFile(recipeJson);

            sender.sendMessage("§aShapeless recipe added successfully! Use /cr reload to apply changes.");

        } catch (NumberFormatException e) {
            sender.sendMessage("§cInvalid number format.");
        } catch (IllegalArgumentException e) {
            sender.sendMessage("§cInvalid material name.");
        } catch (Exception e) {
            sender.sendMessage("§cAn error occurred: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void handleAddCookingRecipe(CommandSender sender, String[] args) {
        // /recipe add cooking <result_item> <result_count> <exp_reward> <cooking_type1,cooking_type2...> <cooking_time> <ingredient1,ingredient2...>
        if (args.length < 8) {
            sender.sendMessage("§cUsage: /cr add cooking <result_item> <result_count> <exp_reward> <cooking_type1,cooking_type2...> <cooking_time> <ingredient1,ingredient2...>");
            sender.sendMessage("§cCooking types: furnace, blasting, smoking, campfire");
            return;
        }

        try {
            String resultItem = args[2].toUpperCase();
            Material.valueOf(resultItem); // Validate result material exists

            int resultCount = Integer.parseInt(args[3]);
            if (resultCount <= 0 || resultCount > 64) {
                sender.sendMessage("§cResult count must be between 1 and 64.");
                return;
            }

            float expReward = Float.parseFloat(args[4]);
            if (expReward < 0) {
                sender.sendMessage("§cExp reward cannot be negative.");
                return;
            }

            // Process cooking types
            String[] cookingTypesArr = args[5].split(",");
            List<JsonObject> cookingTypes = new ArrayList<>();
            for (String type : cookingTypesArr) {
                String formattedType = formatCookingType(type);
                if (formattedType == null) {
                    sender.sendMessage("§cInvalid cooking type: " + type + ". Valid types: furnace, blasting, smoking, campfire");
                    return;
                }

                int cookingTime = Integer.parseInt(args[6]);
                if (cookingTime <= 0) {
                    sender.sendMessage("§cCooking time must be greater than 0.");
                    return;
                }

                JsonObject cookingTypeJson = new JsonObject();
                cookingTypeJson.addProperty("type", formattedType);
                cookingTypeJson.addProperty("cooking_time", cookingTime);
                cookingTypes.add(cookingTypeJson);
            }

            // Process ingredients
            String[] ingredientsArr = args[7].split(",");
            JsonArray ingredientsArray = new JsonArray();
            for (String ingredient : ingredientsArr) {
                try {
                    Material.valueOf(ingredient.toUpperCase());
                    ingredientsArray.add(ingredient.toLowerCase());
                } catch (IllegalArgumentException e) {
                    sender.sendMessage("§cInvalid material: " + ingredient);
                    return;
                }
            }

            // Create and save cooking recipe
            JsonObject recipeJson = new JsonObject();
            recipeJson.addProperty("type", "CookingRecipe");

            JsonObject resultJson = new JsonObject();
            resultJson.addProperty("item", resultItem.toLowerCase());
            resultJson.addProperty("count", resultCount);
            recipeJson.add("result", resultJson);

            JsonArray cookingTypesArray = new JsonArray();
            for (JsonObject cookingType : cookingTypes) {
                cookingTypesArray.add(cookingType);
            }
            recipeJson.add("cookingTypes", cookingTypesArray);

            recipeJson.add("ingredient", ingredientsArray);
            recipeJson.addProperty("exp_reward", expReward);

            // Add recipe to the recipes.json file
            addRecipeToFile(recipeJson);

            sender.sendMessage("§aCooking recipe added successfully! Use /cr reload to apply changes.");

        } catch (NumberFormatException e) {
            sender.sendMessage("§cInvalid number format.");
        } catch (IllegalArgumentException e) {
            sender.sendMessage("§cInvalid material name.");
        } catch (Exception e) {
            sender.sendMessage("§cAn error occurred: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private String formatCookingType(String type) {
        type = type.toLowerCase();
        switch (type) {
            case "furnace":
                return "FurnaceRecipe";
            case "blasting":
                return "BlastingRecipe";
            case "smoking":
                return "SmokingRecipe";
            case "campfire":
                return "CampfireRecipe";
            default:
                return null;
        }
    }

    private void handleAddStonecuttingRecipe(CommandSender sender, String[] args) {
        // /recipe add stonecutting <result_item> <result_count> <ingredient1,ingredient2...>
        if (args.length < 5) {
            sender.sendMessage("§cUsage: /cr add stonecutting <result_item> <result_count> <ingredient1,ingredient2...>");
            return;
        }

        try {
            String resultItem = args[2].toUpperCase();
            Material.valueOf(resultItem); // Validate result material exists

            int resultCount = Integer.parseInt(args[3]);
            if (resultCount <= 0 || resultCount > 64) {
                sender.sendMessage("§cResult count must be between 1 and 64.");
                return;
            }

            // Process ingredients
            String[] ingredientsArr = args[4].split(",");

            // Validate all ingredients
            for (String ingredient : ingredientsArr) {
                try {
                    Material.valueOf(ingredient.toUpperCase());
                } catch (IllegalArgumentException e) {
                    sender.sendMessage("§cInvalid material: " + ingredient);
                    return;
                }
            }

            // Create and save stonecutting recipe
            JsonObject recipeJson = new JsonObject();
            recipeJson.addProperty("type", "StonecuttingRecipe");

            JsonObject resultJson = new JsonObject();
            resultJson.addProperty("item", resultItem.toLowerCase());
            resultJson.addProperty("count", resultCount);
            recipeJson.add("result", resultJson);

            JsonObject ingredientJson = new JsonObject();
            if (ingredientsArr.length == 1) {
                ingredientJson.addProperty("item", ingredientsArr[0].toLowerCase());
            } else {
                JsonArray itemArray = new JsonArray();
                for (String ingredient : ingredientsArr) {
                    itemArray.add(ingredient.toLowerCase());
                }
                ingredientJson.add("item", itemArray);
            }
            recipeJson.add("ingredient", ingredientJson);

            // Add recipe to the recipes.json file
            addRecipeToFile(recipeJson);

            sender.sendMessage("§aStonecutting recipe added successfully! Use /cr reload to apply changes.");

        } catch (NumberFormatException e) {
            sender.sendMessage("§cInvalid number format.");
        } catch (IllegalArgumentException e) {
            sender.sendMessage("§cInvalid material name.");
        } catch (Exception e) {
            sender.sendMessage("§cAn error occurred: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void handleAddMerchantRecipe(CommandSender sender, String[] args) {
        // /recipe add merchant <result_item> <result_count> <max_uses> <exp_reward> <villager_exp> <price_multiplier> <profession> <ingredient1> <count1> [<ingredient2> <count2>]
        if (args.length < 10) {
            sender.sendMessage("§cUsage: /cr add merchant <result_item> <result_count> <max_uses> <exp_reward> <villager_exp> <price_multiplier> <profession> <ingredient1> <count1> [<ingredient2> <count2>]");
            return;
        }

        try {
            String resultItem = args[2].toUpperCase();
            Material.valueOf(resultItem); // Validate result material exists

            int resultCount = Integer.parseInt(args[3]);
            if (resultCount <= 0 || resultCount > 64) {
                sender.sendMessage("§cResult count must be between 1 and 64.");
                return;
            }

            int maxUses = Integer.parseInt(args[4]);
            if (maxUses <= 0) {
                sender.sendMessage("§cMax uses must be greater than 0.");
                return;
            }

            boolean expReward = Boolean.parseBoolean(args[5]);
            int villagerExp = Integer.parseInt(args[6]);
            float priceMultiplier = Float.parseFloat(args[7]);

            String profession = args[8].toUpperCase();
            try {
                Villager.Profession.valueOf(profession);
            } catch (IllegalArgumentException e) {
                sender.sendMessage("§cInvalid profession: " + profession);
                return;
            }

            // Process ingredients (up to 2)
            List<JsonObject> ingredients = new ArrayList<>();
            for (int i = 9; i < args.length; i += 2) {
                if (i + 1 >= args.length) {
                    sender.sendMessage("§cEach ingredient must have a count.");
                    return;
                }

                String ingredientStr = args[i].toUpperCase();
                try {
                    Material.valueOf(ingredientStr);
                } catch (IllegalArgumentException e) {
                    sender.sendMessage("§cInvalid material: " + ingredientStr);
                    return;
                }

                int count = Integer.parseInt(args[i + 1]);
                if (count <= 0) {
                    sender.sendMessage("§cIngredient count must be greater than 0.");
                    return;
                }

                JsonObject ingredientJson = new JsonObject();
                ingredientJson.addProperty("item", args[i].toLowerCase());
                ingredientJson.addProperty("count", count);
                ingredients.add(ingredientJson);

                if (ingredients.size() > 2) {
                    sender.sendMessage("§cA merchant recipe can have at most 2 ingredients.");
                    return;
                }
            }

            // Create and save merchant recipe
            JsonObject recipeJson = new JsonObject();
            recipeJson.addProperty("type", "MerchantRecipe");

            JsonObject resultJson = new JsonObject();
            resultJson.addProperty("item", resultItem.toLowerCase());
            resultJson.addProperty("count", resultCount);
            recipeJson.add("result", resultJson);

            JsonArray ingredientsArray = new JsonArray();
            for (JsonObject ingredient : ingredients) {
                ingredientsArray.add(ingredient);
            }
            recipeJson.add("ingredients", ingredientsArray);

            recipeJson.addProperty("max_uses", maxUses);
            recipeJson.addProperty("exp_reward", expReward);
            recipeJson.addProperty("villager_exp", villagerExp);
            recipeJson.addProperty("priceMultiplier", priceMultiplier);
            recipeJson.addProperty("demand", 0);
            recipeJson.addProperty("special_price", 0);

            JsonArray professionArray = new JsonArray();
            professionArray.add(profession);
            recipeJson.add("profession", professionArray);

            // Add recipe to the recipes.json file
            addRecipeToFile(recipeJson);

            sender.sendMessage("§aMerchant recipe added successfully! Use /cr reload to apply changes.");

        } catch (NumberFormatException e) {
            sender.sendMessage("§cInvalid number format.");
        } catch (IllegalArgumentException e) {
            sender.sendMessage("§cInvalid material name or profession.");
        } catch (Exception e) {
            sender.sendMessage("§cAn error occurred: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void handleRemoveCommand(CommandSender sender, String[] args) {
        if (!sender.hasPermission("recipes.remove")) {
            sender.sendMessage("§cYou don't have permission to remove recipes.");
            return;
        }

        if (args.length < 2) {
            sender.sendMessage("§cUsage: /cr remove <index>");
            return;
        }

        try {
            int index = Integer.parseInt(args[1]);
            removeRecipeByIndex(sender, index);
        } catch (NumberFormatException e) {
            sender.sendMessage("§cIndex must be a number.");
        }
    }

    private void handleListCommand(CommandSender sender) {
        if (!sender.hasPermission("recipes.list")) {
            sender.sendMessage("§cYou don't have permission to list recipes.");
            return;
        }

        try {
            String jsonContent = readRecipeFile();
            JsonArray recipesArray = JsonParser.parseString(jsonContent).getAsJsonArray();

            sender.sendMessage("§a=== Custom Recipes ===");

            for (int i = 0; i < recipesArray.size(); i++) {
                JsonObject recipe = recipesArray.get(i).getAsJsonObject();
                String type = recipe.get("type").getAsString();
                JsonObject result = recipe.getAsJsonObject("result");
                String resultItem = result.get("item").getAsString();
                int resultCount = result.get("count").getAsInt();

                sender.sendMessage(String.format("§e%d. §f%s §7- §f%dx %s",
                        i, type, resultCount, resultItem));
            }

            sender.sendMessage("§7Use /cr reload to apply any changes.");

        } catch (Exception e) {
            sender.sendMessage("§cFailed to list recipes: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void handleReloadCommand(CommandSender sender) {
        if (!sender.hasPermission("recipes.reload")) {
            sender.sendMessage("§cYou don't have permission to reload recipes.");
            return;
        }

        try {
            // First clear all existing recipes
            for (NamespacedKey key : Initialize.registeredRecipeKeys) {
                plugin.getServer().removeRecipe(key);
            }
            Initialize.registeredRecipeKeys.clear();

            // Then load and register the recipes again
            List<PluginRecipe> recipes = recipeLoader.loadRecipes("recipes.json");
            Initialize.registeredRecipeKeys.addAll(recipeLoader.registerRecipes(recipes));

            sender.sendMessage("§aSuccessfully reloaded " + recipes.size() + " custom recipes!");

        } catch (Exception e) {
            sender.sendMessage("§cFailed to reload recipes: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void addRecipeToFile(JsonObject recipeJson) throws IOException {
        // Read existing recipes
        String jsonContent = readRecipeFile();
        JsonArray recipesArray = JsonParser.parseString(jsonContent).getAsJsonArray();

        // Add new recipe
        recipesArray.add(recipeJson);

        // Write back to file
        try (FileWriter writer = new FileWriter(recipeFile, StandardCharsets.UTF_8)) {
            writer.write(gson.toJson(recipesArray));
        }
    }

    private void removeRecipeByIndex(CommandSender sender, int index) {
        try {
            // Read existing recipes
            String jsonContent = readRecipeFile();
            JsonArray recipesArray = JsonParser.parseString(jsonContent).getAsJsonArray();

            if (index < 0 || index >= recipesArray.size()) {
                sender.sendMessage("§cInvalid recipe index. Use /cr list to see available recipes.");
                return;
            }

            // Remove recipe at index
            JsonElement removedRecipe = recipesArray.remove(index);
            String removedType = removedRecipe.getAsJsonObject().get("type").getAsString();

            // Write back to file
            try (FileWriter writer = new FileWriter(recipeFile, StandardCharsets.UTF_8)) {
                writer.write(gson.toJson(recipesArray));
            }

            sender.sendMessage("§aSuccessfully removed " + removedType + " recipe at index " + index);
            sender.sendMessage("§7Use /cr reload to apply changes.");

        } catch (Exception e) {
            sender.sendMessage("§cFailed to remove recipe: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private String readRecipeFile() throws IOException {
        if (!recipeFile.exists()) {
            throw new IOException("Recipe file not found: " + recipeFile.getAbsolutePath());
        }

        try (FileReader reader = new FileReader(recipeFile, StandardCharsets.UTF_8)) {
            StringBuilder content = new StringBuilder();
            char[] buffer = new char[1024];
            int bytesRead;

            while ((bytesRead = reader.read(buffer)) != -1) {
                content.append(buffer, 0, bytesRead);
            }

            return content.toString();
        }
    }

    private void sendHelpMessage(CommandSender sender) {
        sender.sendMessage("§a=== Custom Recipes Commands ===");
        sender.sendMessage("§e/cr add shaped §7- Add a shaped recipe");
        sender.sendMessage("§e/cr add shapeless §7- Add a shapeless recipe");
        sender.sendMessage("§e/cr add cooking §7- Add a cooking recipe");
        sender.sendMessage("§e/cr add stonecutting §7- Add a stonecutting recipe");
        sender.sendMessage("§e/cr add merchant §7- Add a merchant recipe");
        sender.sendMessage("§e/cr remove <index> §7- Remove a recipe by index");
        sender.sendMessage("§e/cr list §7- List all custom recipes");
        sender.sendMessage("§e/cr reload §7- Reload recipes from file");
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            return filterCompletions(Arrays.asList("add", "remove", "list", "reload", "help"), args[0]);
        } else if (args.length == 2) {
            if (args[0].equalsIgnoreCase("add")) {
                return filterCompletions(Arrays.asList("shaped", "shapeless", "cooking", "stonecutting", "merchant"), args[1]);
            }
        } else if (args.length >= 3) {
            if (args[0].equalsIgnoreCase("add")) {
                String recipeType = args[1].toLowerCase();

                // For material suggestions
                if ((recipeType.equals("shaped") && args.length >= 3 && args.length <= 4) ||
                        (recipeType.equals("shapeless") && args.length == 3) ||
                        (recipeType.equals("cooking") && args.length == 3) ||
                        (recipeType.equals("stonecutting") && args.length == 3) ||
                        (recipeType.equals("merchant") && args.length == 3)) {
                    return filterCompletions(getMaterialNames(), args[args.length - 1]);
                }

                // For profession suggestions
                if (recipeType.equals("merchant") && args.length == 9) {
                    return filterCompletions(getVillagerProfessions(), args[8]);
                }

                // For cooking type suggestions
                if (recipeType.equals("cooking") && args.length == 6) {
                    return filterCompletions(Arrays.asList("furnace", "blasting", "smoking", "campfire",
                            "furnace,blasting", "furnace,smoking", "furnace,campfire",
                            "blasting,smoking", "blasting,campfire", "smoking,campfire",
                            "furnace,blasting,smoking", "furnace,blasting,campfire", "furnace,smoking,campfire",
                            "blasting,smoking,campfire", "furnace,blasting,smoking,campfire"), args[5]);
                }

                // For ingredient suggestions in cooking recipes
                if (recipeType.equals("cooking") && args.length == 8) {
                    return filterCompletions(getMaterialNames(), args[7]);
                }

                // For stonecutting ingredient suggestions
                if (recipeType.equals("stonecutting") && args.length == 5) {
                    return filterCompletions(getMaterialNames(), args[4]);
                }

                // For merchant recipe ingredient suggestions
                if (recipeType.equals("merchant") && (args.length == 10 || args.length == 12)) {
                    return filterCompletions(getMaterialNames(), args[args.length - 2]);
                }
            }
        }

        return completions;
    }

    private List<String> filterCompletions(List<String> completions, String prefix) {
        return completions.stream()
                .filter(s -> s.toLowerCase().startsWith(prefix.toLowerCase()))
                .collect(Collectors.toList());
    }

    private List<String> getMaterialNames() {
        return Stream.of(Material.values())
                .filter(material -> material.isItem())
                .map(material -> material.name().toLowerCase())
                .collect(Collectors.toList());
    }

    private List<String> getVillagerProfessions() {
        return Stream.of(Villager.Profession.values())
                .map(profession -> profession.name().toLowerCase())
                .collect(Collectors.toList());
    }
}