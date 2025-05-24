package org.KreativeName.recipes.builders;

import org.bukkit.command.CommandSender;

public interface RecipeBuilder {
    void build(CommandSender sender, String[] args);
}
