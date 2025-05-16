package org.KreativeName.recipes;

import org.bukkit.NamespacedKey;
import org.bukkit.command.CommandMap;
import org.bukkit.inventory.Recipe;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


public final class Initialize extends JavaPlugin {
    public static List<NamespacedKey> registeredRecipeKeys = new ArrayList<>();
    private CommandMap commandMap;

    @Override
    public void onEnable() {
        try {
            try {
            RecipeLoader recipeLoader = new RecipeLoader(this);
            List<PluginRecipe> recipes = recipeLoader.loadRecipes("recipes.json");
            registeredRecipeKeys.addAll(recipeLoader.registerRecipes(recipes));
            getLogger().info("Loaded " + recipes.size() + " custom recipes");
            } catch (Exception e) {
                getLogger().severe("Failed to load recipes: " + e.getMessage());
                e.printStackTrace();
            }
            commandMap = getServer().getCommandMap();
            commandMap.register("recipes", new ReloadCommand(this));
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
            commandMap.clearCommands();
        } catch (Exception e) {
            getLogger().severe("An error occurred during plugin disable: " + e.getMessage());
            e.printStackTrace();
        }
    }
}