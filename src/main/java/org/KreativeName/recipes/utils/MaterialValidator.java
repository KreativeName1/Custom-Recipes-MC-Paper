package org.KreativeName.recipes.utils;

import org.bukkit.Material;
import org.bukkit.entity.Villager;

import java.util.Set;

public class MaterialValidator {

    public boolean isValidMaterial(String materialName) {
        try {
            Material.valueOf(materialName.toUpperCase());
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    public boolean isValidCount(int count) {
        return count > 0 && count <= 64;
    }

    public boolean isValidPattern(String[] patternLines) {
        for (String line : patternLines) {
            if (line.length() > 3) {
                return false;
            }
        }
        return true;
    }

    public boolean validatePatternKeys(String[] patternLines, Set<Character> definedKeys) {
        for (String line : patternLines) {
            for (char c : line.toCharArray()) {
                if (c != ' ' && !definedKeys.contains(c)) {
                    return false;
                }
            }
        }
        return true;
    }

    public boolean isValidProfession(String profession) {
        try {
            Villager.Profession.valueOf(profession.toUpperCase());
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
}