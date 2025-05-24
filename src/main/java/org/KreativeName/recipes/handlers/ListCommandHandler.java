package org.KreativeName.recipes.handlers;

import org.KreativeName.recipes.utils.RecipeFileManager;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

public class ListCommandHandler implements CommandHandler {
    private final RecipeFileManager fileManager;

    public ListCommandHandler(JavaPlugin plugin) {
        this.fileManager = new RecipeFileManager(plugin);
    }

    @Override
    public void handle(CommandSender sender, String[] args) {
        if (!sender.hasPermission("recipes.list")) {
            sender.sendMessage("§cYou don't have permission to list recipes.");
            return;
        }

        fileManager.listRecipes(sender);
    }
}