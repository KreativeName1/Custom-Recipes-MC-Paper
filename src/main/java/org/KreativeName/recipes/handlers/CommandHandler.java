package org.KreativeName.recipes.handlers;

import org.bukkit.command.CommandSender;

public interface CommandHandler {
    void handle(CommandSender sender, String[] args);
}
