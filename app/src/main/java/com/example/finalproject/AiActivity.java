package com.example.finalproject;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import org.json.JSONArray;
import org.json.JSONObject;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class AiActivity extends AppCompatActivity {

    // ===== Chat UI =====
    private RecyclerView rvChat;
    private EditText etMessage;
    private Button btnSend;

    // suggestions UI
    private LinearLayout layoutSuggestions;
    private TextView tvSuggestionsTitle;
    private Button btnSug1, btnSug2, btnSug3, btnLiked, btnDisliked;

    // chat data
    private final ArrayList<ChatMessage> messages = new ArrayList<>();
    private ChatAdapter adapter;

    // bot state
    private int round = 0;
    private String lastGenre = null;
    private String lastMood = null;

    // ✅ NEW: real recs (Firestore/TMDB)
    private final ArrayList<MovieRec> currentRecs = new ArrayList<>();

    private enum State { ASK_GENRE, SHOWING_RECS, REFINE, DONE }
    private State state = State.ASK_GENRE;

    // ===== Firestore config (EDIT IF NEEDED) =====
    private static final String COLLECTION = "titles";
    private static final String FIELD_TYPE = "type";        // if you don't have it -> remove filter in query
    private static final String FIELD_TITLE = "title";
    private static final String FIELD_GENRES = "genres";
    private static final String FIELD_POSTER = "posterUrl";
    private static final String FIELD_TMDB_ID = "tmdbId";

    // ===== TMDB config =====
    private static final String TMDB_BASE = "https://api.themoviedb.org/3";
    private static final String TMDB_IMG = "https://image.tmdb.org/t/p/w500";
    private final OkHttpClient tmdbClient = new OkHttpClient();

    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ai);

        db = FirebaseFirestore.getInstance();

        // views
        rvChat = findViewById(R.id.rvChat);
        etMessage = findViewById(R.id.etMessage);
        btnSend = findViewById(R.id.btnSend);

        layoutSuggestions = findViewById(R.id.layoutSuggestions);
        tvSuggestionsTitle = findViewById(R.id.tvSuggestionsTitle);
        btnSug1 = findViewById(R.id.btnSug1);
        btnSug2 = findViewById(R.id.btnSug2);
        btnSug3 = findViewById(R.id.btnSug3);
        btnLiked = findViewById(R.id.btnLiked);
        btnDisliked = findViewById(R.id.btnDisliked);

        adapter = new ChatAdapter(messages);
        rvChat.setLayoutManager(new LinearLayoutManager(this));
        rvChat.setAdapter(adapter);

        addBot("היי! מה נשמע 😊 כתבי מה בא לך לראות (למשל: \"משהו מצחיק\" / \"אקשן\" / \"רומנטי\" / \"מפחיד\")");

        btnSend.setOnClickListener(v -> {
            String text = etMessage.getText().toString().trim();
            if (text.isEmpty()) return;
            etMessage.setText("");

            addUser(text);
            handleUserText(text);
        });

        btnSug1.setOnClickListener(v -> openSuggestion(0));
        btnSug2.setOnClickListener(v -> openSuggestion(1));
        btnSug3.setOnClickListener(v -> openSuggestion(2));

        btnLiked.setOnClickListener(v -> {
            addUser("אהבתי");
            handleUserText("אהבתי");
        });

        btnDisliked.setOnClickListener(v -> {
            addUser("לא אהבתי");
            handleUserText("לא אהבתי");
        });
    }

    private void handleUserText(String userText) {
        String t = userText == null ? "" : userText.toLowerCase(Locale.ROOT).trim();

        if (state == State.DONE) {
            addBot("סגרנו 😊 אם תרצי עוד המלצות פשוט תכתבי משהו חדש.");
            return;
        }

        // ✅ קודם DISLIKED
        if (containsAny(t, "לא אהבתי", "לא משהו", "nah", "nope", "didn't like")) {
            state = State.REFINE;
            hideSuggestions();
            addBot("סבבה 🙂 מה לשנות? יותר מצחיק / יותר מותח / בלי אימה / משהו רגוע?");
            return;
        }

        // ✅ אחרי זה LIKED
        if (!t.startsWith("לא") && containsAny(t, "אהבתי", "כן", "סגור", "מעולה", "perfect", "i like", "liked", "yes")) {
            state = State.DONE;
            hideSuggestions();
            addBot("יאללהה! שמחה שאהבת 🎬 רוצה עוד המלצות בז׳אנר אחר?");
            return;
        }

        // ====== AI detect genre ======
        if (state == State.ASK_GENRE || state == State.REFINE) {
            addBot("שנייה אני חושבת 🤖 ...");

            // ✅ כאן יש לך 2 אופציות:
            // A) לשים את ה-ZERO-SHOT שעשית ב-HfClient.detectGenreZeroShot(...)
            // B) להשאיר כרגע את classifyText + Local fallback.
            //
            // אני משאירה לך כאן את B כדי שזה ירוץ מיד,
            // ואת יכולה להחליף ל-ZERO-SHOT בשורה אחת.

            HfClient.classifyText(userText, new HfClient.HfCallback() {
                @Override
                public void onSuccess(String label, double score) {
                    runOnUiThread(() -> {
                        addBot("ה-AI זיהה: " + label + " (" + String.format(Locale.ROOT, "%.2f", score) + ")");
                        round++;

                        // ✅ TEMP mapping (עד שתעברי ל-ZeroShot)
                        String lower = userText.toLowerCase(Locale.ROOT);

                        if (containsAny(lower, "deep", "think", "serious", "emotional", "meaningful", "dark", "intense")) {
                            lastGenre = "drama";
                            lastMood = "serious";
                        } else if ("POSITIVE".equalsIgnoreCase(label)) {
                            lastGenre = "comedy";
                            lastMood = "funny";
                        } else if ("NEGATIVE".equalsIgnoreCase(label)) {
                            lastGenre = "drama";
                            lastMood = "emotional";
                        } else {
                            lastGenre = "romance";
                            lastMood = "calm";
                        }

                        addBot("קלטתי 😉 הולכים על " + lastGenre + ". מביאה 3 המלצות אמיתיות…");
                        fetchSmartRecommendations(lastGenre);

                        state = State.SHOWING_RECS;
                    });
                }

                @Override
                public void onError(String error) {
                    runOnUiThread(() -> {
                        Log.e("HF_AI", "HF ERROR CALLBACK = " + error);
                        addBot("ה-AI לא זמין כרגע 😅 אז אני ממשיכה עם גיבוי מקומי");

                        LocalTextClassifier.Result r = LocalTextClassifier.classify(userText);
                        lastGenre = r.genre;
                        lastMood = r.mood;
                        round++;

                        addBot("קלטתי 😉 הולכים על " + lastGenre + ". מביאה 3 המלצות אמיתיות…");
                        fetchSmartRecommendations(lastGenre);

                        state = State.SHOWING_RECS;
                    });
                }
            });

            return;
        }

        addBot("רוצה לבחור משהו? לחצי על אחד הסרטים, או כתבי 'אהבתי' / 'לא אהבתי'.");
    }

    // =========================================================
    // ✅ STEP 3: Firestore first, then TMDB fallback    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
    // =========================================================

    private interface ListCb { void onDone(ArrayList<MovieRec> list); }
    private interface ErrCb { void onErr(String err); }

    private void fetchSmartRecommendations(String genreLabel) {
        hideSuggestions();
        currentRecs.clear();

        fetchFromFirestoreByGenre(genreLabel, fsList -> {
            if (fsList.size() >= 3) {
                currentRecs.addAll(fsList.subList(0, 3));
                runOnUiThread(() -> showRecommendations(currentRecs));
                return;
            }

            int need = 3 - fsList.size();
            fetchFromTmdbByGenre(genreLabel, need, tmdbList -> {
                ArrayList<MovieRec> merged = new ArrayList<>(fsList);

                for (MovieRec r : tmdbList) {
                    if (!containsRec(merged, r)) merged.add(r);
                    if (merged.size() == 3) break;
                }

                currentRecs.clear();
                currentRecs.addAll(merged);
                runOnUiThread(() -> showRecommendations(currentRecs));

            }, err -> runOnUiThread(() -> {
                addBot("TMDB לא עבד 😅 (" + err + ") מציגה את מה שיש מהאפליקציה.");
                currentRecs.clear();
                currentRecs.addAll(fsList);
                showRecommendations(currentRecs);
            }));

        }, err -> {
            runOnUiThread(() -> addBot("Firestore נפל 😅 (" + err + ") מנסה TMDB…"));
            fetchFromTmdbByGenre(genreLabel, 3, tmdbList -> {
                currentRecs.clear();
                currentRecs.addAll(tmdbList);
                runOnUiThread(() -> showRecommendations(currentRecs));
            }, err2 -> runOnUiThread(() -> addBot("גם TMDB לא עבד 😭 " + err2)));
        });
    }

    private boolean containsRec(List<MovieRec> list, MovieRec rec) {
        for (MovieRec x : list) {
            if (x.tmdbId != null && rec.tmdbId != null && x.tmdbId.equals(rec.tmdbId)) return true;
            if (x.title != null && rec.title != null && x.title.equalsIgnoreCase(rec.title)) return true;
        }
        return false;
    }

    // ---------- Firestore ----------
    private void fetchFromFirestoreByGenre(String genreLabel, ListCb ok, ErrCb bad) {
        String g1 = normalizeGenreLower(genreLabel); // comedy
        String g2 = capitalizeFirst(g1);             // Comedy

        ArrayList<MovieRec> out = new ArrayList<>();

        queryFirestoreByGenreValue(g1, res1 -> {
            out.addAll(res1);
            if (out.size() >= 3) {
                ok.onDone(trimTo3(out));
                return;
            }

            if (g2.equals(g1)) {
                ok.onDone(trimTo3(out));
                return;
            }

            queryFirestoreByGenreValue(g2, res2 -> {
                for (MovieRec r : res2) {
                    if (!containsRec(out, r)) out.add(r);
                    if (out.size() == 3) break;
                }
                ok.onDone(trimTo3(out));
            }, bad);

        }, bad);
    }

    private void queryFirestoreByGenreValue(String genreValue, ListCb ok, ErrCb bad) {
        Query q = db.collection(COLLECTION)
                .whereArrayContains(FIELD_GENRES, genreValue)
                .limit(3);

        // אם אין אצלך type -> תמחקי את השורה הזאת
        q = q.whereEqualTo(FIELD_TYPE, "movie");

        q.get()
                .addOnSuccessListener(snap -> {
                    ArrayList<MovieRec> list = new ArrayList<>();
                    for (DocumentSnapshot d : snap.getDocuments()) {
                        String title = d.getString(FIELD_TITLE);
                        String poster = d.getString(FIELD_POSTER);

                        Long tmdbId = null;
                        Object rawId = d.get(FIELD_TMDB_ID);
                        if (rawId instanceof Number) tmdbId = ((Number) rawId).longValue();

                        if (title != null && !title.trim().isEmpty()) {
                            list.add(new MovieRec(title, poster, tmdbId, "firestore"));
                        }
                    }
                    ok.onDone(list);
                })
                .addOnFailureListener(e -> bad.onErr(e.getMessage()));
    }

    private ArrayList<MovieRec> trimTo3(ArrayList<MovieRec> in) {
        if (in.size() <= 3) return in;
        return new ArrayList<>(in.subList(0, 3));
    }

    private String normalizeGenreLower(String s) {
        if (s == null) return "";
        s = s.trim().toLowerCase(Locale.ROOT);
        if (s.equals("sci fi") || s.equals("sci_fi") || s.equals("science-fiction")) return "sci-fi";
        return s;
    }

    private String capitalizeFirst(String s) {
        if (s == null || s.isEmpty()) return s;
        return s.substring(0, 1).toUpperCase(Locale.ROOT) + s.substring(1);
    }

    // ---------- TMDB ----------
    private void fetchFromTmdbByGenre(String genreLabel, int limit, ListCb ok, ErrCb bad) {
        String apiKey = BuildConfig.TMDB_API_KEY;
        if (apiKey == null || apiKey.trim().isEmpty()) {
            bad.onErr("TMDB_API_KEY ריק");
            return;
        }

        int genreId = tmdbGenreIdFor(genreLabel);
        if (genreId == 0) {
            bad.onErr("Unknown genre: " + genreLabel);
            return;
        }

        String url = TMDB_BASE + "/discover/movie"
                + "?api_key=" + apiKey
                + "&with_genres=" + genreId
                + "&language=en-US"
                + "&sort_by=popularity.desc"
                + "&page=1";

        Request req = new Request.Builder().url(url).get().build();

        tmdbClient.newCall(req).enqueue(new Callback() {
            @Override public void onFailure(@NonNull Call call, @NonNull java.io.IOException e) {
                bad.onErr("Network fail: " + e.getMessage());
            }

            @Override public void onResponse(@NonNull Call call, @NonNull Response res) throws java.io.IOException {
                String raw = res.body() != null ? res.body().string() : "";
                if (!res.isSuccessful()) {
                    bad.onErr("HTTP " + res.code() + ": " + safePreview(raw));
                    return;
                }

                try {
                    JSONObject obj = new JSONObject(raw);
                    JSONArray results = obj.getJSONArray("results");

                    ArrayList<MovieRec> out = new ArrayList<>();
                    for (int i = 0; i < results.length() && out.size() < limit; i++) {
                        JSONObject m = results.getJSONObject(i);
                        String title = m.optString("title", "");
                        long id = m.optLong("id", 0);
                        String posterPath = m.optString("poster_path", null);
                        String posterUrl = (posterPath != null && !posterPath.equals("null") && !posterPath.isEmpty())
                                ? (TMDB_IMG + posterPath)
                                : null;

                        if (title != null && !title.trim().isEmpty() && id != 0) {
                            out.add(new MovieRec(title, posterUrl, id, "tmdb"));
                        }
                    }

                    ok.onDone(out);

                } catch (Exception e) {
                    bad.onErr("Parse error: " + e.getMessage());
                }
            }
        });
    }

    private String safePreview(String s) {
        if (s == null) return "";
        s = s.replace("\n", " ").trim();
        return s.length() <= 160 ? s : s.substring(0, 160) + "…";
    }

    private static int tmdbGenreIdFor(String label) {
        if (label == null) return 0;
        switch (label.toLowerCase(Locale.ROOT)) {
            case "action": return 28;
            case "comedy": return 35;
            case "drama": return 18;
            case "horror": return 27;
            case "romance": return 10749;
            case "thriller": return 53;
            case "sci-fi":
            case "scifi":
            case "science fiction": return 878;
            default: return 0;
        }
    }

    // =========================================================
    // ✅ Suggestions UI (buttons)
    // =========================================================
    private void showRecommendations(List<MovieRec> recs) {
        if (recs == null || recs.isEmpty()) {
            addBot("לא מצאתי המלצות כרגע 😅 נסי משפט אחר.");
            hideSuggestions();
            return;
        }

        layoutSuggestions.setVisibility(View.VISIBLE);
        tvSuggestionsTitle.setText("הנה 3 הצעות 🎬");

        bindButton(btnSug1, recs.size() > 0 ? recs.get(0) : null);
        bindButton(btnSug2, recs.size() > 1 ? recs.get(1) : null);
        bindButton(btnSug3, recs.size() > 2 ? recs.get(2) : null);

        scrollToBottom();
    }

    private void bindButton(Button b, MovieRec rec) {
        if (rec == null) {
            b.setVisibility(View.GONE);
            return;
        }
        b.setVisibility(View.VISIBLE);

        String src = rec.source != null ? (" (" + rec.source + ")") : "";
        b.setText("🎥 " + rec.title + src);
    }

    private void hideSuggestions() {
        layoutSuggestions.setVisibility(View.GONE);
    }

    private void openSuggestion(int index) {
        if (index < 0 || index >= currentRecs.size()) return;

        MovieRec m = currentRecs.get(index);

        addUser("בחרתי: " + m.title);
        addBot("פותחת לך טריילר/חיפוש 🎬 ואם אהבת — תלחצי 'אהבתי'.");

        // ✅ הכי פשוט: יוטיוב חיפוש טריילר
        openTrailerSearch(m.title);

        // אם תרצי לפתוח מסך פרטים שלך:
        // Intent i = new Intent(AiActivity.this, MovieContentActivity.class);
        // i.putExtra(MovieContentActivity.EXTRA_MOVIE_TITLE, m.title);
        // i.putExtra(MovieContentActivity.EXTRA_TRAILER_URL, "...");
        // startActivity(i);
    }

    private void openTrailerSearch(String title) {
        try {
            Uri uri = Uri.parse("https://www.youtube.com/results?search_query="
                    + URLEncoder.encode(title + " trailer", "UTF-8"));
            startActivity(new Intent(Intent.ACTION_VIEW, uri));
        } catch (Exception ignored) {}
    }

    // =========================================================
    // Chat helpers
    // =========================================================
    private void addBot(String text) {
        messages.add(new ChatMessage(text, true));
        adapter.notifyItemInserted(messages.size() - 1);
        scrollToBottom();
    }

    private void addUser(String text) {
        messages.add(new ChatMessage(text, false));
        adapter.notifyItemInserted(messages.size() - 1);
        scrollToBottom();
    }

    private void scrollToBottom() {
        rvChat.post(() -> rvChat.scrollToPosition(messages.size() - 1));
    }

    private boolean containsAny(String text, String... arr) {
        for (String a : arr) if (text.contains(a)) return true;
        return false;
    }

    // =========================================================
    // Models + Adapter
    // =========================================================
    public static class ChatMessage {
        public final String text;
        public final boolean fromBot;
        public ChatMessage(String text, boolean fromBot) {
            this.text = text;
            this.fromBot = fromBot;
        }
    }

    // ✅ Real recommendation model
    public static class MovieRec {
        public String title;
        public String posterUrl;  // optional
        public Long tmdbId;       // optional
        public String source;     // "firestore" / "tmdb"

        public MovieRec(String title, String posterUrl, Long tmdbId, String source) {
            this.title = title;
            this.posterUrl = posterUrl;
            this.tmdbId = tmdbId;
            this.source = source;
        }
    }

    private static class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.VH> {

        private final ArrayList<ChatMessage> data;

        ChatAdapter(ArrayList<ChatMessage> data) {
            this.data = data;
        }

        @NonNull
        @Override
        public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            TextView tv = new TextView(parent.getContext());
            tv.setTextSize(16f);
            tv.setPadding(18, 12, 18, 12);

            RecyclerView.LayoutParams lp = new RecyclerView.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            );
            lp.bottomMargin = 12;
            tv.setLayoutParams(lp);

            return new VH(tv);
        }

        @Override
        public void onBindViewHolder(@NonNull VH holder, int position) {
            ChatMessage msg = data.get(position);
            TextView tv = (TextView) holder.itemView;
            tv.setText(msg.text);

            RecyclerView.LayoutParams lp = (RecyclerView.LayoutParams) tv.getLayoutParams();

            if (msg.fromBot) {
                tv.setBackgroundResource(android.R.drawable.dialog_holo_light_frame);
                tv.setGravity(Gravity.START);
                lp.leftMargin = 0;
                lp.rightMargin = 80;
            } else {
                tv.setBackgroundResource(android.R.drawable.dialog_holo_dark_frame);
                tv.setGravity(Gravity.END);
                lp.leftMargin = 80;
                lp.rightMargin = 0;
            }
            tv.setLayoutParams(lp);
        }

        @Override
        public int getItemCount() {
            return data.size();
        }

        static class VH extends RecyclerView.ViewHolder {
            VH(@NonNull View itemView) { super(itemView); }
        }
    }
}
