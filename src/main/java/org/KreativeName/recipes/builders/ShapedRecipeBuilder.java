package org.KreativeName.recipes.builders;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.KreativeName.recipes.utils.RecipeFileManager;
import org.KreativeName.recipes.utils.MaterialValidator;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;

public class ShapedRecipeBuilder implements RecipeBuilder {
    private final RecipeFileManager fileManager;
    private final MaterialValidator validator;

    public ShapedRecipeBuilder(JavaPlugin plugin) {
        this.fileManager = new RecipeFileManager(plugin);
        this.validator = new MaterialValidator();
    }

    @Override
    public void build(CommandSender sender, String[] args) {
        if (args.length < 8) {
            sender.sendMessage("§cUsage: /cr add shaped <result_item> <result_count> <pattern_line1> <pattern_line2> <pattern_line3> <key1> <material1> [<key2> <material2>...]");
            return;
        }

        try {
            String resultItem = args[2].toUpperCase();
            if (!validator.isValidMaterial(resultItem)) {
                sender.sendMessage("§cInvalid result material: " + resultItem);
                return;
            }

            int resultCount = Integer.parseInt(args[3]);
            if (!validator.isValidCount(resultCount)) {
                sender.sendMessage("§cResult count must be between 1 and 64.");
                return;
            }

            String[] patternLines = {args[4], args[5], args[6]};
            if (!validator.isValidPattern(patternLines)) {
                sender.sendMessage("§cPattern lines cannot be longer than 3 characters.");
                return;
            }

            Map<Character, List<String>> ingredients = parseIngredients(args, 7, sender);
            if (ingredients == null) return;

            if (!validator.validatePatternKeys(patternLines, ingredients.keySet())) {
                sender.sendMessage("§cAll keys in pattern must be defined.");
                return;
            }

            JsonObject recipeJson = createShapedRecipeJson(resultItem, resultCount, patternLines, ingredients);
            fileManager.addRecipeToFile(recipeJson);

            sender.sendMessage("§aShaped recipe added successfully! Use /cr reload to apply changes.");

        } catch (Exception e) {
            sender.sendMessage("§cAn error occurred: " + e.getMessage());
        }
    }

    private Map<Character, List<String>> parseIngredients(String[] args, int startIndex, CommandSender sender) {
        Map<Character, List<String>> ingredients = new HashMap<>();

        for (int i = startIndex; i < args.length; i += 2) {
            if (i + 1 >= args.length) {
                sender.sendMessage("§cEach key must have at least one material.");
                return null;
            }

            char key = args[i].charAt(0);
            String materialStr = args[i + 1];
            String[] materials = materialStr.split(",");

            List<String> validMaterials = new ArrayList<>();
            for (String mat : materials) {
                String upperMat = mat.toUpperCase();
                if (!validator.isValidMaterial(upperMat)) {
                    sender.sendMessage("§cInvalid material: " + mat);
                    return null;
                }
                validMaterials.add(upperMat);
            }

            ingredients.put(key, validMaterials);
        }

        return ingredients;
    }

    private JsonObject createShapedRecipeJson(String resultItem, int resultCount,
                                              String[] patternLines, Map<Character, List<String>> ingredients) {
        JsonObject recipeJson = new JsonObject();
        recipeJson.addProperty("type", "ShapedRecipe");

        JsonObject resultJson = new JsonObject();
        resultJson.addProperty("item", resultItem.toLowerCase());
        resultJson.addProperty("count", resultCount);
        recipeJson.add("result", resultJson);

        JsonArray patternArray = new JsonArray();
        for (String line : patternLines) {
            patternArray.add(line);
        }
        recipeJson.add("pattern", patternArray);

        JsonObject replaceJson = new JsonObject();
        for (Map.Entry<Character, List<String>> entry : ingredients.entrySet()) {
            char key = entry.getKey();
            List<String> materials = entry.getValue();

            if (materials.size() == 1) {
                replaceJson.addProperty(String.valueOf(key), materials.get(0).toLowerCase());
            } else {
                JsonArray materialsArray = new JsonArray();
                for (String material : materials) {
                    materialsArray.add(material.toLowerCase());
                }
                replaceJson.add(String.valueOf(key), materialsArray);
            }
        }
        recipeJson.add("replace", replaceJson);

        return recipeJson;
    }
}