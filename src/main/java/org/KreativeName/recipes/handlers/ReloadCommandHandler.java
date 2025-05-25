package org.KreativeName.recipes.handlers;

import org.KreativeName.recipes.Initialize;
import org.KreativeName.recipes.RecipeLoader;
import org.KreativeName.recipes.utils.RecipeFileManager;
import org.bukkit.NamespacedKey;
import org.bukkit.command.CommandSender;
import org.bukkit.inventory.Recipe;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;
import java.util.Map;

public class ReloadCommandHandler implements CommandHandler {
    private final JavaPlugin plugin;
    private final RecipeLoader recipeLoader;
    private final RecipeFileManager recipeFileManager;

    public ReloadCommandHandler(JavaPlugin plugin) {
        this.plugin = plugin;
        this.recipeLoader = new RecipeLoader(plugin);
        this.recipeFileManager = new RecipeFileManager(plugin);
    }

    @Override
    public void handle(CommandSender sender, String[] args) {
        if (!sender.hasPermission("recipes.reload")) {
            sender.sendMessage("§cYou don't have permission to reload recipes.");
            return;
        }

        try {
            // Clear existing recipes
            for (NamespacedKey key : Initialize.registeredRecipes.keySet()) {
                plugin.getServer().removeRecipe(key);
            }
            Initialize.registeredRecipes.clear();

            // get amount of recipes inside the file

            // Load and register recipes
            Map<NamespacedKey, Recipe> recipes = recipeLoader.loadRecipes();
            Initialize.registeredRecipes = recipes;

            int loadedCount = recipes.size();
            int totalCount = recipeFileManager.getRecipeCount();

            for (NamespacedKey key : recipes.keySet()) {
                plugin.getServer().addRecipe(recipes.get(key));
                plugin.getLogger().info("Registered recipe: " + key);
            }

            sender.sendMessage("§aSuccessfully reloaded recipes. Loaded " + loadedCount + " out of " + totalCount + " recipes.");

        } catch (Exception e) {
            sender.sendMessage("§cFailed to reload recipes: " + e.getMessage());
            e.printStackTrace();
        }
    }
}