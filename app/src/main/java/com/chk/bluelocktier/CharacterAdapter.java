package com.chk.bluelocktier;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class CharacterAdapter extends RecyclerView.Adapter<CharacterAdapter.Holder> {
    public interface Listener { void onCheckedChanged(CharacterItem item); }

    private final List<CharacterItem> shown = new ArrayList<>();
    private final Listener listener;

    public CharacterAdapter(Listener listener) {
        this.listener = listener;
    }

    public void submit(List<CharacterItem> items) {
        shown.clear();
        shown.addAll(items);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_character, parent, false);
        return new Holder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull Holder h, int position) {
        CharacterItem item = shown.get(position);
        h.name.setText(item.name);
        h.meta.setText(item.rarity + " • " + item.role);
        bindProfileImage(h.profileImage, item);

        h.checkBox.setOnCheckedChangeListener(null);
        h.checkBox.setChecked(item.checked);
        h.checkBox.setContentDescription(item.checked ? "Décocher " + item.name : "Cocher " + item.name);
        h.checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (item.checked == isChecked) return;
            item.checked = isChecked;
            h.checkBox.setContentDescription(isChecked ? "Décocher " + item.name : "Cocher " + item.name);
            listener.onCheckedChanged(item);
        });
    }

    private void bindProfileImage(ImageView imageView, CharacterItem item) {
        Context context = imageView.getContext();
        String resourceName = "profile_" + item.id;
        int resourceId = context.getResources().getIdentifier(resourceName, "drawable", context.getPackageName());
        if (resourceId != 0) {
            imageView.setImageResource(resourceId);
            imageView.setContentDescription("Image de profil de " + item.name);
        } else {
            imageView.setImageResource(R.drawable.profile_placeholder);
            imageView.setContentDescription("Image de profil à ajouter pour " + item.name);
        }
    }

    @Override
    public int getItemCount() {
        return shown.size();
    }

    static class Holder extends RecyclerView.ViewHolder {
        final ImageView profileImage;
        final TextView name;
        final TextView meta;
        final CheckBox checkBox;

        Holder(View v) {
            super(v);
            profileImage = v.findViewById(R.id.profileImage);
            name = v.findViewById(R.id.name);
            meta = v.findViewById(R.id.meta);
            checkBox = v.findViewById(R.id.characterCheckBox);
        }
    }
}
