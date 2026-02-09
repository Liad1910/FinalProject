package com.example.finalproject;

import android.Manifest;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.android.gms.location.CurrentLocationRequest;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.gms.tasks.CancellationTokenSource;

import org.json.JSONArray;
import org.json.JSONObject;
import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class NearbyCinemaFreeActivity extends AppCompatActivity {

    private MapView map;

    private FusedLocationProviderClient fusedLocationClient;

    private static final MediaType PLAIN_TEXT =
            MediaType.get("text/plain; charset=utf-8");

    private final OkHttpClient client = new OkHttpClient();

    private final List<Marker> cinemaMarkers = new ArrayList<>();
    private Marker myLocationMarker;

    // ============== Permissions launcher ==============
    private final ActivityResultLauncher<String[]> locationPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), result -> {

                boolean fine = Boolean.TRUE.equals(result.get(Manifest.permission.ACCESS_FINE_LOCATION));
                boolean coarse = Boolean.TRUE.equals(result.get(Manifest.permission.ACCESS_COARSE_LOCATION));

                if (fine || coarse) {
                    fetchCurrentLocationAndLoadCinemas();
                } else {
                    Toast.makeText(this,
                            "בלי הרשאת מיקום אי אפשר לטעון קולנועים לידך",
                            Toast.LENGTH_LONG).show();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // חשוב ל-OSMDroid כדי לא להיחסם
        Configuration.getInstance().setUserAgentValue(getPackageName());

        setContentView(R.layout.activity_nearby_cinema_free);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        // Map init
        map = findViewById(R.id.osmMap);
        if (map != null) {
            map.setTileSource(TileSourceFactory.MAPNIK);
            map.setMultiTouchControls(true);

            // ברירת מחדל עד שמקבלים מיקום
            GeoPoint center = new GeoPoint(32.0853, 34.7818);
            map.getController().setZoom(12.0);
            map.getController().setCenter(center);
        }

        // טוען אוטומטית לפי מיקום אמיתי
        ensureLocationPermissionThenLoad();
    }

    // =========================
    // Permission + location
    // =========================
    private void ensureLocationPermissionThenLoad() {
        boolean fineGranted = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED;
        boolean coarseGranted = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                == PackageManager.PERMISSION_GRANTED;

        if (fineGranted || coarseGranted) {
            fetchCurrentLocationAndLoadCinemas();
        } else {
            locationPermissionLauncher.launch(new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
            });
        }
    }

    private void fetchCurrentLocationAndLoadCinemas() {
        if (map == null) return;

        boolean fineGranted = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED;
        boolean coarseGranted = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                == PackageManager.PERMISSION_GRANTED;

        if (!fineGranted && !coarseGranted) return;

        // בדיקה שהמיקום דלוק במכשיר
        LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        boolean enabled = lm != null && (lm.isProviderEnabled(LocationManager.GPS_PROVIDER)
                || lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER));

        if (!enabled) {
            Toast.makeText(this, "המיקום כבוי — הדליקי Location/GPS", Toast.LENGTH_LONG).show();
            return;
        }

        Toast.makeText(this, "מביא מיקום אמיתי...", Toast.LENGTH_SHORT).show();

        CancellationTokenSource cts = new CancellationTokenSource();
        CurrentLocationRequest req = new CurrentLocationRequest.Builder()
                .setPriority(fineGranted ? Priority.PRIORITY_HIGH_ACCURACY : Priority.PRIORITY_BALANCED_POWER_ACCURACY)
                .build();

        fusedLocationClient.getCurrentLocation(req, cts.getToken())
                .addOnSuccessListener(location -> {
                    if (location == null) {
                        Toast.makeText(this,
                                "לא הצלחתי להביא מיקום. נסי לצאת החוצה/להפעיל GPS.",
                                Toast.LENGTH_LONG).show();
                        return;
                    }

                    double lat = location.getLatitude();
                    double lon = location.getLongitude();

                    GeoPoint me = new GeoPoint(lat, lon);
                    map.getController().setZoom(14.5);
                    map.getController().setCenter(me);

                    showOrUpdateMyLocationMarker(me);

                    Toast.makeText(this, "טוען קולנועים לידך...", Toast.LENGTH_SHORT).show();
                    loadCinemasFromOverpass(lat, lon, 7000); // 7 ק"מ
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this,
                                "שגיאה בקבלת מיקום: " + e.getMessage(),
                                Toast.LENGTH_LONG).show()
                );
    }

    private void showOrUpdateMyLocationMarker(GeoPoint me) {
        if (map == null) return;

        if (myLocationMarker == null) {
            myLocationMarker = new Marker(map);
            myLocationMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
            myLocationMarker.setTitle("📍 המיקום שלי");
            map.getOverlays().add(myLocationMarker);
        }

        myLocationMarker.setPosition(me);
        map.invalidate();
    }

    // ==========================
    // Overpass -> cinemas
    // ==========================
    private void loadCinemasFromOverpass(double lat, double lon, int radiusMeters) {

        clearCinemaMarkers();

        String query =
                "[out:json][timeout:25];" +
                        "(" +
                        "  node[amenity=cinema](around:" + radiusMeters + "," + lat + "," + lon + ");" +
                        "  way[amenity=cinema](around:" + radiusMeters + "," + lat + "," + lon + ");" +
                        "  relation[amenity=cinema](around:" + radiusMeters + "," + lat + "," + lon + ");" +
                        ");" +
                        "out center tags;";

        Request request = new Request.Builder()
                .url("https://overpass-api.de/api/interpreter")
                .post(RequestBody.create(query, PLAIN_TEXT))
                .build();

        client.newCall(request).enqueue(new Callback() {

            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() ->
                        Toast.makeText(NearbyCinemaFreeActivity.this,
                                "נכשל לטעון קולנועים (בדקי אינטרנט)",
                                Toast.LENGTH_LONG).show());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {

                String txt = (response.body() != null) ? response.body().string() : "";

                if (!response.isSuccessful()) {
                    String shortBody = txt.length() > 200 ? txt.substring(0, 200) : txt;
                    runOnUiThread(() ->
                            Toast.makeText(NearbyCinemaFreeActivity.this,
                                    "Overpass error: " + response.code() + "\n" + shortBody,
                                    Toast.LENGTH_LONG).show());
                    return;
                }

                try {
                    JSONObject root = new JSONObject(txt);
                    JSONArray elements = root.getJSONArray("elements");

                    int added = 0;

                    for (int i = 0; i < elements.length(); i++) {
                        JSONObject el = elements.getJSONObject(i);

                        double clat, clon;

                        // node
                        if (el.has("lat") && el.has("lon")) {
                            clat = el.getDouble("lat");
                            clon = el.getDouble("lon");

                            // way/relation -> center
                        } else if (el.has("center")) {
                            JSONObject c = el.getJSONObject("center");
                            clat = c.getDouble("lat");
                            clon = c.getDouble("lon");
                        } else {
                            continue;
                        }

                        String name = "Cinema";
                        if (el.has("tags")) {
                            JSONObject tags = el.getJSONObject("tags");
                            if (tags.has("name")) name = tags.getString("name");
                        }

                        final double fLat = clat;
                        final double fLon = clon;
                        final String fName = name;

                        runOnUiThread(() -> addCinemaMarker(fLat, fLon, fName));
                        added++;
                    }

                    final int finalAdded = added;

                    runOnUiThread(() -> {
                        if (map != null) map.invalidate();
                        Toast.makeText(NearbyCinemaFreeActivity.this,
                                "סימנתי " + finalAdded + " קולנועים ✅ (לחצי על אחד)",
                                Toast.LENGTH_LONG).show();
                    });

                } catch (Exception e) {
                    runOnUiThread(() ->
                            Toast.makeText(NearbyCinemaFreeActivity.this,
                                    "שגיאה בפענוח נתונים",
                                    Toast.LENGTH_LONG).show());
                }
            }
        });
    }

    // ==========================
    // Marker click -> Google actions
    // ==========================
    private void addCinemaMarker(double lat, double lon, String name) {
        if (map == null) return;

        Marker m = new Marker(map);
        m.setPosition(new GeoPoint(lat, lon));
        m.setTitle("🎥 " + name);
        m.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);

        m.setOnMarkerClickListener((marker, mapView) -> {
            String cinemaName = name;

            new AlertDialog.Builder(NearbyCinemaFreeActivity.this)
                    .setTitle(cinemaName)
                    .setMessage("מה תרצי לעשות?")
                    .setPositiveButton("🎬 סרטים שמוקרנים עכשיו", (d, which) ->
                            openGoogleSearch(cinemaName + " showtimes")
                    )
                    .setNeutralButton("📍 פתח בגוגל מפות", (d, which) ->
                            openGoogleMapsAt(lat, lon, cinemaName)
                    )
                    .setNegativeButton("סגור", null)
                    .show();

            return true;
        });

        map.getOverlays().add(m);
        cinemaMarkers.add(m);
    }

    private void clearCinemaMarkers() {
        if (map == null) return;
        for (Marker m : cinemaMarkers) {
            map.getOverlays().remove(m);
        }
        cinemaMarkers.clear();
        map.invalidate();
    }

    private void openGoogleMapsAt(double lat, double lon, String label) {
        Uri uri = Uri.parse("geo:" + lat + "," + lon + "?q=" +
                lat + "," + lon + "(" + Uri.encode(label) + ")");

        Intent i = new Intent(Intent.ACTION_VIEW, uri);
        i.setPackage("com.google.android.apps.maps");

        try {
            startActivity(i);
        } catch (ActivityNotFoundException e) {
            Uri web = Uri.parse("https://www.google.com/maps/search/?api=1&query=" +
                    lat + "," + lon);
            startActivity(new Intent(Intent.ACTION_VIEW, web));
        }
    }

    private void openGoogleSearch(String query) {
        Uri web = Uri.parse("https://www.google.com/search?q=" + Uri.encode(query));
        startActivity(new Intent(Intent.ACTION_VIEW, web));
    }

    // ====================
    // lifecycle map
    // ====================
    @Override
    protected void onResume() {
        super.onResume();
        if (map != null) map.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (map != null) map.onPause();
    }
}
