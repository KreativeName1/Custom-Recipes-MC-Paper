package org.KreativeName.recipes.classes;

/**
 * Base class for all recipe types
 */
public class PluginRecipe {
    public String Type;
    public PluginRecipeResult Result;
    
    public PluginRecipe() {
        // Default constructor
    }
    
    public PluginRecipe(String type, PluginRecipeResult result) {
        this.Type = type;
        this.Result = result;
    }
}