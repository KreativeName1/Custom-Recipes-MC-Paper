package org.KreativeName.recipes.parsers;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.RecipeChoice;
import org.bukkit.inventory.ShapelessRecipe;
import org.bukkit.inventory.StonecuttingRecipe;

public class ShapelessRecipeParser {

    public ShapelessRecipe parse(JsonObject recipeJson, NamespacedKey key) {
        if (!recipeJson.has("type") || !recipeJson.get("type").getAsString().equals("ShapelessRecipe")) {
            return null;
        }

        JsonObject result = recipeJson.getAsJsonObject("result");
        String item = result.get("item").getAsString();
        int count = result.has("count") ? result.get("count").getAsInt() : 1;
        ItemStack itemStack = new ItemStack(Material.matchMaterial(item), count);
        ShapelessRecipe recipe = new ShapelessRecipe(key, itemStack);

        // Parse ingredients
        JsonArray ingredientsArray = recipeJson.getAsJsonArray("ingredients");
        for (JsonElement ingredientElement : ingredientsArray) {
            JsonObject ingredientJson = ingredientElement.getAsJsonObject();
            int ingredientCount = ingredientJson.has("count") ? ingredientJson.get("count").getAsInt() : 1;

            if (ingredientJson.has("choices")) {
                // Handle multiple choices
                JsonArray choicesArray = ingredientJson.getAsJsonArray("choices");
                Material[] materials = new Material[choicesArray.size()];

                for (int i = 0; i < choicesArray.size(); i++) {
                    String choiceItem = choicesArray.get(i).getAsString();
                    materials[i] = Material.matchMaterial(choiceItem);
                }

                RecipeChoice.MaterialChoice materialChoice = new RecipeChoice.MaterialChoice(materials);

                // Add the ingredient multiple times based on count
                for (int i = 0; i < ingredientCount; i++) {
                    recipe.addIngredient(materialChoice);
                }
            } else if (ingredientJson.has("item")) {
                // Handle single item
                String ingredientItem = ingredientJson.get("item").getAsString();
                Material material = Material.matchMaterial(ingredientItem);

                if (material != null) {
                    recipe.addIngredient(ingredientCount, material);
                }
            }
        }

        return recipe;
    }
}
/*
 {
    "type": "ShapelessRecipe",
    "result": {
      "item": "dirt",
      "count": 5
    },
    "ingredients": [
      {
        "count": 1,
        "choices": ["sand", "red_sand"]
      },
      {
        "count": 1,
        "choices": ["gravel"]
      }
    ]
  },

    private void registerShapelessRecipe(NamespacedKey key, PluginShapelessRecipe recipe) {
        ShapelessRecipe bukkitRecipe = new ShapelessRecipe(
                key,
                new ItemStack(
                        Material.valueOf(recipe.Result.Item.toUpperCase()),
                        recipe.Result.Count
                )
        );

        // Add ingredients
        for (PluginIngredient ingredient : recipe.Ingredients) {
            if (ingredient.Choices.length == 1) {
                // Single material choice
                Material material = Material.valueOf(ingredient.Choices[0].toUpperCase());
                bukkitRecipe.addIngredient(ingredient.Count, material);
            } else {
                // Multiple material choices
                Material[] materials = new Material[ingredient.Choices.length];
                for (int i = 0; i < ingredient.Choices.length; i++) {
                    materials[i] = Material.valueOf(ingredient.Choices[i].toUpperCase());
                }

                RecipeChoice.MaterialChoice materialChoice =
                        new RecipeChoice.MaterialChoice(materials);

                // Add the ingredient multiple times based on count
                for (int i = 0; i < ingredient.Count; i++) {
                    bukkitRecipe.addIngredient(materialChoice);
                }
            }
        }

        plugin.getServer().addRecipe(bukkitRecipe);
    }

     private PluginShapelessRecipe parseShapelessRecipe(JsonObject recipeJson, PluginRecipeResult result) {
        PluginShapelessRecipe recipe = new PluginShapelessRecipe();
        recipe.Type = "ShapelessRecipe";
        recipe.Result = result;

        // Parse ingredients
        JsonArray ingredientsArray = recipeJson.getAsJsonArray("ingredients");
        List<PluginIngredient> ingredients = new ArrayList<>();

        for (JsonElement ingredientElement : ingredientsArray) {
            JsonObject ingredientJson = ingredientElement.getAsJsonObject();
            PluginIngredient ingredient = new PluginIngredient();

            ingredient.Count = ingredientJson.get("count").getAsInt();

            if (ingredientJson.has("choices")) {
                JsonArray choicesArray = ingredientJson.getAsJsonArray("choices");
                String[] choices = new String[choicesArray.size()];

                for (int i = 0; i < choicesArray.size(); i++) {
                    choices[i] = choicesArray.get(i).getAsString();
                }

                ingredient.Choices = choices;
            } else if (ingredientJson.has("item")) {
                // Handle single item case
                String item = ingredientJson.get("item").getAsString();
                ingredient.Choices = new String[]{item};
            }

            ingredients.add(ingredient);
        }

        recipe.Ingredients = ingredients;
        return recipe;
    }
 */
