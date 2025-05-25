package org.KreativeName.recipes;

import org.bukkit.NamespacedKey;
import org.bukkit.command.CommandMap;
import org.bukkit.inventory.Recipe;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public final class Initialize extends JavaPlugin {
    public static Map<NamespacedKey, Recipe> registeredRecipes = new HashMap<>();
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
                Map<NamespacedKey, Recipe> map = recipeLoader.loadRecipes();
                for (Map.Entry<NamespacedKey, Recipe> entry : map.entrySet()) {
                    this.getLogger().info("Registering recipe: " + entry.getKey());
                    getServer().addRecipe(entry.getValue());
                    registeredRecipes.put(entry.getKey(), entry.getValue());
                }
                getLogger().info("Loaded " + map.size() + " custom recipes");
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
            for (NamespacedKey key : registeredRecipes.keySet()) {
                getServer().removeRecipe(key);
            }
            getLogger().info("Removed " + registeredRecipes.keySet().size() + " custom recipes");
            registeredRecipes.clear();

            if (commandMap != null) {
                commandMap.getKnownCommands().remove("customrecipe");
                getLogger().info("Unregistered customrecipe command");
            }

            getLogger().info("Custom Recipes plugin disabled successfully");

        } catch (Exception e) {
            getLogger().severe("An error occurred during plugin disable: " + e.getMessage());
            e.printStackTrace();
        }
    }
}