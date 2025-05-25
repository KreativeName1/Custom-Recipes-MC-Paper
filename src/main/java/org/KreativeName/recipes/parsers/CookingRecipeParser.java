package org.KreativeName.recipes.parsers;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CookingRecipeParser {

    public Map<NamespacedKey,Recipe> parse(JsonObject recipeJson, NamespacedKey key) {
        if (!recipeJson.has("type") || !recipeJson.get("type").getAsString().equals("CookingRecipe")) {
            return null;
        }

        JsonObject resultJson = recipeJson.getAsJsonObject("result");
        String resultItem = resultJson.get("item").getAsString();
        int resultCount = resultJson.has("count") ? resultJson.get("count").getAsInt() : 1;
        ItemStack resultStack = new ItemStack(Material.valueOf(resultItem.toUpperCase()), resultCount);

        RecipeChoice ingredientChoice;
        if (recipeJson.has("ingredient")) {
            if (recipeJson.get("ingredient").isJsonArray()) {
                JsonArray ingredientArray = recipeJson.getAsJsonArray("ingredient");
                List<Material> materials = new ArrayList<>();

                for (JsonElement element : ingredientArray) {
                    materials.add(Material.valueOf(element.getAsString().toUpperCase()));
                }

                ingredientChoice = new RecipeChoice.MaterialChoice(materials);
            } else {
                String ingredient = recipeJson.get("ingredient").getAsString();
                ingredientChoice = new RecipeChoice.MaterialChoice(Material.valueOf(ingredient.toUpperCase()));
            }
        } else {
            return null;
        }
        float expReward = recipeJson.has("exp_reward") ? recipeJson.get("exp_reward").getAsFloat() : 0;

        Map<NamespacedKey, Recipe> map = new HashMap<>();
        if (recipeJson.has("cookingTypes")) {
            JsonArray cookingTypes = recipeJson.getAsJsonArray("cookingTypes");
            for (JsonElement element : cookingTypes) {
                JsonObject cookingType = element.getAsJsonObject();
                String type = cookingType.get("type").getAsString();
                int cookingTime = cookingType.get("cooking_time").getAsInt();
                switch (type) {
                    case "BlastingRecipe":
                        NamespacedKey blastingKey = new NamespacedKey(key.getNamespace(), key.getKey() + "_blasting");
                        map.put(blastingKey, new BlastingRecipe(blastingKey, resultStack, ingredientChoice, expReward, cookingTime));
                        break;
                    case "CampfireRecipe":
                        NamespacedKey campfireKey = new NamespacedKey(key.getNamespace(), key.getKey() + "_campfire");
                        map.put(campfireKey, new CampfireRecipe(campfireKey, resultStack, ingredientChoice, expReward, cookingTime));
                        break;
                    case "FurnaceRecipe":
                        NamespacedKey furnaceKey = new NamespacedKey(key.getNamespace(), key.getKey() + "_furnace");
                        map.put(furnaceKey, new FurnaceRecipe(furnaceKey, resultStack, ingredientChoice, expReward, cookingTime));
                        break;
                    case "SmokingRecipe":
                        NamespacedKey smokingKey = new NamespacedKey(key.getNamespace(), key.getKey() + "_smoking");
                        map.put(smokingKey, new SmokingRecipe(smokingKey, resultStack, ingredientChoice, expReward, cookingTime));
                        break;
                    default:
                        // Unknown cooking type
                        break;
                }
            }
        }
        if (map.isEmpty()) {
            return null;
        }
        return map;
    }
}