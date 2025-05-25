package org.KreativeName.recipes;

import com.google.gson.*;
import org.KreativeName.recipes.parsers.CookingRecipeParser;
import org.KreativeName.recipes.parsers.ShapedRecipeParser;
import org.KreativeName.recipes.parsers.ShapelessRecipeParser;
import org.KreativeName.recipes.parsers.StonecuttingRecipeParser;
import org.KreativeName.recipes.utils.RecipeFileManager;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Villager;
import org.bukkit.inventory.*;
import org.bukkit.plugin.Plugin;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.stream.Collectors;

public class RecipeLoader {
    private final Plugin plugin;
    private final Gson gson;

    public RecipeLoader(Plugin plugin) {
        this.plugin = plugin;
        this.gson = new Gson();
    }

    public Map<NamespacedKey, Recipe> loadRecipes() throws IOException {
        Map<NamespacedKey, Recipe> map = new HashMap<>();
        RecipeFileManager recipeFileManager = new RecipeFileManager(plugin);

        for (JsonObject recipeJson : recipeFileManager.getRecipes()) {
            if (!recipeJson.has("type")) {
                plugin.getLogger().log(Level.WARNING, "Recipe JSON does not have a 'type' field: " + recipeJson);
                continue;
            }

            String type = recipeJson.get("type").getAsString();
            String keyString = recipeJson.get("key").getAsString();
            NamespacedKey key = new NamespacedKey(plugin.getName().toLowerCase(), keyString);

            switch (type) {
                case "ShapedRecipe":
                    ShapedRecipe shapedRecipe = new ShapedRecipeParser().parse(recipeJson, key);
                    if (shapedRecipe != null) {
                        map.put(key, shapedRecipe);
                    } else {
                        plugin.getLogger().log(Level.WARNING, "Failed to parse ShapedRecipe: " + recipeJson);
                    }
                    break;
                case "ShapelessRecipe":
                    ShapelessRecipe shapelessRecipe = new ShapelessRecipeParser().parse(recipeJson, key);
                    if (shapelessRecipe != null) {
                        map.put(key, shapelessRecipe);
                    } else {
                        plugin.getLogger().log(Level.WARNING, "Failed to parse ShapelessRecipe: " + recipeJson);
                    }
                    break;
                case "CookingRecipe":
                    Map<NamespacedKey, Recipe> cookingRecipes = new CookingRecipeParser().parse(recipeJson, key);
                    if (cookingRecipes != null) {
                        map.putAll(cookingRecipes);
                    } else {
                        plugin.getLogger().log(Level.WARNING, "Failed to parse CookingRecipe: " + recipeJson);
                    }

                    break;
                case "StonecuttingRecipe":
                    StonecuttingRecipe stonecuttingRecipe = new StonecuttingRecipeParser().parse(recipeJson, key);
                    if (stonecuttingRecipe != null) {
                        map.put(key, stonecuttingRecipe);
                    } else {
                        plugin.getLogger().log(Level.WARNING, "Failed to parse StonecuttingRecipe: " + recipeJson);
                    }
                    break;
//                case "MerchantRecipe":
//                    MerchantRecipe merchantRecipe = new MerchantRecipeParser().parse(recipeJson, key);
//                    if (merchantRecipe != null) {
//                        map.put(key, merchantRecipe);
//                    } else {
//                        plugin.getLogger().log(Level.WARNING, "VillagerTrade recipe is missing required fields: " + recipeJson);
//                    }
//                    break;
                default:
                    plugin.getLogger().log(Level.WARNING, "Unknown recipe type: " + type);
            }
        }
        return map;
    }
}