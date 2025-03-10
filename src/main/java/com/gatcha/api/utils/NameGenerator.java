package com.gatcha.api.utils;

import java.util.Arrays;
import java.util.List;

/**
 * Monster Name Generator
 * Maintains consistent naming logic with the frontend
 */
public class NameGenerator {
    // Monster name prefixes
    private static final List<String> prefixes = Arrays.asList(
            "Blazing", "Frozen", "Savage", "Mysterious", "Ancient", "Ghostly", "Lightning",
            "Shadow", "Radiant", "Chaotic", "Serene", "Furious", "Wise", "Mighty",
            "Swift", "Sturdy", "Deadly", "Silent", "Thundering", "Hurricane", "Earth",
            "Ocean", "Stellar", "Moonlight", "Solar", "Dark", "Holy", "Evil",
            "Benevolent", "Divine", "Magical", "Natural", "Mechanical", "Primordial", "Futuristic");

    // Monster name suffixes
    private static final List<String> suffixes = Arrays.asList(
            "Warrior", "Mage", "Assassin", "Guardian", "Beast", "Dragon", "Elf", "Demon", "Angel",
            "Wizard", "Knight", "Hunter", "Ghost", "Giant", "Dwarf", "Fairy", "Orc", "Undead",
            "Overlord", "Champion", "Hero", "Messenger", "Prophet", "Protector", "Destroyer", "Creator",
            "Ruler", "Dominator", "Explorer", "Predator", "Ranger", "Warlock", "Summoner", "Elementalist",
            "Swordsman", "Archer", "Gunner", "Fighter", "Samurai", "Ninja", "Paladin", "Reaper");

    /**
     * Generates a deterministic name based on monster ID and element type
     * 
     * @param monsterId Monster ID
     * @param element   Monster element type
     * @return Generated monster name
     */
    public static String generateName(String monsterId, String element) {
        // Use the hash of the monster ID to determine prefix and suffix indices
        int hash = hashString(monsterId);
        int prefixIndex = Math.abs(hash % prefixes.size());
        int suffixIndex = Math.abs((hash * 31) % suffixes.size());

        // Choose different prefix based on element type
        String elementPrefix = "";
        switch (element.toLowerCase()) {
            case "fire":
                elementPrefix = "Fire ";
                break;
            case "water":
                elementPrefix = "Water ";
                break;
            case "earth":
                elementPrefix = "Earth ";
                break;
            case "wind":
                elementPrefix = "Wind ";
                break;
            default:
                elementPrefix = "";
        }

        return prefixes.get(prefixIndex) + " " + elementPrefix + suffixes.get(suffixIndex);
    }

    /**
     * Simple string hash function
     * 
     * @param str String to hash
     * @return Hash value
     */
    private static int hashString(String str) {
        int hash = 0;
        for (int i = 0; i < str.length(); i++) {
            char c = str.charAt(i);
            hash = ((hash << 5) - hash) + c;
            hash = hash & hash; // Convert to 32bit integer
        }
        return Math.abs(hash);
    }
}