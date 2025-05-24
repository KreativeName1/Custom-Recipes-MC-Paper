package org.KreativeName.recipes.utils;

import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Villager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class TabCompletionHelper {

    public List<String> getCompletions(CommandSender sender, String[] args) {
        if (args.length == 1) {
            return filterCompletions(Arrays.asList("add", "remove", "list", "reload", "help"), args[0]);
        } else if (args.length == 2 && args[0].equalsIgnoreCase("add")) {
            return filterCompletions(Arrays.asList("shaped", "shapeless", "cooking", "stonecutting", "merchant"), args[1]);
        } else if (args.length >= 3 && args[0].equalsIgnoreCase("add")) {
            return getRecipeSpecificCompletions(args);
        }

        return new ArrayList<>();
    }

    private List<String> getRecipeSpecificCompletions(String[] args) {
        String recipeType = args[1].toLowerCase();

        // Material suggestions for result items
        if ((recipeType.equals("shaped") && args.length >= 3 && args.length <= 4) ||
                (recipeType.equals("shapeless") && args.length == 3) ||
                (recipeType.equals("cooking") && args.length == 3) ||
                (recipeType.equals("stonecutting") && args.length == 3) ||
                (recipeType.equals("merchant") && args.length == 3)) {
            return filterCompletions(getMaterialNames(), args[args.length - 1]);
        }

        // Profession suggestions for merchant recipes
        if (recipeType.equals("merchant") && args.length == 9) {
            return filterCompletions(getVillagerProfessions(), args[8]);
        }

        // Cooking type suggestions
        if (recipeType.equals("cooking") && args.length == 6) {
            return filterCompletions(Arrays.asList("furnace", "blasting", "smoking", "campfire"), args[5]);
        }

        return new ArrayList<>();
    }

    private List<String> filterCompletions(List<String> completions, String prefix) {
        return completions.stream()
                .filter(s -> s.toLowerCase().startsWith(prefix.toLowerCase()))
                .collect(Collectors.toList());
    }

    private List<String> getMaterialNames() {
        return Stream.of(Material.values())
                .filter(Material::isItem)
                .map(material -> material.name().toLowerCase())
                .collect(Collectors.toList());
    }

    private List<String> getVillagerProfessions() {
        return Stream.of(Villager.Profession.values())
                .map(profession -> profession.name().toLowerCase())
                .collect(Collectors.toList());
    }
}
