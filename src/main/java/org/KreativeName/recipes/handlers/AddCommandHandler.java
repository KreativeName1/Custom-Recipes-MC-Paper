package org.KreativeName.recipes.handlers;

import org.KreativeName.recipes.builders.*;
import org.KreativeName.recipes.utils.RecipeFileManager;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

public class AddCommandHandler implements CommandHandler {
    private final JavaPlugin plugin;
    private final ShapedRecipeBuilder shapedBuilder;
    private final ShapelessRecipeBuilder shapelessBuilder;
    private final CookingRecipeBuilder cookingBuilder;
    private final StonecuttingRecipeBuilder stonecuttingBuilder;
    private final MerchantRecipeBuilder merchantBuilder;

    public AddCommandHandler(JavaPlugin plugin) {
        this.plugin = plugin;
        this.shapedBuilder = new ShapedRecipeBuilder(plugin);
        this.shapelessBuilder = new ShapelessRecipeBuilder(plugin);
        this.cookingBuilder = new CookingRecipeBuilder(plugin);
        this.stonecuttingBuilder = new StonecuttingRecipeBuilder(plugin);
        this.merchantBuilder = new MerchantRecipeBuilder(plugin);
    }

    @Override
    public void handle(CommandSender sender, String[] args) {
        if (!sender.hasPermission("recipes.add")) {
            sender.sendMessage("§cYou don't have permission to add recipes.");
            return;
        }

        if (args.length < 2) {
            sender.sendMessage("§cUsage: /cr add <recipe_type> [options...]");
            return;
        }

        String recipeType = args[1].toLowerCase();

        switch (recipeType) {
            case "shaped":
                shapedBuilder.build(sender, args);
                break;
            case "shapeless":
                shapelessBuilder.build(sender, args);
                break;
            case "cooking":
                cookingBuilder.build(sender, args);
                break;
            case "stonecutting":
                stonecuttingBuilder.build(sender, args);
                break;
            case "merchant":
                merchantBuilder.build(sender, args);
                break;
            default:
                sender.sendMessage("§cUnknown recipe type. Available types: shaped, shapeless, cooking, stonecutting, merchant");
                break;
        }
    }
}
