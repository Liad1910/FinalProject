package com.example.finalproject;

import android.Manifest;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
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
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

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
import java.util.concurrent.TimeUnit;

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
    private BottomNavigationView bottomNav;

    private static final MediaType PLAIN_TEXT = MediaType.get("text/plain; charset=utf-8");

    private final OkHttpClient client = new OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build();

    private final List<Marker> cinemaMarkers = new ArrayList<>();
    private Marker myLocationMarker;

    private final ActivityResultLauncher<String[]> locationPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), result -> {
                boolean fine = Boolean.TRUE.equals(result.get(Manifest.permission.ACCESS_FINE_LOCATION));
                boolean coarse = Boolean.TRUE.equals(result.get(Manifest.permission.ACCESS_COARSE_LOCATION));

                if (fine || coarse) {
                    fetchCurrentLocationAndLoadCinemas();
                } else {
                    Toast.makeText(this, "בלי הרשאת מיקום אי אפשר לטעון קולנועים לידך", Toast.LENGTH_LONG).show();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getSupportActionBar() != null) getSupportActionBar().hide();

        // ===== User Agent תקין =====
        Configuration.getInstance().setUserAgentValue("Watchly/1.0 (Android)");

        setContentView(R.layout.activity_nearby_cinema_free);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        bottomNav = findViewById(R.id.bottomNav);
        setupBottomNav();
        bottomNav.getMenu().findItem(R.id.bnav_more).setChecked(false);

        map = findViewById(R.id.osmMap);
        if (map != null) {
            map.setTileSource(TileSourceFactory.MAPNIK);
            map.setMultiTouchControls(true);
            map.getController().setZoom(12.0);
            map.getController().setCenter(new GeoPoint(32.0853, 34.7818));
        }

        ensureLocationPermissionThenLoad();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) { return false; }

    // =====================================================
    // BottomNav
    // =====================================================
    private void setupBottomNav() {
        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.bnav_home) {
                Intent i = new Intent(this, MainActivity.class);
                i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(i);
                finish();
                return true;
            }
            if (id == R.id.bnav_movies) {
                Intent i = new Intent(this, MoviesCategoryActivity.class);
                i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(i);
                finish();
                return true;
            }
            if (id == R.id.bnav_series) {
                Intent i = new Intent(this, SeriesCategoryActivity.class);
                i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(i);
                finish();
                return true;
            }
            if (id == R.id.bnav_more) {
                showMoreDialog();
                bottomNav.getMenu().findItem(R.id.bnav_more).setChecked(false);
                return true;
            }
            return false;
        });
    }

    private void showMoreDialog() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        boolean isLoggedIn = (user != null && !user.isAnonymous());

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("עוד");

        if (!isLoggedIn) {
            String[] options = {"התחברות", "הרשמה", "הקולנוע הקרוב", "צ'אט", "צור סרט / סדרה"};
            builder.setItems(options, (dialog, which) -> {
                switch (which) {
                    case 0: startActivity(new Intent(this, loginPage.class)); break;
                    case 1: startActivity(new Intent(this, registerPage.class)); break;
                    case 2: return; // כבר פה
                    case 3: startActivity(new Intent(this, AiActivity.class)); break;
                    case 4: startActivity(new Intent(this, CreateTitleActivity.class)); break;
                }
            });
        } else {
            String[] options = {"פרופיל", "הקולנוע הקרוב", "צ'אט", "צור סרט / סדרה", "התנתקות"};
            builder.setItems(options, (dialog, which) -> {
                switch (which) {
                    case 0: startActivity(new Intent(this, activity_user_page.class)); break;
                    case 1: return; // כבר פה
                    case 2: startActivity(new Intent(this, AiActivity.class)); break;
                    case 3: startActivity(new Intent(this, CreateTitleActivity.class)); break;
                    case 4:
                        FirebaseAuth.getInstance().signOut();
                        Intent i = new Intent(this, MainActivity.class);
                        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                        startActivity(i);
                        finish();
                        break;
                }
            });
        }
        builder.setNegativeButton("סגור", null);
        builder.show();
    }

    // =====================================================
    // Permission + location
    // =====================================================
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

        LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        boolean enabled = lm != null && (lm.isProviderEnabled(LocationManager.GPS_PROVIDER)
                || lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER));

        if (!enabled) {
            Toast.makeText(this, "המיקום כבוי — הדליקי Location/GPS", Toast.LENGTH_LONG).show();
            return;
        }

        Toast.makeText(this, "מביא מיקום...", Toast.LENGTH_SHORT).show();

        CancellationTokenSource cts = new CancellationTokenSource();
        CurrentLocationRequest req = new CurrentLocationRequest.Builder()
                .setPriority(fineGranted ? Priority.PRIORITY_HIGH_ACCURACY : Priority.PRIORITY_BALANCED_POWER_ACCURACY)
                .build();

        fusedLocationClient.getCurrentLocation(req, cts.getToken())
                .addOnSuccessListener(location -> {
                    if (location == null) {
                        Toast.makeText(this, "לא הצלחתי להביא מיקום. נסי להפעיל GPS.", Toast.LENGTH_LONG).show();
                        return;
                    }

                    double lat = location.getLatitude();
                    double lon = location.getLongitude();

                    GeoPoint me = new GeoPoint(lat, lon);
                    map.getController().setZoom(14.5);
                    map.getController().setCenter(me);
                    showOrUpdateMyLocationMarker(me);

                    Toast.makeText(this, "טוען קולנועים לידך...", Toast.LENGTH_SHORT).show();
                    loadCinemasFromOverpass(lat, lon, 7000);
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "שגיאה בקבלת מיקום: " + e.getMessage(), Toast.LENGTH_LONG).show());
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

    // =====================================================
    // Overpass
    // =====================================================
    private void loadCinemasFromOverpass(double lat, double lon, int radiusMeters) {
        clearCinemaMarkers();

        String query = "[out:json][timeout:30];" +
                "node[amenity=cinema](around:" + radiusMeters + "," + lat + "," + lon + ");" +
                "out body;";

        // ===== User Agent תקין למניעת שגיאה 406 =====
        Request request = new Request.Builder()
                .url("https://overpass-api.de/api/interpreter")
                .header("User-Agent", "Watchly/1.0 (Android)")
                .post(RequestBody.create(query, PLAIN_TEXT))
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() ->
                        Toast.makeText(NearbyCinemaFreeActivity.this,
                                "נכשל לטעון קולנועים — בדקי אינטרנט",
                                Toast.LENGTH_LONG).show());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String txt = response.body() != null ? response.body().string() : "";

                if (!response.isSuccessful()) {
                    runOnUiThread(() ->
                            Toast.makeText(NearbyCinemaFreeActivity.this,
                                    "שגיאה מהשרת: " + response.code(),
                                    Toast.LENGTH_LONG).show());
                    return;
                }

                try {
                    JSONObject root = new JSONObject(txt);
                    JSONArray elements = root.getJSONArray("elements");
                    int added = 0;

                    for (int i = 0; i < elements.length(); i++) {
                        JSONObject el = elements.getJSONObject(i);

                        if (!el.has("lat") || !el.has("lon")) continue;

                        double clat = el.getDouble("lat");
                        double clon = el.getDouble("lon");

                        String name = "קולנוע";
                        if (el.has("tags")) {
                            JSONObject tags = el.getJSONObject("tags");
                            if (tags.has("name:he")) name = tags.getString("name:he");
                            else if (tags.has("name")) name = tags.getString("name");
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
                        if (finalAdded == 0) {
                            Toast.makeText(NearbyCinemaFreeActivity.this,
                                    "לא נמצאו קולנועים בקרבתך 😕",
                                    Toast.LENGTH_LONG).show();
                        } else {
                            Toast.makeText(NearbyCinemaFreeActivity.this,
                                    "נמצאו " + finalAdded + " קולנועים ✅ לחצי על סימן",
                                    Toast.LENGTH_LONG).show();
                        }
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

    private void addCinemaMarker(double lat, double lon, String name) {
        if (map == null) return;

        Marker m = new Marker(map);
        m.setPosition(new GeoPoint(lat, lon));
        m.setTitle("🎥 " + name);
        m.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);

        m.setOnMarkerClickListener((marker, mapView) -> {
            new AlertDialog.Builder(NearbyCinemaFreeActivity.this)
                    .setTitle("🎬 " + name)
                    .setMessage("מה תרצי לעשות?")
                    .setPositiveButton("🎬 סרטים שמוקרנים", (d, which) ->
                            openGoogleSearch(name + " showtimes"))
                    .setNeutralButton("📍 גוגל מפות", (d, which) ->
                            openGoogleMapsAt(lat, lon, name))
                    .setNegativeButton("סגור", null)
                    .show();
            return true;
        });

        map.getOverlays().add(m);
        cinemaMarkers.add(m);
    }

    private void clearCinemaMarkers() {
        if (map == null) return;
        for (Marker m : cinemaMarkers) map.getOverlays().remove(m);
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
            Uri web = Uri.parse("https://www.google.com/maps/search/?api=1&query=" + lat + "," + lon);
            startActivity(new Intent(Intent.ACTION_VIEW, web));
        }
    }

    private void openGoogleSearch(String query) {
        Uri web = Uri.parse("https://www.google.com/search?q=" + Uri.encode(query));
        startActivity(new Intent(Intent.ACTION_VIEW, web));
    }

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