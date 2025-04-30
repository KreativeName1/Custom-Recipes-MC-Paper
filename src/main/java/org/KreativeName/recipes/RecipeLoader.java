package org.KreativeName.recipes;

import com.google.gson.*;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Villager;
import org.bukkit.inventory.*;
import org.bukkit.plugin.Plugin;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.stream.Collectors;

public class RecipeLoader {
    private final Plugin plugin;
    private final Gson gson;

    public RecipeLoader(Plugin plugin) {
        this.plugin = plugin;
        this.gson = new Gson();
    }

    public List<PluginRecipe> loadRecipes(String filePath) {
        List<PluginRecipe> recipes = new ArrayList<>();

        // Create the file object pointing to the plugin's data folder
        File dataFolder = plugin.getDataFolder();
        if (!dataFolder.exists()) {
            dataFolder.mkdirs();
        }

        File recipeFile = new File(dataFolder, filePath);

        // If the file doesn't exist, copy the default one from the plugin resources
        if (!recipeFile.exists()) {
            try (InputStream input = plugin.getResource(filePath)) {
                if (input!= null) {
                    Files.copy(input, recipeFile.toPath());
                } else {
                    plugin.getLogger().severe("Could not find resource file: " + filePath);
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            plugin.getLogger().info("Created default recipes file at " + recipeFile.getAbsolutePath());
        }

        try {
            // Read the file content
            String jsonContent = readFile(recipeFile);
            JsonArray recipesArray = JsonParser.parseString(jsonContent).getAsJsonArray();

            for (JsonElement element : recipesArray) {
                JsonObject recipeJson = element.getAsJsonObject();
                String type = recipeJson.get("type").getAsString();

                PluginRecipe recipe = parseRecipe(type, recipeJson);
                if (recipe != null) {
                    recipes.add(recipe);
                }
            }
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to load recipes from " + recipeFile.getAbsolutePath(), e);
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "Error parsing recipes", e);
        }

        return recipes;
    }

    private String readFile(File file) throws IOException {
        try (BufferedReader reader = new BufferedReader(new FileReader(file, StandardCharsets.UTF_8))) {
            return reader.lines().collect(Collectors.joining("\n"));
        }
    }

    public List<NamespacedKey> registerRecipes(List<PluginRecipe> recipes) {
        List<NamespacedKey> registeredKeys = new ArrayList<>();
        for (int i = 0; i < recipes.size(); i++) {
            PluginRecipe recipe = recipes.get(i);
            NamespacedKey key = new NamespacedKey(plugin, "custom_recipe_" + i);

            try {
                if (recipe instanceof PluginShapedRecipe) {
                    registerShapedRecipe(key, (PluginShapedRecipe) recipe);
                    registeredKeys.add(key);
                } else if (recipe instanceof PluginShapelessRecipe) {
                    registerShapelessRecipe(key, (PluginShapelessRecipe) recipe);
                    registeredKeys.add(key);
                } else if (recipe instanceof PluginCookingRecipe) {
                    List<NamespacedKey> cookingKeys = registerCookingRecipe(key, (PluginCookingRecipe) recipe);
                    registeredKeys.addAll(cookingKeys);
                } else if (recipe instanceof PluginStonecuttingRecipe) {
                    registerStonecuttingRecipe(key, (PluginStonecuttingRecipe) recipe);
                    registeredKeys.add(key);
                } else if (recipe instanceof PluginMerchantRecipe) {
                    registerMerchantRecipe(key, (PluginMerchantRecipe) recipe);
                    registeredKeys.add(key);
                }
            } catch (Exception e) {
                plugin.getLogger().log(Level.SEVERE, "Failed to register recipe: " + recipe.Type, e);
            }
        }

        return registeredKeys;
    }

    private PluginRecipe parseRecipe(String type, JsonObject recipeJson) {
        PluginRecipeResult result = parseRecipeResult(recipeJson.getAsJsonObject("result"));

        switch (type) {
            case "ShapedRecipe":
                return parseShapedRecipe(recipeJson, result);
            case "ShapelessRecipe":
                return parseShapelessRecipe(recipeJson, result);
            case "CookingRecipe":
                return parseCookingRecipe(recipeJson, result);
            case "StonecuttingRecipe":
                return parseStonecuttingRecipe(recipeJson, result);
            case "MerchantRecipe":
                return parseMerchantRecipe(recipeJson, result);
            default:
                plugin.getLogger().warning("Unknown recipe type: " + type);
                return null;
        }
    }

    private PluginRecipeResult parseRecipeResult(JsonObject resultJson) {
        PluginRecipeResult result = new PluginRecipeResult();
        result.Item = resultJson.get("item").getAsString();
        result.Count = resultJson.get("count").getAsInt();
        return result;
    }

    private PluginShapedRecipe parseShapedRecipe(JsonObject recipeJson, PluginRecipeResult result) {
        PluginShapedRecipe recipe = new PluginShapedRecipe();
        recipe.Type = "ShapedRecipe";
        recipe.Result = result;

        // Parse pattern
        JsonArray patternArray = recipeJson.getAsJsonArray("pattern");
        List<String> pattern = new ArrayList<>();
        for (JsonElement patternElement : patternArray) {
            pattern.add(patternElement.getAsString());
        }
        recipe.Pattern = pattern;

        // Parse replace map
        JsonObject replaceJson = recipeJson.getAsJsonObject("replace");
        Map<Character, List<String>> replace = new HashMap<>();

        for (Map.Entry<String, JsonElement> entry : replaceJson.entrySet()) {
            char key = entry.getKey().charAt(0);
            List<String> materials = new ArrayList<>();

            if (entry.getValue().isJsonArray()) {
                for (JsonElement material : entry.getValue().getAsJsonArray()) {
                    materials.add(material.getAsString());
                }
            } else {
                materials.add(entry.getValue().getAsString());
            }

            replace.put(key, materials);
        }

        recipe.Replace = replace;
        return recipe;
    }

    private PluginShapelessRecipe parseShapelessRecipe(JsonObject recipeJson, PluginRecipeResult result) {
        PluginShapelessRecipe recipe = new PluginShapelessRecipe();
        recipe.Type = "ShapelessRecipe";
        recipe.Result = result;

        // Parse ingredients
        JsonArray ingredientsArray = recipeJson.getAsJsonArray("ingredients");
        List<PluginIngredient> ingredients = new ArrayList<>();

        for (JsonElement ingredientElement : ingredientsArray) {
            JsonObject ingredientJson = ingredientElement.getAsJsonObject();
            PluginIngredient ingredient = new PluginIngredient();

            ingredient.Count = ingredientJson.get("count").getAsInt();

            if (ingredientJson.has("choices")) {
                JsonArray choicesArray = ingredientJson.getAsJsonArray("choices");
                String[] choices = new String[choicesArray.size()];

                for (int i = 0; i < choicesArray.size(); i++) {
                    choices[i] = choicesArray.get(i).getAsString();
                }

                ingredient.Choices = choices;
            } else if (ingredientJson.has("item")) {
                // Handle single item case
                String item = ingredientJson.get("item").getAsString();
                ingredient.Choices = new String[]{item};
            }

            ingredients.add(ingredient);
        }

        recipe.Ingredients = ingredients;
        return recipe;
    }

    private PluginCookingRecipe parseCookingRecipe(JsonObject recipeJson, PluginRecipeResult result) {
        PluginCookingRecipe recipe = new PluginCookingRecipe();
        recipe.Type = "CookingRecipe";
        recipe.Result = result;

        // Parse cooking types
        JsonArray cookingTypesArray = recipeJson.getAsJsonArray("cookingTypes");
        List<CookingType> cookingTypes = new ArrayList<>();

        for (JsonElement typeElement : cookingTypesArray) {
            JsonObject typeJson = typeElement.getAsJsonObject();
            CookingType cookingType = new CookingType();

            cookingType.Type = typeJson.get("type").getAsString();
            cookingType.CookingTime = typeJson.get("cooking_time").getAsInt();

            cookingTypes.add(cookingType);
        }

        recipe.cookingTypes = cookingTypes;

        // Parse ingredient
        JsonArray ingredientArray = recipeJson.getAsJsonArray("ingredient");
        List<String> ingredients = new ArrayList<>();

        for (JsonElement ingredient : ingredientArray) {
            ingredients.add(ingredient.getAsString());
        }

        recipe.Ingredient = ingredients;

        // Parse exp reward
        if (recipeJson.has("exp_reward")) {
            recipe.expReward = recipeJson.get("exp_reward").getAsFloat();
        }

        return recipe;
    }

    private PluginStonecuttingRecipe parseStonecuttingRecipe(JsonObject recipeJson, PluginRecipeResult result) {
        PluginStonecuttingRecipe recipe = new PluginStonecuttingRecipe();
        recipe.Type = "StonecuttingRecipe";
        recipe.Result = result;

        // Parse ingredient
        JsonObject ingredientJson = recipeJson.getAsJsonObject("ingredient");

        if (ingredientJson.has("item")) {
            JsonElement itemElement = ingredientJson.get("item");
            if (itemElement.isJsonArray()) {
                JsonArray itemArray = itemElement.getAsJsonArray();
                String[] choices = new String[itemArray.size()];

                for (int i = 0; i < itemArray.size(); i++) {
                    choices[i] = itemArray.get(i).getAsString();
                }

                recipe.Ingredient = String.join(",", choices);
            } else {
                recipe.Ingredient = itemElement.getAsString();
            }
        }

        return recipe;
    }

    private PluginMerchantRecipe parseMerchantRecipe(JsonObject recipeJson, PluginRecipeResult result) {
        // Create a new class for merchant recipes since it's not in the provided snippets
        PluginMerchantRecipe recipe = new PluginMerchantRecipe();
        recipe.Type = "MerchantRecipe";
        recipe.Result = result;

        // Parse ingredients
        JsonArray ingredientsArray = recipeJson.getAsJsonArray("ingredients");
        List<PluginIngredient> ingredients = new ArrayList<>();

        for (JsonElement ingredientElement : ingredientsArray) {
            JsonObject ingredientJson = ingredientElement.getAsJsonObject();
            PluginIngredient ingredient = new PluginIngredient();

            ingredient.Count = ingredientJson.get("count").getAsInt();
            ingredient.Choices = new String[]{ingredientJson.get("item").getAsString()};

            ingredients.add(ingredient);
        }

        recipe.Ingredients = ingredients;

        // Parse additional merchant recipe properties
        if (recipeJson.has("max_uses")) {
            recipe.MaxUses = recipeJson.get("max_uses").getAsInt();
        }

        if (recipeJson.has("exp_reward")) {
            recipe.ExpReward = recipeJson.get("exp_reward").getAsBoolean();
        }

        if (recipeJson.has("villager_exp")) {
            recipe.VillagerExp = recipeJson.get("villager_exp").getAsInt();
        }

        if (recipeJson.has("priceMultiplier")) {
            recipe.PriceMultiplier = recipeJson.get("priceMultiplier").getAsFloat();
        }
        if (recipeJson.has("profession")) {
            recipe.Profession = getProfessionFromString(recipeJson.get("profession").getAsString());
        }

        if (recipeJson.has("demand")) {
            recipe.Demand = recipeJson.get("demand").getAsInt();
        }

        if (recipeJson.has("special_price")) {
            recipe.SpecialPrice = recipeJson.get("special_price").getAsInt();
        }

        return recipe;
    }

    private void registerShapedRecipe(NamespacedKey key, PluginShapedRecipe recipe) {
        ShapedRecipe bukkitRecipe = new ShapedRecipe(
                key,
                new ItemStack(
                        Material.valueOf(recipe.Result.Item.toUpperCase()),
                        recipe.Result.Count
                )
        );

        // Set pattern
        String[] pattern = recipe.Pattern.toArray(new String[0]);
        bukkitRecipe.shape(pattern);

        // Set ingredients
        for (Map.Entry<Character, List<String>> entry : recipe.Replace.entrySet()) {
            char ingredientChar = entry.getKey();
            List<String> materialNames = entry.getValue();

            if (materialNames.size() == 1) {
                // Single material choice
                Material material = Material.valueOf(materialNames.get(0).toUpperCase());
                bukkitRecipe.setIngredient(ingredientChar, material);
            } else {
                // Multiple material choices
                Material[] materials = new Material[materialNames.size()];
                for (int i = 0; i < materialNames.size(); i++) {
                    materials[i] = Material.valueOf(materialNames.get(i).toUpperCase());
                }

                RecipeChoice.MaterialChoice materialChoice =
                        new RecipeChoice.MaterialChoice(materials);
                bukkitRecipe.setIngredient(ingredientChar, materialChoice);
            }
        }

        plugin.getServer().addRecipe(bukkitRecipe);
    }

    private void registerShapelessRecipe(NamespacedKey key, PluginShapelessRecipe recipe) {
        ShapelessRecipe bukkitRecipe = new ShapelessRecipe(
                key,
                new ItemStack(
                        Material.valueOf(recipe.Result.Item.toUpperCase()),
                        recipe.Result.Count
                )
        );

        // Add ingredients
        for (PluginIngredient ingredient : recipe.Ingredients) {
            if (ingredient.Choices.length == 1) {
                // Single material choice
                Material material = Material.valueOf(ingredient.Choices[0].toUpperCase());
                bukkitRecipe.addIngredient(ingredient.Count, material);
            } else {
                // Multiple material choices
                Material[] materials = new Material[ingredient.Choices.length];
                for (int i = 0; i < ingredient.Choices.length; i++) {
                    materials[i] = Material.valueOf(ingredient.Choices[i].toUpperCase());
                }

                RecipeChoice.MaterialChoice materialChoice =
                        new RecipeChoice.MaterialChoice(materials);

                // Add the ingredient multiple times based on count
                for (int i = 0; i < ingredient.Count; i++) {
                    bukkitRecipe.addIngredient(materialChoice);
                }
            }
        }

        plugin.getServer().addRecipe(bukkitRecipe);
    }

    private List<NamespacedKey> registerCookingRecipe(NamespacedKey key, PluginCookingRecipe recipe) {
        List<NamespacedKey> cookingKeys = new ArrayList<>();

        // Create result item stack
        ItemStack resultStack = new ItemStack(
                Material.valueOf(recipe.Result.Item.toUpperCase()),
                recipe.Result.Count
        );

        // Create ingredient choice
        RecipeChoice ingredientChoice;

        if (recipe.Ingredient.size() == 1) {
            // Single ingredient
            Material material = Material.valueOf(recipe.Ingredient.get(0).toUpperCase());
            ingredientChoice = new RecipeChoice.MaterialChoice(material);
        } else {
            // Multiple ingredient choices
            Material[] materials = new Material[recipe.Ingredient.size()];
            for (int i = 0; i < recipe.Ingredient.size(); i++) {
                materials[i] = Material.valueOf(recipe.Ingredient.get(i).toUpperCase());
            }
            ingredientChoice = new RecipeChoice.MaterialChoice(materials);
        }

        // Register each cooking type
        for (CookingType cookingType : recipe.cookingTypes) {
            NamespacedKey cookingKey = new NamespacedKey(plugin, key.getKey() + "_" + cookingType.Type.toLowerCase());
            cookingKeys.add(cookingKey);

            switch (cookingType.Type) {
                case "FurnaceRecipe":
                    FurnaceRecipe furnaceRecipe = new FurnaceRecipe(
                            cookingKey,
                            resultStack,
                            ingredientChoice,
                            recipe.expReward,
                            cookingType.CookingTime
                    );
                    plugin.getServer().addRecipe(furnaceRecipe);
                    break;

                case "BlastingRecipe":
                    BlastingRecipe blastingRecipe = new BlastingRecipe(
                            cookingKey,
                            resultStack,
                            ingredientChoice,
                            recipe.expReward,
                            cookingType.CookingTime
                    );
                    plugin.getServer().addRecipe(blastingRecipe);
                    break;

                case "SmokingRecipe":
                    SmokingRecipe smokingRecipe = new SmokingRecipe(
                            cookingKey,
                            resultStack,
                            ingredientChoice,
                            recipe.expReward,
                            cookingType.CookingTime
                    );
                    plugin.getServer().addRecipe(smokingRecipe);
                    break;

                case "CampfireRecipe":
                    CampfireRecipe campfireRecipe = new CampfireRecipe(
                            cookingKey,
                            resultStack,
                            ingredientChoice,
                            recipe.expReward,
                            cookingType.CookingTime
                    );
                    plugin.getServer().addRecipe(campfireRecipe);
                    break;
            }
        }

        return cookingKeys;
    }

    private void registerMerchantRecipe(NamespacedKey key, PluginMerchantRecipe recipe) {
        // Create result item stack
        ItemStack resultStack = new ItemStack(
                Material.valueOf(recipe.Result.Item.toUpperCase()),
                recipe.Result.Count
        );

        MerchantRecipe merchantRecipe = new MerchantRecipe(
                resultStack,
                recipe.Uses,
                recipe.MaxUses,
                recipe.ExpReward,
                recipe.VillagerExp,
                recipe.PriceMultiplier,
                recipe.Demand,
                recipe.SpecialPrice
        );

        // set ingredients
        for (PluginIngredient ingredient : recipe.Ingredients) {
            ItemStack ingredientStack = new ItemStack(
                    Material.valueOf(ingredient.Choices[0].toUpperCase()),
                    ingredient.Count
            );
            merchantRecipe.addIngredient(ingredientStack);
        }

        // Add the recipe to villagers based on profession
        List<Villager> villagers = plugin.getServer().getWorlds().stream()
                .flatMap(world -> world.getEntitiesByClass(Villager.class).stream())
                .collect(Collectors.toList());

        // Filter villagers by profession if specified
        if (recipe.Profession != null) {
            villagers = villagers.stream()
                    .filter(villager -> villager.getProfession().equals(recipe.Profession))
                    .collect(Collectors.toList());

            plugin.getLogger().info("Adding merchant recipe to " + villagers.size() +
                    " villagers with profession: " + recipe.Profession);
        } else {
            plugin.getLogger().warning("No villager profession specified. Recipe won't be added to any villagers.");
        }

        // Add recipe to filtered villagers
        for (org.bukkit.entity.Villager villager : villagers) {
            // Get the current recipes
            List<MerchantRecipe> currentRecipes = new ArrayList<>(villager.getRecipes());
            // Add our new recipe
            currentRecipes.add(merchantRecipe);
            // Set the updated recipes list
            villager.setRecipes(currentRecipes);
        }
    }

    private void registerStonecuttingRecipe(NamespacedKey key, PluginStonecuttingRecipe recipe) {
        // Create result item stack
        ItemStack resultStack = new ItemStack(
                Material.valueOf(recipe.Result.Item.toUpperCase()),
                recipe.Result.Count
        );

        // Create ingredient choice
        RecipeChoice ingredientChoice;

        if (recipe.Ingredient.contains(",")) {
            // Multiple ingredient choices
            String[] ingredientChoices = recipe.Ingredient.split(",");
            Material[] materials = new Material[ingredientChoices.length];
            for (int i = 0; i < ingredientChoices.length; i++) {
                materials[i] = Material.valueOf(ingredientChoices[i].toUpperCase());
            }
            ingredientChoice = new RecipeChoice.MaterialChoice(materials);
        } else {
            // Single ingredient choice
            Material material = Material.valueOf(recipe.Ingredient.toUpperCase());
            ingredientChoice = new RecipeChoice.MaterialChoice(material);
        }

        StonecuttingRecipe stonecuttingRecipe = new StonecuttingRecipe(
                key,
                resultStack,
                ingredientChoice
        );

        plugin.getServer().addRecipe(stonecuttingRecipe);
    }


    private Villager.Profession getProfessionFromString(String professionString) {
        switch (professionString.toUpperCase()) {
            case "BUTCHER":
                return Villager.Profession.BUTCHER;
            case "FARMER":
                return Villager.Profession.FARMER;
            case "CARTOGRAPHER":
                return Villager.Profession.CARTOGRAPHER;
            case "ARMORER":
                return Villager.Profession.ARMORER;
            case "CLERIC":
                return Villager.Profession.CLERIC;
            case "FISHERMAN":
                return Villager.Profession.FISHERMAN;
            case "FLETCHER":
                return Villager.Profession.FLETCHER;
            case "LEATHERWORKER":
                return Villager.Profession.LEATHERWORKER;
            case "LIBRARIAN":
                return Villager.Profession.LIBRARIAN;
            case "MASON":
                return Villager.Profession.MASON;
            case "NITWIT":
                return Villager.Profession.NITWIT;
            case "NONE":
                return Villager.Profession.NONE;
            case "SHEPHERD":
                return Villager.Profession.SHEPHERD;
            case "TOOLSMITH":
                return Villager.Profession.TOOLSMITH;
            case "WEAPONSMITH":
                return Villager.Profession.WEAPONSMITH;

            default:
                return Villager.Profession.NONE;
        }
    }
}