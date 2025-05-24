package org.KreativeName.recipes.handlers;

import org.KreativeName.recipes.Initialize;
import org.KreativeName.recipes.RecipeLoader;
import org.KreativeName.recipes.classes.PluginRecipe;
import org.bukkit.NamespacedKey;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;

public class ReloadCommandHandler implements CommandHandler {
    private final JavaPlugin plugin;
    private final RecipeLoader recipeLoader;

    public ReloadCommandHandler(JavaPlugin plugin) {
        this.plugin = plugin;
        this.recipeLoader = new RecipeLoader(plugin);
    }

    @Override
    public void handle(CommandSender sender, String[] args) {
        if (!sender.hasPermission("recipes.reload")) {
            sender.sendMessage("§cYou don't have permission to reload recipes.");
            return;
        }

        try {
            // Clear existing recipes
            for (NamespacedKey key : Initialize.registeredRecipeKeys) {
                plugin.getServer().removeRecipe(key);
            }
            Initialize.registeredRecipeKeys.clear();

            // Load and register recipes
            List<PluginRecipe> recipes = recipeLoader.loadRecipes("recipes.json");
            Initialize.registeredRecipeKeys.addAll(recipeLoader.registerRecipes(recipes));

            sender.sendMessage("§aSuccessfully reloaded " + recipes.size() + " custom recipes!");

        } catch (Exception e) {
            sender.sendMessage("§cFailed to reload recipes: " + e.getMessage());
            e.printStackTrace();
        }
    }
}