package com.chk.bluelocktier;

import java.util.ArrayList;
import java.util.List;

public final class CharacterRepository {
    private CharacterRepository() {}

    public static List<CharacterItem> all() {
        List<CharacterItem> items = new ArrayList<>();

        add(items, "loki", "Loki", "Master", "Striker / vitesse", "S+");
        add(items, "lavinho", "Lavinho", "Master", "Dribble / attaque", "S+");

        add(items, "kaiser", "Kaiser", "World Class", "Striker", "S");
        add(items, "don_lorenzo", "Don Lorenzo", "World Class", "Defender Hybrid", "S");
        add(items, "sae", "Sae", "World Class", "All-Rounding", "S");
        add(items, "nel_rin", "NEL Rin", "World Class", "Striker", "S");
        add(items, "nel_isagi", "NEL Isagi", "World Class", "Striker", "S");
        add(items, "nel_nagi", "NEL Nagi", "World Class", "Striker", "S");
        add(items, "nel_bachira", "NEL Bachira", "World Class", "Striker", "S");
        add(items, "nel_chigiri", "NEL Chigiri", "World Class", "Striker", "S");
        add(items, "nel_reo", "NEL Reo", "World Class", "All-Rounding", "S");

        add(items, "aiku", "Aiku", "Mythic", "Defender", "A");
        add(items, "yukimiya", "Yukimiya", "Mythic", "Dribbler Hybrid", "A");
        add(items, "rin", "Rin", "Mythic", "Striker", "A");
        add(items, "charles", "Charles", "Mythic", "Playmaker", "A");
        add(items, "shidou", "Shidou", "Mythic", "Striker", "A");
        add(items, "kunigami", "Kunigami", "Mythic", "Striker", "A");
        add(items, "reo", "Reo", "Mythic", "All-Rounding", "A");
        add(items, "hiori", "Hiori", "Mythic", "Playmaker / dribble", "A");

        add(items, "nagi", "Nagi", "Legendary", "Striker / trapping", "B");
        add(items, "ness", "Ness", "Legendary", "Playmaker", "B");
        add(items, "king", "King", "Legendary", "Striker", "B");
        add(items, "bachira", "Bachira", "Legendary", "Dribble / passe", "B");
        add(items, "kiyora", "Kiyora", "Legendary", "Midfielder", "B");

        add(items, "otoya", "Otoya", "Epic", "All-Rounding", "C");
        add(items, "karasu", "Karasu", "Epic", "Defender Hybrid", "C");
        add(items, "raichi", "Raichi", "Epic", "Defensive Playmaker", "C");
        add(items, "kurona", "Kurona", "Epic", "Playmaker", "C");

        add(items, "isagi", "Isagi", "Rare", "Striker", "D");
        add(items, "chigiri", "Chigiri", "Rare", "Speedster", "D");
        add(items, "gagamaru", "Gagamaru", "Rare", "Goalkeeper / défense", "D");

        add(items, "easter_kaiser", "Easter Kaiser", "Limited", "Style limité", "S+");
        add(items, "mcnagi_fryshiro", "McNagi Fryshiro", "Limited", "Style limité", "S+");
        add(items, "reaper_sae", "Reaper Sae", "Limited", "Style limité", "S+");
        add(items, "demon_shidou", "Demon Shidou", "Limited", "Style limité", "S+");
        add(items, "bloodmoon_rin", "Bloodmoon Rin", "Limited", "Style limité", "S+");
        add(items, "phantom_isagi", "Phantom Isagi", "Limited", "Style limité", "S+");
        add(items, "skeleton_nagi", "Skeleton Nagi", "Limited", "Style limité", "S+");
        add(items, "elf_emperor", "Elf Emperor", "Limited", "Style limité", "S+");
        add(items, "krampus_barou", "Krampus Barou", "Limited", "Style limité", "S+");
        add(items, "gingerbread_charles", "Gingerbread Charles", "Limited", "Style limité", "S+");
        add(items, "subzero_loki", "Subzero Loki", "Limited", "Style limité", "S+");
        add(items, "santa_lavinho", "Santa Lavinho", "Limited", "Style limité", "S+");
        add(items, "firework_bachira", "Firework Bachira", "Limited", "Style limité", "S+");
        add(items, "easter_yukimiya", "Easter Yukimiya", "Limited", "Style limité", "S+");

        add(items, "bunny", "Bunny", "Vaulted", "Indisponible / archivé", "NC");
        add(items, "igaguri", "Igaguri", "Vaulted", "Indisponible / archivé", "NC");
        return items;
    }

    private static void add(List<CharacterItem> list, String id, String name, String rarity, String role, String tier) {
        list.add(new CharacterItem(id, name, rarity, role, tier));
    }
}
