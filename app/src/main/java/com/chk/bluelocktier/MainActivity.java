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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {
    private static final String PREFS = "blue_lock_tier_prefs";
    private static final String PREF_AUTO_UPDATE = "auto_update";
    private final String[] rarities = {"Toutes", "Master", "World Class", "Mythic", "Legendary", "Epic", "Rare", "Limited", "Vaulted"};
    private List<CharacterItem> all;
    private CharacterAdapter adapter;
    private SharedPreferences prefs;
    private UpdateManager updateManager;
    private EditText search;
    private Spinner rarity;
    private TextView summary;

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        prefs = getSharedPreferences(PREFS, MODE_PRIVATE);
        updateManager = new UpdateManager(this, prefs);
        all = CharacterRepository.all();
        for (CharacterItem item : all) item.tier = prefs.getString("rank_" + item.id, item.defaultTier);

        summary = findViewById(R.id.txtSummary);
        search = findViewById(R.id.searchBox);
        rarity = findViewById(R.id.raritySpinner);
        RecyclerView recycler = findViewById(R.id.recyclerView);
        recycler.setLayoutManager(new LinearLayoutManager(this));
        adapter = new CharacterAdapter(item -> {
            prefs.edit().putString("rank_" + item.id, item.tier).apply();
            refresh();
        });
        recycler.setAdapter(adapter);

        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, rarities);
        rarity.setAdapter(spinnerAdapter);
        rarity.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(android.widget.AdapterView<?> parent, View view, int position, long id) { refresh(); }
            @Override public void onNothingSelected(android.widget.AdapterView<?> parent) { }
        });
        search.addTextChangedListener(new TextWatcher() {
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
            public void onTextChanged(CharSequence s, int start, int before, int count) { refresh(); }
            public void afterTextChanged(Editable s) { }
        });
        findViewById(R.id.btnSettings).setOnClickListener(v -> showSettings());
        refresh();

        if (prefs.getBoolean(PREF_AUTO_UPDATE, true)) recycler.postDelayed(() -> updateManager.check(false), 900);
    }

    @Override protected void onResume() {
        super.onResume();
        if (updateManager != null) updateManager.resumePendingInstall();
    }

    private void refresh() {
        if (adapter == null || rarity == null) return;
        String q = search.getText().toString().trim().toLowerCase(Locale.ROOT);
        String r = rarity.getSelectedItem() == null ? "Toutes" : rarity.getSelectedItem().toString();
        List<CharacterItem> filtered = new ArrayList<>();
        for (CharacterItem item : all) {
            if (!"Toutes".equals(r) && !r.equals(item.rarity)) continue;
            if (!q.isEmpty() && !(item.name + " " + item.rarity + " " + item.role).toLowerCase(Locale.ROOT).contains(q)) continue;
            filtered.add(item);
        }
        filtered.sort(Comparator.comparingInt((CharacterItem i) -> tierOrder(i.tier)).thenComparing(i -> i.name));
        adapter.submit(filtered);
        summary.setText(filtered.size() + " affichés • " + all.size() + " styles/personnages • classement sauvegardé localement");
    }

    private int tierOrder(String t) {
        switch (t) {
            case "S+": return 0;
            case "S": return 1;
            case "A": return 2;
            case "B": return 3;
            case "C": return 4;
            case "D": return 5;
            default: return 6;
        }
    }

    private void showSettings() {
        View v = LayoutInflater.from(this).inflate(R.layout.dialog_settings, null, false);
        TextView version = v.findViewById(R.id.versionText);
        Switch auto = v.findViewById(R.id.autoUpdateSwitch);
        Button check = v.findViewById(R.id.checkUpdateButton);
        Button reset = v.findViewById(R.id.resetRankingButton);
        version.setText("Version installée : " + BuildConfig.VERSION_NAME + " (" + BuildConfig.VERSION_CODE + ")");
        auto.setChecked(prefs.getBoolean(PREF_AUTO_UPDATE, true));
        auto.setOnCheckedChangeListener((buttonView, isChecked) -> prefs.edit().putBoolean(PREF_AUTO_UPDATE, isChecked).apply());
        check.setOnClickListener(view -> updateManager.check(true));
        AlertDialog dialog = new AlertDialog.Builder(this).setView(v).setPositiveButton("Fermer", null).create();
        reset.setOnClickListener(view -> new AlertDialog.Builder(this)
                .setTitle("Réinitialiser ?")
                .setMessage("Les rangs personnels reviendront au classement initial.")
                .setNegativeButton("Annuler", null)
                .setPositiveButton("Réinitialiser", (d, w) -> {
                    SharedPreferences.Editor e = prefs.edit();
                    for (CharacterItem item : all) {
                        e.remove("rank_" + item.id);
                        item.tier = item.defaultTier;
                    }
                    e.apply();
                    refresh();
                }).show());
        dialog.show();
    }
}
