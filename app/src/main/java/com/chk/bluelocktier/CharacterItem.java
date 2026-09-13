package com.chk.bluelocktier;

public class CharacterItem {
    public final String id;
    public final String name;
    public final String rarity;
    public final String role;
    public final String defaultTier;
    public String tier;

    public CharacterItem(String id, String name, String rarity, String role, String defaultTier) {
        this.id = id;
        this.name = name;
        this.rarity = rarity;
        this.role = role;
        this.defaultTier = defaultTier;
        this.tier = defaultTier;
    }
}
