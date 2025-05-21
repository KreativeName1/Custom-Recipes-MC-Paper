package org.KreativeName.recipes;

import org.KreativeName.recipes.classes.PluginRecipe;
import org.bukkit.NamespacedKey;
import org.bukkit.command.CommandMap;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.ArrayList;
import java.util.List;


public final class Initialize extends JavaPlugin {
    public static List<NamespacedKey> registeredRecipeKeys = new ArrayList<>();
    private CommandMap commandMap;

    @Override
    public void onEnable() {
        try {
            if (!getDataFolder().exists()) {
                getDataFolder().mkdir();
                getLogger().info("Created data folder");
            } else {
                getLogger().info("Data folder found");
            }

            File recipeFile = new File(getDataFolder(), "recipes.json");
            if (!recipeFile.exists()) {
                saveResource("recipes.json", false);
                getLogger().info("Created default recipes.json file");
            }
            else {
                getLogger().info("Found existing recipes.json file");
            }

            try {
                RecipeLoader recipeLoader = new RecipeLoader(this);
                List<PluginRecipe> recipes = recipeLoader.loadRecipes("recipes.json");
                registeredRecipeKeys.addAll(recipeLoader.registerRecipes(recipes));
                getLogger().info("Loaded " + recipes.size() + " custom recipes");
            } catch (Exception e) {
                getLogger().severe("Failed to load recipes: " + e.getMessage());
                e.printStackTrace();
            }

            getCommand("customrecipe").setExecutor(new RecipeCommands(this));
            getCommand("customrecipe").setTabCompleter(new RecipeCommands(this));

            getLogger().info("Custom Recipes plugin enabled successfully");
        } catch (Exception e) {
            getLogger().severe("An error occurred during plugin enable: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public void onDisable() {
        try {
            for (NamespacedKey key : registeredRecipeKeys) {
                getServer().removeRecipe(key);
            }
            getLogger().info("Removed " + registeredRecipeKeys.size() + " custom recipes");
            registeredRecipeKeys.clear();
        } catch (Exception e) {
            getLogger().severe("An error occurred during plugin disable: " + e.getMessage());
            e.printStackTrace();
        }
    }
}