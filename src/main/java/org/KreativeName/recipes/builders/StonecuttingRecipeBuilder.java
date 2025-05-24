package org.KreativeName.recipes.builders;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.KreativeName.recipes.utils.MaterialValidator;
import org.KreativeName.recipes.utils.RecipeFileManager;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

public class StonecuttingRecipeBuilder implements RecipeBuilder {
    private final RecipeFileManager fileManager;
    private final MaterialValidator validator;

    public StonecuttingRecipeBuilder(JavaPlugin plugin) {
        this.fileManager = new RecipeFileManager(plugin);
        this.validator = new MaterialValidator();
    }

    @Override
    public void build(CommandSender sender, String[] args) {
        // /cr add stonecutting <result_item> <result_count> <ingredient1,ingredient2...>
        if (args.length < 5) {
            sender.sendMessage("§cUsage: /cr add stonecutting <result_item> <result_count> <ingredient1,ingredient2...>");
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
            String[] ingredientsArr = args[4].split(",");
            // Validate all ingredients
            for (String ingredient : ingredientsArr) {
                if (!validator.isValidMaterial(ingredient.toUpperCase())) {
                    sender.sendMessage("§cInvalid material: " + ingredient);
                    return;
                }
            }

            JsonObject recipeJson = createStonecuttingRecipeJson(resultItem, resultCount, args[4]);
            fileManager.addRecipeToFile(recipeJson);

            sender.sendMessage("§aStonecutting recipe added successfully! Use /cr reload to apply changes.");

        } catch (NumberFormatException e) {
            sender.sendMessage("§cInvalid number format.");
        } catch (Exception e) {
            sender.sendMessage("§cAn error occurred: " + e.getMessage());
        }
    }

    private JsonObject createStonecuttingRecipeJson(String resultItem, int resultCount, String ingredients) {
        JsonObject recipeJson = new JsonObject();
        recipeJson.addProperty("type", "StonecuttingRecipe");

        JsonObject resultJson = new JsonObject();
        resultJson.addProperty("item", resultItem.toLowerCase());
        resultJson.addProperty("count", resultCount);
        recipeJson.add("result", resultJson);

        recipeJson.addProperty("ingredient", ingredients.toLowerCase());

        return recipeJson;
    }
}