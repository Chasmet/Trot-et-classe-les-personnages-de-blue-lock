package com.chk.bluelocktier;

import android.app.AlertDialog;
import android.content.SharedPreferences;
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

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {
    private static final String PREFS = "blue_lock_tier_prefs";
    private static final String PREF_AUTO_UPDATE = "auto_update";

    private final List<String> rarityChoices = new ArrayList<>();

    private List<CharacterItem> all;
    private CharacterAdapter adapter;
    private SharedPreferences prefs;
    private UpdateManager updateManager;
    private EditText search;
    private Spinner rarity;
    private TextView summary;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        prefs = getSharedPreferences(PREFS, MODE_PRIVATE);
        updateManager = new UpdateManager(this, prefs);
        all = CharacterRepository.all();
        for (CharacterItem item : all) {
            item.checked = prefs.getBoolean("checked_" + item.id, false);
        }

        rarityChoices.add("Toutes");
        rarityChoices.addAll(CharacterRepository.RARITIES);

        summary = findViewById(R.id.txtSummary);
        search = findViewById(R.id.searchBox);
        rarity = findViewById(R.id.raritySpinner);

        RecyclerView recycler = findViewById(R.id.recyclerView);
        adapter = new CharacterAdapter(item -> {
            prefs.edit().putBoolean("checked_" + item.id, item.checked).apply();
            refreshSummary(currentVisibleCount());
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
                .setMessage("Toutes les cases cochées seront remises à zéro.")
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
