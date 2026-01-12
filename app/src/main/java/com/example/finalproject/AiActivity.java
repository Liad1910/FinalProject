package com.example.finalproject;

import android.content.Intent;
import android.os.Bundle;
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

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
    private String lastGenre = null; // comedy/action/...
    private String lastMood = null;  // funny/chill/...
    private List<AiMovie> currentSuggestions = new ArrayList<>();

    private enum State { ASK_GENRE, SHOWING_RECS, REFINE, DONE }
    private State state = State.ASK_GENRE;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ai);

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

        // recycler
        adapter = new ChatAdapter(messages);
        rvChat.setLayoutManager(new LinearLayoutManager(this));
        rvChat.setAdapter(adapter);

        // bot opening
        addBot("היי! מה נשמע 😊 איזה ז׳אנר של סרט תרצי לראות היום? (קומדיה/אקשן/רומנטי/אימה וכו׳)");

        // send handler
        btnSend.setOnClickListener(v -> {
            String text = etMessage.getText().toString().trim();
            if (text.isEmpty()) return;
            etMessage.setText("");

            addUser(text);
            handleUserText(text);
        });

        // suggestions buttons
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

    // ====== BOT LOGIC ======
    private void handleUserText(String userText) {
        String t = userText == null ? "" : userText.toLowerCase(Locale.ROOT).trim();

        if (state == State.DONE) {
            addBot("סגרנו 😊 אם תרצי עוד המלצות פשוט תכתבי ז׳אנר חדש.");
            return;
        }

        // ✅ קודם DISLIKED (כי "לא אהבתי" מכיל "אהבתי")
        if (containsAny(t, "לא אהבתי", "לא", "לא משהו", "nah", "nope", "didn't like")) {
            state = State.REFINE;
            hideSuggestions();
            addBot("סבבה 🙂 מה לשנות? יותר מצחיק / יותר מותח / בלי אימה / משהו רגוע?");
            return;
        }

        // ✅ אחרי זה LIKED (ולוודא שלא מתחיל ב"לא")
        if (!t.startsWith("לא") && containsAny(t, "אהבתי", "כן", "סגור", "מעולה", "perfect", "i like", "liked", "yes")) {
            state = State.DONE;
            hideSuggestions();
            addBot("יאללהה! שמחה שאהבת 🎬 רוצה עוד המלצות בז׳אנר אחר?");
            return;
        }

        // ====== כאן ה-AI האמיתי ======
        if (state == State.ASK_GENRE || state == State.REFINE) {

            addBot("שנייה אני חושבת 🤖 ...");

            HfClient.classifyText(userText, new HfClient.HfCallback() {
                @Override
                public void onSuccess(String label, double score) {
                    runOnUiThread(() -> {

                        addBot("ה-AI זיהה: " + label + " (" + String.format(Locale.ROOT, "%.2f", score) + ")");

                        round++;

                        if ("POSITIVE".equalsIgnoreCase(label)) {
                            lastGenre = "comedy";
                            lastMood = "funny";
                        } else if ("NEGATIVE".equalsIgnoreCase(label)) {
                            lastGenre = "drama";
                            lastMood = "emotional";
                        } else {
                            lastGenre = "comedy";
                            lastMood = "feel_good";
                        }

                        addBot("קלטתי 😉 הולכים על " + lastGenre + " (" + lastMood + "). הנה 3 הצעות!");
                        currentSuggestions = getMoviesForGenre(lastGenre, round);
                        showSuggestions(currentSuggestions);

                        state = State.SHOWING_RECS;
                    });
                }

                @Override
                public void onError(String error) {
                    runOnUiThread(() -> {
                        addBot("נפל ה-AI 😅 (" + error + ") אז אני ממשיכה עם גיבוי מקומי");

                        LocalTextClassifier.Result r = LocalTextClassifier.classify(userText);
                        lastGenre = r.genre;
                        lastMood = r.mood;
                        round++;

                        addBot("קלטתי 😉 הולכים על " + lastGenre + " (" + lastMood + "). הנה 3 הצעות!");
                        currentSuggestions = getMoviesForGenre(lastGenre, round);
                        showSuggestions(currentSuggestions);

                        state = State.SHOWING_RECS;
                    });
                }
            });


            return;
        }

        // default in SHOWING_RECS: nudge feedback
        addBot("רוצה לבחור משהו? לחצי על אחד הסרטים, או כתבי 'אהבתי' / 'לא אהבתי'.");
    }

    // ====== Suggestions UI ======
    private void showSuggestions(List<AiMovie> list) {
        if (list == null || list.size() < 3) return;

        tvSuggestionsTitle.setText("הנה 3 הצעות 🎬");
        btnSug1.setText("🎥 " + list.get(0).title);
        btnSug2.setText("🎥 " + list.get(1).title);
        btnSug3.setText("🎥 " + list.get(2).title);

        layoutSuggestions.setVisibility(View.VISIBLE);
        scrollToBottom();
    }

    private void hideSuggestions() {
        layoutSuggestions.setVisibility(View.GONE);
    }

    private void openSuggestion(int index) {
        if (currentSuggestions == null || index < 0 || index >= currentSuggestions.size()) return;

        AiMovie m = currentSuggestions.get(index);

        addUser("בחרתי: " + m.title);
        addBot("פותחת לך את עמוד הסרט 🎬 ואם אהבת — תלחצי 'אהבתי'.");

        Intent i = new Intent(AiActivity.this, MovieContentActivity.class);
        i.putExtra(MovieContentActivity.EXTRA_MOVIE_ID, m.id);
        i.putExtra(MovieContentActivity.EXTRA_MOVIE_TITLE, m.title);
        i.putExtra(MovieContentActivity.EXTRA_TRAILER_URL, m.trailerUrl);
        i.putExtra(MovieContentActivity.EXTRA_POSTER_RES_ID, m.posterResId);
        startActivity(i);
    }

    // ====== Movies pool ======
    private static class AiMovie {
        String id;
        String title;
        String trailerUrl;
        int posterResId;

        AiMovie(String id, String title, String trailerUrl, int posterResId) {
            this.id = id;
            this.title = title;
            this.trailerUrl = trailerUrl;
            this.posterResId = posterResId;
        }
    }

    // נותן 3 הצעות, וכל round מחליף את השלישייה
    private List<AiMovie> getMoviesForGenre(String genre, int round) {

        List<AiMovie> pool;

        switch (genre) {
            case "comedy":
                pool = Arrays.asList(
                        new AiMovie("mean_girls_2004", "Mean Girls",
                                "https://www.youtube.com/results?search_query=Mean+Girls+trailer",
                                R.drawable.mean_girls_poster),
                        new AiMovie("wrong_missy_2020", "The Wrong Missy",
                                "https://www.youtube.com/results?search_query=The+Wrong+Missy+trailer",
                                R.drawable.wrong_missy_poster),
                        new AiMovie("white_chicks_2004", "White Chicks",
                                "https://www.youtube.com/results?search_query=White+Chicks+trailer",
                                R.drawable.white_chicks_poster),
                        new AiMovie("barbie_2023", "Barbie",
                                "https://www.youtube.com/results?search_query=Barbie+2023+trailer",
                                R.drawable.barbie_poster),
                        new AiMovie("up_2009", "Up",
                                "https://www.youtube.com/results?search_query=Up+trailer",
                                R.drawable.up_poster),
                        new AiMovie("pirates_2003", "Pirates of the Caribbean",
                                "https://www.youtube.com/results?search_query=Pirates+of+the+Caribbean+trailer",
                                R.drawable.pirates1_poster)
                );
                break;

            case "action":
                pool = Arrays.asList(
                        new AiMovie("dark_knight_2008", "The Dark Knight",
                                "https://www.youtube.com/results?search_query=The+Dark+Knight+trailer",
                                R.drawable.dark_knight_poster),
                        new AiMovie("inception_2010", "Inception",
                                "https://www.youtube.com/results?search_query=Inception+trailer",
                                R.drawable.inception_poster),
                        new AiMovie("endgame_2019", "Avengers: Endgame",
                                "https://www.youtube.com/results?search_query=Avengers+Endgame+trailer",
                                R.drawable.endgame_poster),
                        new AiMovie("avatar_2009", "Avatar",
                                "https://www.youtube.com/results?search_query=Avatar+2009+trailer",
                                R.drawable.avatar_poster),
                        new AiMovie("top_gun_2022", "Top Gun: Maverick",
                                "https://www.youtube.com/results?search_query=Top+Gun+Maverick+trailer",
                                R.drawable.top_gun_maverick_poster),
                        new AiMovie("matrix_1999", "The Matrix",
                                "https://www.youtube.com/results?search_query=The+Matrix+trailer",
                                R.drawable.matrix_poster)
                );
                break;

            case "romance":
                pool = Arrays.asList(
                        new AiMovie("titanic_1997", "Titanic",
                                "https://www.youtube.com/results?search_query=Titanic+trailer",
                                R.drawable.titanic_poster),
                        new AiMovie("lala_land_2016", "La La Land",
                                "https://www.youtube.com/results?search_query=La+La+Land+trailer",
                                R.drawable.la_la_land_poster),
                        new AiMovie("to_all_the_boys_2018", "To All the Boys I've Loved Before",
                                "https://www.youtube.com/results?search_query=To+All+the+Boys+trailer",
                                R.drawable.to_all_the_boys_poster)
                );
                break;

            case "horror":
                pool = Arrays.asList(
                        new AiMovie("it_2017", "IT",
                                "https://www.youtube.com/watch?v=FnCdOQsX5kc",
                                R.drawable.it_poster),
                        new AiMovie("the_meg_2018", "The Meg",
                                "https://www.youtube.com/results?search_query=The+Meg+trailer",
                                R.drawable.the_meg_poster),
                        new AiMovie("horror_pick", "Horror Pick",
                                "https://www.youtube.com/results?search_query=horror+movie+trailer",
                                R.drawable.it_poster)
                );
                break;

            default:
                pool = Arrays.asList(
                        new AiMovie("forrest_gump_1994", "Forrest Gump",
                                "https://www.youtube.com/results?search_query=Forrest+Gump+trailer",
                                R.drawable.forrest_gump_poster),
                        new AiMovie("titanic_1997", "Titanic",
                                "https://www.youtube.com/results?search_query=Titanic+trailer",
                                R.drawable.titanic_poster),
                        new AiMovie("inception_2010", "Inception",
                                "https://www.youtube.com/results?search_query=Inception+trailer",
                                R.drawable.inception_poster)
                );
                break;
        }

        int n = pool.size();
        if (n <= 3) return pool;

        int start = (round * 3) % n;
        AiMovie a = pool.get(start % n);
        AiMovie b = pool.get((start + 1) % n);
        AiMovie c = pool.get((start + 2) % n);

        return Arrays.asList(a, b, c);
    }

    // ===== Chat helpers =====
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

    // ===== Models + Adapter =====
    public static class ChatMessage {
        public final String text;
        public final boolean fromBot;
        public ChatMessage(String text, boolean fromBot) {
            this.text = text;
            this.fromBot = fromBot;
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
