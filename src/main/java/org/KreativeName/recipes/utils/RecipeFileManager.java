package org.KreativeName.recipes.utils;

import com.google.gson.*;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class RecipeFileManager {
    private final JavaPlugin plugin;
    private final File recipeFile;
    private final Gson gson;

    public RecipeFileManager(JavaPlugin plugin) {
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

                sender.sendMessage(String.format("§e%d. §f%s §7- §f%dx %s",
                        i, type, resultCount, resultItem));
            }

            sender.sendMessage("§7Use /cr reload to apply any changes.");

        } catch (Exception e) {
            sender.sendMessage("§cFailed to list recipes: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private String readRecipeFile() throws IOException {
        if (!recipeFile.exists()) {
            throw new IOException("Recipe file not found: " + recipeFile.getAbsolutePath());
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
}
