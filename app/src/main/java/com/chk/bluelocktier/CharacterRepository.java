package com.chk.bluelocktier;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Catalogue Steal a Striker.
 *
 * L'application attend 70 entrées au total. Cette classe refuse les doublons et
 * les raretés qui ne font pas partie des 8 familles officielles. Les noms ajoutés
 * ici sont uniquement ceux dont la famille a pu être vérifiée sans supposition.
 */
public final class CharacterRepository {
    public static final int EXPECTED_TOTAL = 70;

    public static final String COMMON = "Commun";
    public static final String UNCOMMON = "Peu commun";
    public static final String RARE = "Rare";
    public static final String EPIC = "Épique";
    public static final String LEGENDARY = "Légendaire";
    public static final String MYTHICAL = "Mythique";
    public static final String WORLD_CLASS = "World Class";
    public static final String WORLDS_BEST = "The World's Best";

    public static final List<String> RARITIES = Arrays.asList(
            COMMON,
            UNCOMMON,
            RARE,
            EPIC,
            LEGENDARY,
            MYTHICAL,
            WORLD_CLASS,
            WORLDS_BEST
    );

    private CharacterRepository() { }

    public static List<CharacterItem> all() {
        List<CharacterItem> items = new ArrayList<>();

        // Commun — 6 personnages au total dans l'Index du jeu.
        add(items, "raichi", "Raichi", COMMON);
        add(items, "niko", "Niko", COMMON);
        add(items, "isagi", "Isagi", COMMON);
        add(items, "kuon", "Kuon", COMMON);

        // Peu commun — 7 personnages au total dans l'Index du jeu.
        add(items, "himizu", "Himizu", UNCOMMON);
        add(items, "kunigami", "Kunigami", UNCOMMON);
        add(items, "hiori", "Hiori", UNCOMMON);

        // Rare — 7 personnages au total dans l'Index du jeu.
        add(items, "aryu", "Aryu", RARE);
        add(items, "chigiri", "Chigiri", RARE);
        add(items, "sendou", "Sendou", RARE);

        // Épique — 5 personnages au total dans l'Index du jeu.
        add(items, "nagi", "Nagi", EPIC);
        add(items, "barou", "Barou", EPIC);
        add(items, "bachira", "Bachira", EPIC);
        add(items, "reo", "Reo", EPIC);

        // Légendaire — 6 personnages au total dans l'Index du jeu.
        add(items, "karasu", "Karasu", LEGENDARY);
        add(items, "yukimiya", "Yukimiya", LEGENDARY);
        add(items, "shidou", "Shidou", LEGENDARY);

        // The World's Best — exemples explicitement confirmés dans les sources du jeu.
        add(items, "snuffy", "Snuffy", WORLDS_BEST);
        add(items, "chris_prince", "Chris Prince", WORLDS_BEST);
        add(items, "noel_noa", "Noel Noa", WORLDS_BEST);
        add(items, "lavinho", "Lavinho", WORLDS_BEST);
        add(items, "sae_serious", "Sae [Serious]", WORLDS_BEST);
        add(items, "ego_prime", "Ego [Prime]", WORLDS_BEST);
        add(items, "loki_godspeed", "Loki [Godspeed]", WORLDS_BEST);

        validate(items);
        return items;
    }

    public static boolean isComplete(List<CharacterItem> items) {
        return items != null && items.size() == EXPECTED_TOTAL;
    }

    private static void add(List<CharacterItem> list, String id, String name, String rarity) {
        list.add(new CharacterItem(id, name, rarity));
    }

    private static void validate(List<CharacterItem> items) {
        Set<String> ids = new HashSet<>();
        Set<String> names = new HashSet<>();
        for (CharacterItem item : items) {
            if (!RARITIES.contains(item.rarity)) {
                throw new IllegalStateException("Rareté invalide : " + item.rarity);
            }
            if (!ids.add(item.id)) {
                throw new IllegalStateException("ID en doublon : " + item.id);
            }
            String normalized = item.name.trim().toLowerCase(java.util.Locale.ROOT);
            if (!names.add(normalized)) {
                throw new IllegalStateException("Personnage en doublon : " + item.name);
            }
        }
        if (items.size() > EXPECTED_TOTAL) {
            throw new IllegalStateException("L'Index dépasse " + EXPECTED_TOTAL + " personnages");
        }
    }
}
