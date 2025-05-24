package org.KreativeName.recipes.builders;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.KreativeName.recipes.utils.MaterialValidator;
import org.KreativeName.recipes.utils.RecipeFileManager;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;

public class CookingRecipeBuilder implements RecipeBuilder {
    private final RecipeFileManager fileManager;
    private final MaterialValidator validator;

    public CookingRecipeBuilder(JavaPlugin plugin) {
        this.fileManager = new RecipeFileManager(plugin);
        this.validator = new MaterialValidator();
    }

    @Override
    public void build(CommandSender sender, String[] args) {
        // /cr add cooking <result_item> <result_count> <exp_reward> <cooking_type1,cooking_type2...> <cooking_time> <ingredient1,ingredient2...>
        if (args.length < 8) {
            sender.sendMessage("§cUsage: /cr add cooking <result_item> <result_count> <exp_reward> <cooking_type1,cooking_type2...> <cooking_time> <ingredient1,ingredient2...>");
            sender.sendMessage("§cCooking types: furnace, blasting, smoking, campfire");
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

            float expReward;
            try {
                expReward = Float.parseFloat(args[4]);
                if (expReward < 0) {
                    sender.sendMessage("§cExp reward cannot be negative.");
                    return;
                }
            } catch (NumberFormatException e) {
                sender.sendMessage("§cInvalid experience reward value: " + args[4]);
                return;
            }

            // Process cooking types
            List<JsonObject> cookingTypes = parseCookingTypes(args[5], args[6], sender);
            if (cookingTypes == null || cookingTypes.isEmpty()) return;

            // Process ingredients
            List<String> ingredients = parseIngredients(args[7], sender);
            if (ingredients == null || ingredients.isEmpty()) return;

            JsonObject recipeJson = createCookingRecipeJson(resultItem, resultCount, expReward, cookingTypes, ingredients);
            fileManager.addRecipeToFile(recipeJson);

            sender.sendMessage("§aCooking recipe added successfully! Use /cr reload to apply changes.");

        } catch (Exception e) {
            sender.sendMessage("§cAn error occurred: " + e.getMessage());
        }
    }

    private List<JsonObject> parseCookingTypes(String cookingTypesStr, String cookingTimeStr, CommandSender sender) {
        List<JsonObject> cookingTypes = new ArrayList<>();
        int cookingTime;

        try {
            cookingTime = Integer.parseInt(cookingTimeStr);
            if (cookingTime <= 0) {
                sender.sendMessage("§cCooking time must be greater than 0.");
                return null;
            }
        } catch (NumberFormatException e) {
            sender.sendMessage("§cInvalid cooking time: " + cookingTimeStr);
            return null;
        }

        String[] cookingTypesArr = cookingTypesStr.split(",");
        for (String type : cookingTypesArr) {
            String formattedType = formatCookingType(type);
            if (formattedType == null) {
                sender.sendMessage("§cInvalid cooking type: " + type + ". Valid types: furnace, blasting, smoking, campfire");
                return null;
            }

            JsonObject cookingTypeJson = new JsonObject();
            cookingTypeJson.addProperty("type", formattedType);
            cookingTypeJson.addProperty("cooking_time", cookingTime);
            cookingTypes.add(cookingTypeJson);
        }

        return cookingTypes;
    }

    private List<String> parseIngredients(String ingredientsStr, CommandSender sender) {
        List<String> validIngredients = new ArrayList<>();
        String[] ingredientsArr = ingredientsStr.split(",");

        for (String ingredient : ingredientsArr) {
            if (!validator.isValidMaterial(ingredient.toUpperCase())) {
                sender.sendMessage("§cInvalid material: " + ingredient);
                return null;
            }
            validIngredients.add(ingredient.toLowerCase());
        }

        return validIngredients;
    }

    private JsonObject createCookingRecipeJson(String resultItem, int resultCount, float expReward,
                                             List<JsonObject> cookingTypes, List<String> ingredients) {
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

        JsonArray ingredientsArray = new JsonArray();
        for (String ingredient : ingredients) {
            ingredientsArray.add(ingredient);
        }
        recipeJson.add("ingredient", ingredientsArray);
        recipeJson.addProperty("exp_reward", expReward);

        return recipeJson;
    }

    private String formatCookingType(String type) {
        type = type.toLowerCase();
        switch (type) {
            case "furnace": return "FurnaceRecipe";
            case "blasting": return "BlastingRecipe";
            case "smoking": return "SmokingRecipe";
            case "campfire": return "CampfireRecipe";
            default: return null;
        }
    }
}