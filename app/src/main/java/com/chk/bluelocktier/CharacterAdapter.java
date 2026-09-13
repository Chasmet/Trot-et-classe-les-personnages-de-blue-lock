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

public final class CharacterAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    public interface Listener {
        void onCheckedChanged(CharacterItem item);
    }

    private static final int TYPE_SECTION = 0;
    private static final int TYPE_CHARACTER = 1;

    private final List<Row> rows = new ArrayList<>();
    private final Listener listener;

    public CharacterAdapter(Listener listener) {
        this.listener = listener;
        setHasStableIds(true);
    }

    public void submit(List<CharacterItem> items, String selectedRarity) {
        rows.clear();

        for (String rarity : CharacterRepository.RARITIES) {
            if (!"Toutes".equals(selectedRarity) && !rarity.equals(selectedRarity)) {
                continue;
            }

            int count = 0;
            for (CharacterItem item : items) {
                if (rarity.equals(item.rarity)) count++;
            }

            // Le titre de famille reste visible même lorsque la recherche ne renvoie
            // aucun personnage : l'interface conserve toujours les 8 familles.
            rows.add(Row.section(rarity, count));
            for (CharacterItem item : items) {
                if (rarity.equals(item.rarity)) {
                    rows.add(Row.character(item));
                }
            }
        }

        notifyDataSetChanged();
    }

    public int getSpanSize(int position) {
        if (position < 0 || position >= rows.size()) return 2;
        return rows.get(position).character == null ? 2 : 1;
    }

    @Override
    public long getItemId(int position) {
        Row row = rows.get(position);
        if (row.character == null) {
            return ("section_" + row.section).hashCode();
        }
        return row.character.id.hashCode();
    }

    @Override
    public int getItemViewType(int position) {
        return rows.get(position).character == null ? TYPE_SECTION : TYPE_CHARACTER;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        if (viewType == TYPE_SECTION) {
            return new SectionHolder(inflater.inflate(R.layout.item_section_header, parent, false));
        }
        return new CharacterHolder(inflater.inflate(R.layout.item_character, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Row row = rows.get(position);
        if (holder instanceof SectionHolder) {
            SectionHolder sectionHolder = (SectionHolder) holder;
            sectionHolder.title.setText(row.section);
            sectionHolder.count.setText(row.sectionCount + " personnage" + (row.sectionCount > 1 ? "s" : ""));
            return;
        }

        CharacterHolder characterHolder = (CharacterHolder) holder;
        CharacterItem item = row.character;
        characterHolder.name.setText(item.name);
        bindProfileImage(characterHolder.profileImage, item);

        characterHolder.checkBox.setOnCheckedChangeListener(null);
        characterHolder.checkBox.setChecked(item.checked);
        updateCheckContentDescription(characterHolder.checkBox, item);
        characterHolder.checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (item.checked == isChecked) return;
            item.checked = isChecked;
            updateCheckContentDescription(characterHolder.checkBox, item);
            listener.onCheckedChanged(item);
        });
    }

    private static void updateCheckContentDescription(CheckBox checkBox, CharacterItem item) {
        checkBox.setContentDescription((item.checked ? "Décocher " : "Cocher ") + item.name);
    }

    private static void bindProfileImage(ImageView imageView, CharacterItem item) {
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
        return rows.size();
    }

    static final class SectionHolder extends RecyclerView.ViewHolder {
        final TextView title;
        final TextView count;

        SectionHolder(View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.sectionTitle);
            count = itemView.findViewById(R.id.sectionCount);
        }
    }

    static final class CharacterHolder extends RecyclerView.ViewHolder {
        final ImageView profileImage;
        final TextView name;
        final CheckBox checkBox;

        CharacterHolder(View itemView) {
            super(itemView);
            profileImage = itemView.findViewById(R.id.profileImage);
            name = itemView.findViewById(R.id.name);
            checkBox = itemView.findViewById(R.id.characterCheckBox);
        }
    }

    private static final class Row {
        final String section;
        final int sectionCount;
        final CharacterItem character;

        private Row(String section, int sectionCount, CharacterItem character) {
            this.section = section;
            this.sectionCount = sectionCount;
            this.character = character;
        }

        static Row section(String section, int count) {
            return new Row(section, count, null);
        }

        static Row character(CharacterItem character) {
            return new Row(null, 0, character);
        }
    }
}
