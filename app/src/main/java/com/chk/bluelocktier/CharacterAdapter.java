package com.chk.bluelocktier;

import android.app.AlertDialog;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class CharacterAdapter extends RecyclerView.Adapter<CharacterAdapter.Holder> {
    public interface Listener { void onTierChanged(CharacterItem item); }
    private static final String[] TIERS = {"S+", "S", "A", "B", "C", "D", "NC"};
    private final List<CharacterItem> shown = new ArrayList<>();
    private final Listener listener;

    public CharacterAdapter(Listener listener) { this.listener = listener; }

    public void submit(List<CharacterItem> items) {
        shown.clear();
        shown.addAll(items);
        notifyDataSetChanged();
    }

    @NonNull @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_character, parent, false);
        return new Holder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull Holder h, int position) {
        CharacterItem item = shown.get(position);
        h.name.setText(item.name);
        h.meta.setText(item.rarity + " • " + item.role);
        h.avatar.setText(initials(item.name));
        h.avatar.setBackground(circle(rarityColor(item.rarity)));
        h.tier.setText(item.tier);
        h.tier.setTextColor(Color.WHITE);
        h.tier.setBackgroundTintList(android.content.res.ColorStateList.valueOf(tierColor(item.tier)));
        h.tier.setOnClickListener(v -> {
            item.tier = nextTier(item.tier);
            int pos = h.getBindingAdapterPosition();
            if (pos != RecyclerView.NO_POSITION) notifyItemChanged(pos);
            listener.onTierChanged(item);
        });
        h.tier.setOnLongClickListener(v -> {
            new AlertDialog.Builder(v.getContext())
                    .setTitle("Rang de " + item.name)
                    .setItems(TIERS, (d, which) -> {
                        item.tier = TIERS[which];
                        int pos = h.getBindingAdapterPosition();
                        if (pos != RecyclerView.NO_POSITION) notifyItemChanged(pos);
                        listener.onTierChanged(item);
                    }).show();
            return true;
        });
    }

    @Override public int getItemCount() { return shown.size(); }

    private static String nextTier(String current) {
        for (int i = 0; i < TIERS.length; i++) if (TIERS[i].equals(current)) return TIERS[(i + 1) % TIERS.length];
        return "NC";
    }

    private static String initials(String name) {
        String[] parts = name.replace("NEL ", "N ").split(" ");
        StringBuilder b = new StringBuilder();
        for (String p : parts) if (!p.isEmpty()) b.append(p.charAt(0));
        return b.substring(0, Math.min(2, b.length())).toUpperCase(Locale.ROOT);
    }

    private static GradientDrawable circle(int color) {
        GradientDrawable d = new GradientDrawable();
        d.setShape(GradientDrawable.OVAL);
        d.setColor(color);
        return d;
    }

    private static int rarityColor(String r) {
        switch (r) {
            case "Master": return Color.rgb(255, 81, 0);
            case "World Class": return Color.rgb(234, 0, 255);
            case "Mythic": return Color.rgb(142, 45, 226);
            case "Legendary": return Color.rgb(255, 176, 0);
            case "Epic": return Color.rgb(0, 166, 255);
            case "Rare": return Color.rgb(19, 185, 111);
            case "Limited": return Color.rgb(230, 50, 95);
            default: return Color.rgb(90, 105, 120);
        }
    }

    private static int tierColor(String t) {
        switch (t) {
            case "S+": return Color.rgb(220, 36, 76);
            case "S": return Color.rgb(245, 112, 45);
            case "A": return Color.rgb(224, 158, 32);
            case "B": return Color.rgb(40, 161, 96);
            case "C": return Color.rgb(32, 132, 205);
            case "D": return Color.rgb(92, 90, 191);
            default: return Color.rgb(85, 99, 114);
        }
    }

    static class Holder extends RecyclerView.ViewHolder {
        final TextView avatar, name, meta;
        final Button tier;
        Holder(View v) {
            super(v);
            avatar = v.findViewById(R.id.avatar);
            name = v.findViewById(R.id.name);
            meta = v.findViewById(R.id.meta);
            tier = v.findViewById(R.id.tierButton);
        }
    }
}
