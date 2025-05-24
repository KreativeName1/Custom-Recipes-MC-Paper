package org.KreativeName.recipes.builders;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.KreativeName.recipes.utils.MaterialValidator;
import org.KreativeName.recipes.utils.RecipeFileManager;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;

public class ShapelessRecipeBuilder implements RecipeBuilder {
    private final RecipeFileManager fileManager;
    private final MaterialValidator validator;

    public ShapelessRecipeBuilder(JavaPlugin plugin) {
        this.fileManager = new RecipeFileManager(plugin);
        this.validator = new MaterialValidator();
    }

    @Override
    public void build(CommandSender sender, String[] args) {
        // /cr add shapeless <result_item> <result_count> <ingredient1> <count1> [<ingredient2> <count2>...]
        if (args.length < 6 || args.length % 2 != 0) {
            sender.sendMessage("§cUsage: /cr add shapeless <result_item> <result_count> <ingredient1> <count1> [<ingredient2> <count2>...]");
            return;
        }

        try {
            String resultItem = args[2].toUpperCase();
            if (!validator.isValidMaterial(resultItem)) {
                sender.sendMessage("§cInvalid result material: " + resultItem);
                return;
            }

            int resultCount = Integer.parseInt(args[3]);
            if (!validator.isValidCount(resultCount)) {
                sender.sendMessage("§cResult count must be between 1 and 64.");
                return;
            }

            // Process ingredients
            List<JsonObject> ingredients = parseIngredients(args, sender);
            if (ingredients == null) return;

            JsonObject recipeJson = createShapelessRecipeJson(resultItem, resultCount, ingredients);
            fileManager.addRecipeToFile(recipeJson);

            sender.sendMessage("§aShapeless recipe added successfully! Use /cr reload to apply changes.");

        } catch (NumberFormatException e) {
            sender.sendMessage("§cInvalid number format.");
        } catch (Exception e) {
            sender.sendMessage("§cAn error occurred: " + e.getMessage());
        }
    }

    private List<JsonObject> parseIngredients(String[] args, CommandSender sender) {
        List<JsonObject> ingredients = new ArrayList<>();

        for (int i = 4; i < args.length; i += 2) {
            if (i + 1 >= args.length) {
                sender.sendMessage("§cEach ingredient must have a count.");
                return null;
            }

            String ingredientStr = args[i];
            int count;

            try {
                count = Integer.parseInt(args[i + 1]);
            } catch (NumberFormatException e) {
                sender.sendMessage("§cInvalid count for ingredient: " + args[i + 1]);
                return null;
            }

            if (count <= 0) {
                sender.sendMessage("§cIngredient count must be greater than 0.");
                return null;
            }

            String[] materials = ingredientStr.split(",");
            // Validate all materials
            for (String mat : materials) {
                if (!validator.isValidMaterial(mat.toUpperCase())) {
                    sender.sendMessage("§cInvalid material: " + mat);
                    return null;
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

        return ingredients;
    }

    private JsonObject createShapelessRecipeJson(String resultItem, int resultCount, List<JsonObject> ingredients) {
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

        return recipeJson;
    }
}