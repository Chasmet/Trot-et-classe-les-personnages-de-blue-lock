package com.chk.bluelocktier;

public final class CharacterItem {
    public final String id;
    public final String name;
    public final String rarity;
    public final boolean custom;
    public boolean checked;
    public String imageUri;

    public CharacterItem(String id, String name, String rarity) {
        this(id, name, rarity, false);
    }

    public CharacterItem(String id, String name, String rarity, boolean custom) {
        this.id = id;
        this.name = name;
        this.rarity = rarity;
        this.custom = custom;
        this.checked = false;
        this.imageUri = null;
    }
}
