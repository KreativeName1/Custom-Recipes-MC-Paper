package org.KreativeName.recipes.builders;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.KreativeName.recipes.utils.KeyGenerator;
import org.KreativeName.recipes.utils.MaterialValidator;
import org.KreativeName.recipes.utils.RecipeFileManager;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Villager;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;

public class MerchantRecipeBuilder implements RecipeBuilder {
    private final RecipeFileManager fileManager;
    private final MaterialValidator validator;

    public MerchantRecipeBuilder(JavaPlugin plugin) {
        this.fileManager = new RecipeFileManager(plugin);
        this.validator = new MaterialValidator();
    }

    @Override
    public void build(CommandSender sender, String[] args) {
        // /cr add merchant <result_item> <result_count> <max_uses> <exp_reward> <villager_exp> <price_multiplier> <profession> <ingredient1> <count1> [<ingredient2> <count2>]
        if (args.length < 10) {
            sender.sendMessage("§cUsage: /cr add merchant <result_item> <result_count> <max_uses> <exp_reward> <villager_exp> <price_multiplier> <profession> <ingredient1> <count1> [<ingredient2> <count2>]");
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

            int maxUses = Integer.parseInt(args[4]);
            if (maxUses <= 0) {
                sender.sendMessage("§cMax uses must be greater than 0.");
                return;
            }

            boolean expReward = Boolean.parseBoolean(args[5]);

            int villagerExp;
            try {
                villagerExp = Integer.parseInt(args[6]);
                if (villagerExp < 0) {
                    sender.sendMessage("§cVillager experience cannot be negative.");
                    return;
                }
            } catch (NumberFormatException e) {
                sender.sendMessage("§cInvalid villager experience value: " + args[6]);
                return;
            }

            float priceMultiplier;
            try {
                priceMultiplier = Float.parseFloat(args[7]);
                if (priceMultiplier < 0) {
                    sender.sendMessage("§cPrice multiplier cannot be negative.");
                    return;
                }
            } catch (NumberFormatException e) {
                sender.sendMessage("§cInvalid price multiplier: " + args[7]);
                return;
            }

            // Validate profession
            String profession = args[8].toUpperCase();
            try {
                Villager.Profession.valueOf(profession);
            } catch (IllegalArgumentException e) {
                sender.sendMessage("§cInvalid profession: " + profession);
                return;
            }

            // Process ingredients (up to 2)
            List<JsonObject> ingredients = parseIngredients(args, sender);
            if (ingredients == null || ingredients.isEmpty() || ingredients.size() > 2) {
                return;
            }

            JsonObject recipeJson = createMerchantRecipeJson(resultItem, resultCount, maxUses,
                    expReward, villagerExp, priceMultiplier, profession, ingredients);
            fileManager.addRecipeToFile(recipeJson);

            sender.sendMessage("§aMerchant recipe added successfully! Use /cr reload to apply changes.");

        } catch (Exception e) {
            sender.sendMessage("§cAn error occurred: " + e.getMessage());
        }
    }

    private List<JsonObject> parseIngredients(String[] args, CommandSender sender) {
        List<JsonObject> ingredients = new ArrayList<>();

        for (int i = 9; i < args.length; i += 2) {
            if (i + 1 >= args.length) {
                sender.sendMessage("§cEach ingredient must have a count.");
                return null;
            }

            String ingredientStr = args[i].toUpperCase();
            if (!validator.isValidMaterial(ingredientStr)) {
                sender.sendMessage("§cInvalid material: " + ingredientStr);
                return null;
            }

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

            JsonObject ingredientJson = new JsonObject();
            ingredientJson.addProperty("item", args[i].toLowerCase());
            ingredientJson.addProperty("count", count);
            ingredients.add(ingredientJson);

            if (ingredients.size() > 2) {
                sender.sendMessage("§cA merchant recipe can have at most 2 ingredients.");
                return null;
            }
        }

        return ingredients;
    }

    private JsonObject createMerchantRecipeJson(String resultItem, int resultCount, int maxUses,
                                              boolean expReward, int villagerExp, float priceMultiplier,
                                              String profession, List<JsonObject> ingredients) {
        JsonObject recipeJson = new JsonObject();
        recipeJson.addProperty("type", "MerchantRecipe");
        recipeJson.addProperty("key", KeyGenerator.generateKey("merchant", resultItem));

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

        return recipeJson;
    }
}