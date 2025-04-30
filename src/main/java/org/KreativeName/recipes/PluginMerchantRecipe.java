package org.KreativeName.recipes;

import org.bukkit.entity.Villager;

import java.util.List;

/**
 * Represents a merchant (villager) trade recipe
 */
public class PluginMerchantRecipe extends PluginRecipe {
    public Villager.Profession Profession;
    public List<PluginIngredient> Ingredients;
    public int Uses;
    public int MaxUses;
    public boolean ExpReward;
    public int VillagerExp;
    public float PriceMultiplier;
    public int Demand;
    public int SpecialPrice;
    
    public PluginMerchantRecipe() {
        super();
    }
}