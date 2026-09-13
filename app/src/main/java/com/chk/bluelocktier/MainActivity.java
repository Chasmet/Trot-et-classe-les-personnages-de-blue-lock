package com.chk.bluelocktier;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class MainActivity extends AppCompatActivity {
    private static final String PREFS = "blue_lock_tier_prefs";
    private static final String PREF_AUTO_UPDATE = "auto_update";
    private static final String PREF_CUSTOM_ITEMS = "custom_items_v1";

    private final List<String> rarityChoices = new ArrayList<>();

    private List<CharacterItem> all;
    private CharacterAdapter adapter;
    private SharedPreferences prefs;
    private UpdateManager updateManager;
    private EditText search;
    private Spinner rarity;
    private TextView summary;

    private CharacterItem pendingImageItem;
    private Uri pendingAddImageUri;
    private TextView pendingAddImageStatus;

    private final ActivityResultLauncher<String[]> imagePickerLauncher =
            registerForActivityResult(new ActivityResultContracts.OpenDocument(), uri -> {
                if (uri == null) return;

                try {
                    getContentResolver().takePersistableUriPermission(
                            uri,
                            Intent.FLAG_GRANT_READ_URI_PERMISSION
                    );
                } catch (Exception ignored) {
                    // Certains fournisseurs de fichiers ne proposent pas de permission persistante.
                }

                if (pendingImageItem != null) {
                    pendingImageItem.imageUri = uri.toString();
                    prefs.edit().putString("image_" + pendingImageItem.id, pendingImageItem.imageUri).apply();
                    pendingImageItem = null;
                    refresh();
                    Toast.makeText(this, "Image enregistrée", Toast.LENGTH_SHORT).show();
                    return;
                }

                pendingAddImageUri = uri;
                if (pendingAddImageStatus != null) {
                    pendingAddImageStatus.setText("Image sélectionnée ✓");
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        prefs = getSharedPreferences(PREFS, MODE_PRIVATE);
        updateManager = new UpdateManager(this, prefs);

        all = CharacterRepository.all();
        appendSavedCustomItems();
        for (CharacterItem item : all) {
            item.checked = prefs.getBoolean("checked_" + item.id, false);
            item.imageUri = prefs.getString("image_" + item.id, null);
        }

        rarityChoices.add("Toutes");
        rarityChoices.addAll(CharacterRepository.RARITIES);

        summary = findViewById(R.id.txtSummary);
        search = findViewById(R.id.searchBox);
        rarity = findViewById(R.id.raritySpinner);

        RecyclerView recycler = findViewById(R.id.recyclerView);
        adapter = new CharacterAdapter(new CharacterAdapter.Listener() {
            @Override
            public void onCheckedChanged(CharacterItem item) {
                prefs.edit().putBoolean("checked_" + item.id, item.checked).apply();
                refreshSummary(currentVisibleCount());
            }

            @Override
            public void onAddRequested(String selectedCategory) {
                showAddCharacterDialog(selectedCategory);
            }

            @Override
            public void onImageRequested(CharacterItem item) {
                pendingAddImageUri = null;
                pendingAddImageStatus = null;
                pendingImageItem = item;
                imagePickerLauncher.launch(new String[]{"image/*"});
            }
        });

        GridLayoutManager layoutManager = new GridLayoutManager(this, 2);
        layoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {
                return adapter.getSpanSize(position);
            }
        });
        recycler.setLayoutManager(layoutManager);
        recycler.setAdapter(adapter);

        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_dropdown_item,
                rarityChoices
        );
        rarity.setAdapter(spinnerAdapter);
        rarity.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(android.widget.AdapterView<?> parent, View view, int position, long id) {
                refresh();
            }

            @Override
            public void onNothingSelected(android.widget.AdapterView<?> parent) { }
        });

        search.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                refresh();
            }

            @Override
            public void afterTextChanged(Editable s) { }
        });

        findViewById(R.id.btnSettings).setOnClickListener(v -> showSettings());
        refresh();

        if (prefs.getBoolean(PREF_AUTO_UPDATE, true)) {
            recycler.postDelayed(() -> updateManager.check(false), 900);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (updateManager != null) updateManager.resumePendingInstall();
    }

    private void appendSavedCustomItems() {
        String raw = prefs.getString(PREF_CUSTOM_ITEMS, "[]");
        Set<String> names = new HashSet<>();
        for (CharacterItem item : all) {
            names.add(item.name.trim().toLowerCase(Locale.ROOT));
        }

        try {
            JSONArray array = new JSONArray(raw);
            for (int i = 0; i < array.length(); i++) {
                if (all.size() >= CharacterRepository.EXPECTED_TOTAL) break;

                JSONObject object = array.optJSONObject(i);
                if (object == null) continue;

                String id = object.optString("id", "").trim();
                String name = object.optString("name", "").trim();
                String itemRarity = object.optString("rarity", "").trim();
                String normalized = name.toLowerCase(Locale.ROOT);

                if (id.isEmpty() || name.isEmpty()) continue;
                if (!CharacterRepository.RARITIES.contains(itemRarity)) continue;
                if (!names.add(normalized)) continue;

                all.add(new CharacterItem(id, name, itemRarity, true));
            }
        } catch (Exception ignored) {
            // En cas d'ancienne donnée invalide, l'Index officiel continue de fonctionner.
        }
    }

    private void showAddCharacterDialog(String defaultRarity) {
        if (all.size() >= CharacterRepository.EXPECTED_TOTAL) {
            Toast.makeText(this, "L’Index contient déjà 70 personnages", Toast.LENGTH_LONG).show();
            return;
        }

        pendingImageItem = null;
        pendingAddImageUri = null;

        View view = LayoutInflater.from(this).inflate(R.layout.dialog_add_character, null, false);
        EditText nameInput = view.findViewById(R.id.addName);
        Spinner rarityInput = view.findViewById(R.id.addRarity);
        Button imageButton = view.findViewById(R.id.addImageButton);
        pendingAddImageStatus = view.findViewById(R.id.addImageStatus);

        ArrayAdapter<String> rarityAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_dropdown_item,
                CharacterRepository.RARITIES
        );
        rarityInput.setAdapter(rarityAdapter);
        int defaultIndex = CharacterRepository.RARITIES.indexOf(defaultRarity);
        if (defaultIndex >= 0) rarityInput.setSelection(defaultIndex);

        imageButton.setOnClickListener(v -> {
            pendingImageItem = null;
            imagePickerLauncher.launch(new String[]{"image/*"});
        });

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(view)
                .setNegativeButton("Annuler", null)
                .setPositiveButton("Enregistrer", null)
                .create();

        dialog.setOnShowListener(ignored -> dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            String name = nameInput.getText().toString().trim();
            String selectedCategory = rarityInput.getSelectedItem() == null
                    ? defaultRarity
                    : rarityInput.getSelectedItem().toString();

            if (name.isEmpty()) {
                nameInput.setError("Écris le nom de l’attaquant");
                return;
            }
            if (!CharacterRepository.RARITIES.contains(selectedCategory)) {
                Toast.makeText(this, "Catégorie invalide", Toast.LENGTH_SHORT).show();
                return;
            }
            if (containsName(name)) {
                nameInput.setError("Ce personnage existe déjà");
                return;
            }
            if (all.size() >= CharacterRepository.EXPECTED_TOTAL) {
                Toast.makeText(this, "L’Index contient déjà 70 personnages", Toast.LENGTH_LONG).show();
                dialog.dismiss();
                return;
            }

            String id = "custom_" + System.currentTimeMillis();
            CharacterItem item = new CharacterItem(id, name, selectedCategory, true);
            if (pendingAddImageUri != null) {
                item.imageUri = pendingAddImageUri.toString();
                prefs.edit().putString("image_" + item.id, item.imageUri).apply();
            }

            all.add(item);
            saveCustomItems();
            pendingAddImageUri = null;
            pendingAddImageStatus = null;
            refresh();
            dialog.dismiss();
        }));

        dialog.setOnDismissListener(ignored -> {
            pendingAddImageUri = null;
            pendingAddImageStatus = null;
        });

        dialog.show();
    }

    private boolean containsName(String name) {
        String normalized = name.trim().toLowerCase(Locale.ROOT);
        for (CharacterItem item : all) {
            if (item.name.trim().toLowerCase(Locale.ROOT).equals(normalized)) return true;
        }
        return false;
    }

    private void saveCustomItems() {
        JSONArray array = new JSONArray();
        for (CharacterItem item : all) {
            if (!item.custom) continue;
            try {
                JSONObject object = new JSONObject();
                object.put("id", item.id);
                object.put("name", item.name);
                object.put("rarity", item.rarity);
                array.put(object);
            } catch (Exception ignored) { }
        }
        prefs.edit().putString(PREF_CUSTOM_ITEMS, array.toString()).apply();
    }

    private void refresh() {
        if (adapter == null || rarity == null || search == null) return;

        String query = search.getText().toString().trim().toLowerCase(Locale.ROOT);
        String selectedRarity = selectedRarity();
        List<CharacterItem> filtered = new ArrayList<>();

        for (CharacterItem item : all) {
            if (!"Toutes".equals(selectedRarity) && !selectedRarity.equals(item.rarity)) continue;
            if (!query.isEmpty() && !item.name.toLowerCase(Locale.ROOT).contains(query)) continue;
            filtered.add(item);
        }

        adapter.submit(filtered, selectedRarity);
        refreshSummary(filtered.size());
    }

    private String selectedRarity() {
        if (rarity == null || rarity.getSelectedItem() == null) return "Toutes";
        return rarity.getSelectedItem().toString();
    }

    private int currentVisibleCount() {
        if (all == null) return 0;
        String query = search == null ? "" : search.getText().toString().trim().toLowerCase(Locale.ROOT);
        String selectedRarity = selectedRarity();
        int visible = 0;
        for (CharacterItem item : all) {
            if (!"Toutes".equals(selectedRarity) && !selectedRarity.equals(item.rarity)) continue;
            if (!query.isEmpty() && !item.name.toLowerCase(Locale.ROOT).contains(query)) continue;
            visible++;
        }
        return visible;
    }

    private void refreshSummary(int visible) {
        int checkedCount = 0;
        for (CharacterItem item : all) {
            if (item.checked) checkedCount++;
        }

        String catalogState = CharacterRepository.isComplete(all)
                ? CharacterRepository.EXPECTED_TOTAL + "/" + CharacterRepository.EXPECTED_TOTAL + " indexés"
                : all.size() + "/" + CharacterRepository.EXPECTED_TOTAL + " indexés";

        summary.setText(checkedCount + " cochés • " + visible + " affichés • " + catalogState);
    }

    private void showSettings() {
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_settings, null, false);
        TextView version = view.findViewById(R.id.versionText);
        Switch auto = view.findViewById(R.id.autoUpdateSwitch);
        Button check = view.findViewById(R.id.checkUpdateButton);
        Button reset = view.findViewById(R.id.resetRankingButton);

        version.setText("Version installée : " + BuildConfig.VERSION_NAME + " (" + BuildConfig.VERSION_CODE + ")");
        auto.setChecked(prefs.getBoolean(PREF_AUTO_UPDATE, true));
        auto.setOnCheckedChangeListener((buttonView, isChecked) ->
                prefs.edit().putBoolean(PREF_AUTO_UPDATE, isChecked).apply());
        check.setOnClickListener(v -> updateManager.check(true));

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(view)
                .setPositiveButton("Fermer", null)
                .create();

        reset.setOnClickListener(v -> new AlertDialog.Builder(this)
                .setTitle("Tout décocher ?")
                .setMessage("Toutes les cases cochées seront remises à zéro. Les personnages et images ajoutés restent conservés.")
                .setNegativeButton("Annuler", null)
                .setPositiveButton("Tout décocher", (d, w) -> {
                    SharedPreferences.Editor editor = prefs.edit();
                    for (CharacterItem item : all) {
                        editor.remove("checked_" + item.id);
                        item.checked = false;
                    }
                    editor.apply();
                    refresh();
                })
                .show());

        dialog.show();
    }
}
