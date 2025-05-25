package org.KreativeName.recipes.parsers;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.RecipeChoice;
import org.bukkit.inventory.ShapedRecipe;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ShapedRecipeParser {
    public ShapedRecipe parse(JsonObject recipeJson, NamespacedKey key) {
        if (!recipeJson.has("type") || !recipeJson.get("type").getAsString().equals("ShapedRecipe")) {
            return null;
        }

        JsonObject result = recipeJson.getAsJsonObject("result");
        String item = result.get("item").getAsString();
        int count = result.has("count") ? result.get("count").getAsInt() : 1;
        ItemStack itemStack = new ItemStack(Material.matchMaterial(item), count);
        ShapedRecipe recipe = new ShapedRecipe(key, itemStack);

        if (recipeJson.has("pattern")) {
            JsonArray patternArray = recipeJson.getAsJsonArray("pattern");
            List<String> pattern = new ArrayList<>();
            for (JsonElement element : patternArray) {
                pattern.add(element.getAsString());
            }
            recipe.shape(pattern.toArray(new String[0]));
        }

        if (recipeJson.has("replace")) {
            JsonObject replaceObject = recipeJson.getAsJsonObject("replace");
            for (Map.Entry<String, JsonElement> entry : replaceObject.entrySet()) {
                char ingredientChar = entry.getKey().charAt(0);
                JsonElement ingredientElement = entry.getValue();

                // Handle both string and array formats
                if (ingredientElement.isJsonArray()) {
                    // Multiple material choices
                    JsonArray materialNamesArray = ingredientElement.getAsJsonArray();
                    Material[] materials = new Material[materialNamesArray.size()];
                    for (int i = 0; i < materialNamesArray.size(); i++) {
                        materials[i] = Material.valueOf(materialNamesArray.get(i).getAsString().toUpperCase());
                    }

                    RecipeChoice.MaterialChoice materialChoice = new RecipeChoice.MaterialChoice(materials);
                    recipe.setIngredient(ingredientChar, materialChoice);
                } else {
                    // Single material choice as string
                    Material material = Material.valueOf(ingredientElement.getAsString().toUpperCase());
                    recipe.setIngredient(ingredientChar, material);
                }
            }
        }

        return recipe;
    }
}