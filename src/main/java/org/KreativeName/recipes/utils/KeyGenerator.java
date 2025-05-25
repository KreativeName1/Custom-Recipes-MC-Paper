package org.KreativeName.recipes.utils;

import java.util.UUID;

public class KeyGenerator {

    public static String generateKey(String type, String resultItem) {
        return "recipe_" + type + "_" + resultItem.toLowerCase() + "_" + UUID.randomUUID().toString().replace("-", "");
    }
}
