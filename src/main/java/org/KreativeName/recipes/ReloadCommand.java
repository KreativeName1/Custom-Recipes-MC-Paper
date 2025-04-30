package org.KreativeName.recipes;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class ReloadCommand extends Command {
    private final Initialize plugin;

    public ReloadCommand(Initialize plugin) {
        super("recipereload", "Reloads Recipes from JSON", "/recipereload", List.of("reciperl"));
        this.plugin = plugin;
    }

    @Override
    public boolean execute(@NotNull CommandSender commandSender, @NotNull String s, @NotNull String @NotNull [] args) {
        if (!commandSender.hasPermission("recipes.reload")) {
            commandSender.sendMessage(ChatColor.RED + "You don't have permission to use this command.");
            return true;
        }

        // Remove existing recipes
        for (org.bukkit.NamespacedKey key : plugin.registeredRecipeKeys) {
            plugin.getServer().removeRecipe(key);
        }
        plugin.registeredRecipeKeys.clear();

        // Load and register new recipes
        RecipeLoader recipeLoader = new RecipeLoader(plugin);
        List<PluginRecipe> recipes = recipeLoader.loadRecipes("recipes.json");
        plugin.registeredRecipeKeys.addAll(recipeLoader.registerRecipes(recipes));

        commandSender.sendMessage(ChatColor.GREEN + "Successfully reloaded " + recipes.size() + " custom recipes!");
        return true;
    }

    @Override
    public @NotNull List<String> tabComplete(
            @NotNull CommandSender sender, @NotNull String alias, @NotNull String @NotNull [] args)
            throws IllegalArgumentException {
        // No tab completions needed for this command
        return new ArrayList<>();
    }
}