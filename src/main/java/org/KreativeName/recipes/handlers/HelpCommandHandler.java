package org.KreativeName.recipes.handlers;

import org.bukkit.command.CommandSender;

public class HelpCommandHandler implements CommandHandler {
    @Override
    public void handle(CommandSender sender, String[] args) {
        sender.sendMessage("§a=== Custom Recipes Commands ===");
        sender.sendMessage("§e/cr add shaped §7- Add a shaped recipe");
        sender.sendMessage("§e/cr add shapeless §7- Add a shapeless recipe");
        sender.sendMessage("§e/cr add cooking §7- Add a cooking recipe");
        sender.sendMessage("§e/cr add stonecutting §7- Add a stonecutting recipe");
        sender.sendMessage("§e/cr add merchant §7- Add a merchant recipe");
        sender.sendMessage("§e/cr remove <index> §7- Remove a recipe by index");
        sender.sendMessage("§e/cr list §7- List all custom recipes");
        sender.sendMessage("§e/cr reload §7- Reload recipes from file");
    }
}
