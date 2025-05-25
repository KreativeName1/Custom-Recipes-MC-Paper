package org.KreativeName.recipes.handlers;


import org.KreativeName.recipes.Initialize;
import org.KreativeName.recipes.utils.RecipeFileManager;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

public class RemoveCommandHandler implements CommandHandler {
    private final RecipeFileManager fileManager;

    public RemoveCommandHandler(JavaPlugin plugin) {
        this.fileManager = new RecipeFileManager(plugin);
    }

    @Override
    public void handle(CommandSender sender, String[] args) {
        if (!sender.hasPermission("recipes.remove")) {
            sender.sendMessage("§cYou don't have permission to remove recipes.");
            return;
        }

        if (args.length < 2) {
            sender.sendMessage("§cUsage: /cr remove <index>");
            return;
        }

        try {
            int index = Integer.parseInt(args[1]);

            fileManager.removeRecipeByIndex(sender, index);
        } catch (NumberFormatException e) {
            sender.sendMessage("§cIndex must be a number.");
        }
    }
}