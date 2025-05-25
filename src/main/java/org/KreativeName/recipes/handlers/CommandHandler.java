package org.KreativeName.recipes.handlers;

import org.bukkit.command.CommandSender;

public interface CommandHandler {
    public void handle(CommandSender sender, String[] args);
}
