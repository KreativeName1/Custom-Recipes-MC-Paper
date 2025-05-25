package org.KreativeName.recipes.parsers;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.RecipeChoice;
import org.bukkit.inventory.StonecuttingRecipe;

import java.util.ArrayList;
import java.util.List;

public class StonecuttingRecipeParser {

    public StonecuttingRecipe parse(JsonObject recipeJson, NamespacedKey key) {
        if (!recipeJson.has("type") || !recipeJson.get("type").getAsString().equals("StonecuttingRecipe")) {
            return null;
        }

        ItemStack resultItem = new ItemStack(
                Material.valueOf(recipeJson.getAsJsonObject("result").get("item").getAsString().toUpperCase()),
                recipeJson.getAsJsonObject("result").has("count") ? recipeJson.getAsJsonObject("result").get("count").getAsInt() : 1
        );

        RecipeChoice ingredientChoice;
        if (recipeJson.has("ingredient")) {
            JsonElement ingredientElement = recipeJson.get("ingredient");

            if (ingredientElement.isJsonObject()) {
                JsonObject ingredientObject = ingredientElement.getAsJsonObject();
                String itemName = ingredientObject.get("item").getAsString();
                ingredientChoice = new RecipeChoice.MaterialChoice(Material.valueOf(itemName.toUpperCase()));
            } else if (ingredientElement.isJsonArray()) {
                JsonArray ingredientArray = ingredientElement.getAsJsonArray();
                List<Material> materials = new ArrayList<>();

                for (JsonElement element : ingredientArray) {
                    materials.add(Material.valueOf(element.getAsString().toUpperCase()));
                }

                ingredientChoice = new RecipeChoice.MaterialChoice(materials);
            } else {
                return null;
            }
        } else {
            return null;
        }

        return new StonecuttingRecipe(key, resultItem, ingredientChoice);
    }
}