package org.KreativeName.recipes.utils;

import com.google.gson.*;
import org.bukkit.command.CommandSender;
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

                if ("CookingRecipe".equals(type) && recipe.has("cookingTypes")) {
                    JsonArray cookingTypes = recipe.getAsJsonArray("cookingTypes");
                    StringBuilder typesStr = new StringBuilder("(");

                    for (int j = 0; j < cookingTypes.size(); j++) {
                        JsonObject cookingType = cookingTypes.get(j).getAsJsonObject();
                        String cookType = cookingType.get("type").getAsString();
                        cookType = cookType.replace("Recipe", "");

                        typesStr.append(cookType.toLowerCase());
                        if (j < cookingTypes.size() - 1) {
                            typesStr.append(",");
                        }
                    }
                    typesStr.append(")");

                    sender.sendMessage(String.format("§e%d. §f%s %s §7- §f%dx %s",
                            i, type, typesStr, resultCount, resultItem));
                } else {
                    sender.sendMessage(String.format("§e%d. §f%s §7- §f%dx %s",
                            i, type, resultCount, resultItem));
                }
            }

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
}
