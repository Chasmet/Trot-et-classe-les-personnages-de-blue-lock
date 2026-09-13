package com.chk.bluelocktier;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.Settings;
import android.widget.Toast;

import androidx.core.content.FileProvider;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class UpdateManager {
    private static final String API = "https://api.github.com/repos/Chasmet/Trot-et-classe-les-personnages-de-blue-lock/releases/latest";
    private static final String PREF_PENDING = "pending_apk";
    private final Activity activity;
    private final SharedPreferences prefs;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    public UpdateManager(Activity activity, SharedPreferences prefs) {
        this.activity = activity;
        this.prefs = prefs;
    }

    public void check(boolean notifyIfCurrent) {
        executor.execute(() -> {
            try {
                HttpURLConnection c = (HttpURLConnection) new URL(API).openConnection();
                c.setConnectTimeout(10000);
                c.setReadTimeout(10000);
                c.setRequestProperty("User-Agent", "BlueLockTierList-Android");
                if (c.getResponseCode() != 200) throw new Exception("HTTP " + c.getResponseCode());
                String json = readStream(c);
                JSONObject release = new JSONObject(json);
                String tag = release.optString("tag_name", "0").replaceFirst("^[vV]", "");
                String apkUrl = findApk(release.optJSONArray("assets"));
                boolean newer = compare(tag, BuildConfig.VERSION_NAME) > 0;
                activity.runOnUiThread(() -> {
                    if (newer && apkUrl != null) showUpdate(tag, apkUrl);
                    else if (notifyIfCurrent) Toast.makeText(activity, "Application déjà à jour (v" + BuildConfig.VERSION_NAME + ")", Toast.LENGTH_LONG).show();
                });
            } catch (Exception e) {
                if (notifyIfCurrent) activity.runOnUiThread(() -> Toast.makeText(activity, "Vérification impossible : " + e.getMessage(), Toast.LENGTH_LONG).show());
            }
        });
    }

    private String readStream(HttpURLConnection c) throws Exception {
        try (BufferedInputStream in = new BufferedInputStream(c.getInputStream()); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            byte[] buf = new byte[4096];
            int n;
            while ((n = in.read(buf)) > 0) out.write(buf, 0, n);
            return out.toString("UTF-8");
        }
    }

    private String findApk(JSONArray assets) {
        if (assets == null) return null;
        for (int i = 0; i < assets.length(); i++) {
            JSONObject a = assets.optJSONObject(i);
            if (a != null && a.optString("name").toLowerCase().endsWith(".apk")) return a.optString("browser_download_url", null);
        }
        return null;
    }

    private void showUpdate(String version, String url) {
        new AlertDialog.Builder(activity)
                .setTitle("Mise à jour disponible")
                .setMessage("Version " + version + " disponible. Le classement sauvegardé sera conservé.")
                .setNegativeButton("Plus tard", null)
                .setPositiveButton("Télécharger", (d, w) -> download(version, url))
                .show();
    }

    private void download(String version, String url) {
        Toast.makeText(activity, "Téléchargement de la mise à jour…", Toast.LENGTH_LONG).show();
        executor.execute(() -> {
            try {
                File dir = activity.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS);
                if (dir == null) throw new Exception("stockage indisponible");
                File apk = new File(dir, "BlueLockTierList-" + version + ".apk");
                HttpURLConnection c = (HttpURLConnection) new URL(url).openConnection();
                c.setInstanceFollowRedirects(true);
                c.setConnectTimeout(15000);
                c.setReadTimeout(30000);
                c.setRequestProperty("User-Agent", "BlueLockTierList-Android");
                try (BufferedInputStream in = new BufferedInputStream(c.getInputStream()); FileOutputStream out = new FileOutputStream(apk)) {
                    byte[] buf = new byte[8192];
                    int n;
                    while ((n = in.read(buf)) > 0) out.write(buf, 0, n);
                }
                prefs.edit().putString(PREF_PENDING, apk.getAbsolutePath()).apply();
                activity.runOnUiThread(this::resumePendingInstall);
            } catch (Exception e) {
                activity.runOnUiThread(() -> Toast.makeText(activity, "Téléchargement impossible : " + e.getMessage(), Toast.LENGTH_LONG).show());
            }
        });
    }

    public void resumePendingInstall() {
        String path = prefs.getString(PREF_PENDING, null);
        if (path == null) return;
        File apk = new File(path);
        if (!apk.exists()) {
            prefs.edit().remove(PREF_PENDING).apply();
            return;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && !activity.getPackageManager().canRequestPackageInstalls()) {
            Intent s = new Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES, Uri.parse("package:" + activity.getPackageName()));
            activity.startActivity(s);
            Toast.makeText(activity, "Autorisez l’installation depuis cette application, puis revenez ici.", Toast.LENGTH_LONG).show();
            return;
        }
        Uri uri = FileProvider.getUriForFile(activity, activity.getPackageName() + ".fileprovider", apk);
        Intent i = new Intent(Intent.ACTION_VIEW);
        i.setDataAndType(uri, "application/vnd.android.package-archive");
        i.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_ACTIVITY_NEW_TASK);
        activity.startActivity(i);
    }

    private int compare(String a, String b) {
        String[] x = a.split("[^0-9]+");
        String[] y = b.split("[^0-9]+");
        int n = Math.max(x.length, y.length);
        for (int i = 0; i < n; i++) {
            int xv = i < x.length && !x[i].isEmpty() ? Integer.parseInt(x[i]) : 0;
            int yv = i < y.length && !y[i].isEmpty() ? Integer.parseInt(y[i]) : 0;
            if (xv != yv) return Integer.compare(xv, yv);
        }
        return 0;
    }
}
