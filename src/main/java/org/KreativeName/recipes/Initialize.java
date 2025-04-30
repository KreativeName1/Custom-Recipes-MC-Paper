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
        RecipeLoader recipeLoader = new RecipeLoader(this);
        List<PluginRecipe> recipes = recipeLoader.loadRecipes("recipes.json");
        registeredRecipeKeys.addAll(recipeLoader.registerRecipes(recipes));
        getLogger().info("Loaded " + recipes.size() + " custom recipes");
        commandMap = getServer().getCommandMap();
        commandMap.register("recipes", new ReloadCommand(this));

    }
    
    @Override
    public void onDisable() {
        for (NamespacedKey key : registeredRecipeKeys) {
            getServer().removeRecipe(key);
        }
        getLogger().info("Removed " + registeredRecipeKeys.size() + " custom recipes");
        registeredRecipeKeys.clear();
    }
}