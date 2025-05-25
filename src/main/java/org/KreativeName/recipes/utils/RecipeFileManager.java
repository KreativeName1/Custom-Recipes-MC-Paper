package org.KreativeName.recipes.utils;

import com.google.gson.*;
import org.KreativeName.recipes.Initialize;
import org.bukkit.NamespacedKey;
import org.bukkit.command.CommandSender;
import org.bukkit.inventory.Recipe;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class RecipeFileManager {
    private final Plugin plugin;
    private final File recipeFile;
    private final Gson gson;

    public RecipeFileManager(Plugin plugin) {
        this.plugin = plugin;
        this.recipeFile = new File(plugin.getDataFolder(), "recipes.json");
        this.gson = new GsonBuilder().setPrettyPrinting().create();
    }

    public void addRecipeToFile(JsonObject recipeJson) throws IOException {
        String jsonContent = readRecipeFile();
        JsonArray recipesArray = JsonParser.parseString(jsonContent).getAsJsonArray();
        recipesArray.add(recipeJson);




        try (FileWriter writer = new FileWriter(recipeFile, StandardCharsets.UTF_8)) {
            writer.write(gson.toJson(recipesArray));
        }
    }

    public void removeRecipeByIndex(CommandSender sender, int index) {
        try {
            String jsonContent = readRecipeFile();
            JsonArray recipesArray = JsonParser.parseString(jsonContent).getAsJsonArray();

            if (index < 0 || index >= recipesArray.size()) {
                sender.sendMessage("§cInvalid recipe index. Use /cr list to see available recipes.");
                return;
            }

            JsonElement removedRecipe = recipesArray.remove(index);
            String removedType = removedRecipe.getAsJsonObject().get("type").getAsString();

            try (FileWriter writer = new FileWriter(recipeFile, StandardCharsets.UTF_8)) {
                writer.write(gson.toJson(recipesArray));
            }

            Initialize.unregisteredRecipes.put(
                    Initialize.registeredRecipes.keySet().toArray(new NamespacedKey[0])[index],
                    Initialize.registeredRecipes.get(Initialize.registeredRecipes.keySet().toArray(new NamespacedKey[0])[index])
            );

            sender.sendMessage("§aSuccessfully removed " + removedType + " recipe at index " + index);
            sender.sendMessage("§7Use /cr reload to apply changes.");

        } catch (Exception e) {
            sender.sendMessage("§cFailed to remove recipe: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public List<JsonObject> getRecipes() throws IOException {
        String jsonContent = readRecipeFile();
        List<JsonObject> recipesList = new Gson().fromJson(jsonContent, new com.google.gson.reflect.TypeToken<List<JsonObject>>(){}.getType());
        return recipesList;
    }

    public void listRecipes(CommandSender sender) {
        try {
            String jsonContent = readRecipeFile();
            JsonArray recipesArray = JsonParser.parseString(jsonContent).getAsJsonArray();

            sender.sendMessage("§a=== Custom Recipes ===");

            for (int i = 0; i < recipesArray.size(); i++) {
                JsonObject recipe = recipesArray.get(i).getAsJsonObject();
                String type = recipe.get("type").getAsString();
                JsonObject result = recipe.getAsJsonObject("result");
                String resultItem = result.get("item").getAsString();
                int resultCount = result.get("count").getAsInt();
                String keyString = recipe.get("key").getAsString();

                boolean isLoaded = false;

                if ("CookingRecipe".equals(type) && recipe.has("cookingTypes")) {
                    JsonArray cookingTypes = recipe.getAsJsonArray("cookingTypes");
                    StringBuilder typesStr = new StringBuilder("(");

                    for (int j = 0; j < cookingTypes.size(); j++) {
                        JsonObject cookingType = cookingTypes.get(j).getAsJsonObject();
                        String cookType = cookingType.get("type").getAsString();
                        cookType = cookType.replace("Recipe", "");

                        // Check if cooking recipe is loaded (with type suffix)
                        NamespacedKey cookingKey = new NamespacedKey(plugin.getName().toLowerCase(),
                                keyString + "_" + cookType.toLowerCase());
                        if (org.KreativeName.recipes.Initialize.registeredRecipes.containsKey(cookingKey)) {
                            isLoaded = true;
                        }


                        typesStr.append(cookType.toLowerCase());
                        if (j < cookingTypes.size() - 1) {
                            typesStr.append(",");
                        }
                    }
                    typesStr.append(")");

                    String colorPrefix = isLoaded ? "§f" : "§c"; // White if loaded, red if not
                    sender.sendMessage(String.format("§e%d. %s%s %s §7- %s%dx %s",
                            i, colorPrefix, type, typesStr, colorPrefix, resultCount, resultItem));
                } else {
                    // For non-cooking recipes, check the key directly
                    NamespacedKey key = new NamespacedKey(plugin.getName().toLowerCase(), keyString);
                    isLoaded = org.KreativeName.recipes.Initialize.registeredRecipes.containsKey(key);

                    String colorPrefix = isLoaded ? "§f" : "§c"; // White if loaded, red if not
                    sender.sendMessage(String.format("§e%d. %s%s §7- %s%dx %s",
                            i, colorPrefix, type, colorPrefix, resultCount, resultItem));
                }


            }
        if (!Initialize.unregisteredRecipes.isEmpty()) {
            for (NamespacedKey key : Initialize.unregisteredRecipes.keySet()) {
                Recipe recipe = Initialize.unregisteredRecipes.get(key);
                String type = getTypeFromRecipe(recipe);
                String resultItem = recipe.getResult().getType().name();
                int resultCount = recipe.getResult().getAmount();
                sender.sendMessage(String.format("§7- %s - %dx %s",
                        type, resultCount, resultItem));
            }
        }

        sender.sendMessage("");
        sender.sendMessage("§7Legend: §fLoaded, §cNot Loaded, §7Removed, still loaded");
        sender.sendMessage("§7Use /cr reload to apply any changes.");

        } catch (Exception e) {
            sender.sendMessage("§cFailed to list recipes: " + e.getMessage());
            e.printStackTrace();
        }
    }
    public String readRecipeFile() throws IOException {
        if (!recipeFile.exists()) {
            recipeFile.getParentFile().mkdirs();
            try (FileWriter writer = new FileWriter(recipeFile, StandardCharsets.UTF_8)) {
                writer.write("[]");
            }
            return "[]";
        }

        try (FileReader reader = new FileReader(recipeFile, StandardCharsets.UTF_8)) {
            StringBuilder content = new StringBuilder();
            char[] buffer = new char[1024];
            int bytesRead;

            while ((bytesRead = reader.read(buffer)) != -1) {
                content.append(buffer, 0, bytesRead);
            }

            return content.toString();
        }
    }


    public int getRecipeCount() throws IOException {
        String jsonContent = readRecipeFile();
        JsonArray recipesArray = JsonParser.parseString(jsonContent).getAsJsonArray();
        int totalCount = 0;

        for (int i = 0; i < recipesArray.size(); i++) {
            JsonObject recipe = recipesArray.get(i).getAsJsonObject();
            String type = recipe.get("type").getAsString();

            if ("CookingRecipe".equals(type) && recipe.has("cookingTypes")) {
                JsonArray cookingTypes = recipe.getAsJsonArray("cookingTypes");
                totalCount += cookingTypes.size();
            } else {
                totalCount++;
            }
        }

        return totalCount;
    }


    private String getTypeFromRecipe(Recipe recipe) {
        if (recipe instanceof org.bukkit.inventory.CookingRecipe) {
            return "CookingRecipe";
        } else if (recipe instanceof org.bukkit.inventory.ShapedRecipe) {
            return "ShapedRecipe";
        } else if (recipe instanceof org.bukkit.inventory.ShapelessRecipe) {
            return "ShapelessRecipe";
        } else if (recipe instanceof org.bukkit.inventory.MerchantRecipe) {
            return "MerchantRecipe";
        } else if (recipe instanceof org.bukkit.inventory.RecipeChoice) {
            return "RecipeChoice";
        }
        return "UnknownRecipeType";
    }
}
