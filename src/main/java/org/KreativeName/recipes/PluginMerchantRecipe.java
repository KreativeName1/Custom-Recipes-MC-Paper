package org.KreativeName.recipes;

import java.util.List;

/**
 * Represents a merchant (villager) trade recipe
 */
public class PluginMerchantRecipe extends PluginRecipe {
    public List<PluginIngredient> Ingredients;
    public int MaxUses;
    public int ExpReward;
    public int VillagerExp;
    public float PriceMultiplier;
    public int Demand;
    public int SpecialPrice;
    
    public PluginMerchantRecipe() {
        super();
    }
}