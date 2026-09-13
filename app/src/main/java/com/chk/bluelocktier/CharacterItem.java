package com.chk.bluelocktier;

public final class CharacterItem {
    public final String id;
    public final String name;
    public final String rarity;
    public boolean checked;

    public CharacterItem(String id, String name, String rarity) {
        this.id = id;
        this.name = name;
        this.rarity = rarity;
        this.checked = false;
    }
}
