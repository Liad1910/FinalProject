package com.example.finalproject;

import java.util.Locale;

public class LocalTextClassifier {

    public static class Result {
        public String mood;
        public String genre;

        public Result(String mood, String genre) {
            this.mood = mood;
            this.genre = genre;
        }
    }

    public static Result classify(String input) {
        if (input == null) return new Result("feel_good", "drama");

        String text = normalize(input);

        // ====== MOOD ======
        String mood = "feel_good";

        if (containsAny(text,
                // Hebrew
                "מצחיק", "קורע", "צחוקים", "חח",
                // English
                "funny", "hilarious", "comedy", "laugh", "lol"
        )) mood = "funny";

        else if (containsAny(text,
                "רומנטי", "אהבה", "דייט",
                "romantic", "romance", "love", "date"
        )) mood = "romantic";

        else if (containsAny(text,
                "מרגש", "דמעות", "לב", "סוף טוב",
                "emotional", "moving", "tear", "heart", "uplifting", "happy ending"
        )) mood = "emotional";

        else if (containsAny(text,
                "עצוב", "דיכאון", "שובר",
                "sad", "depressing", "heartbreaking"
        )) mood = "sad";

        else if (containsAny(text,
                "מותח", "מתח", "לחץ",
                "tense", "thriller", "suspense", "edge of my seat"
        )) mood = "tense";

        else if (containsAny(text,
                "מפחיד", "אימה", "סיוט", "ג'אמפסקייר",
                "scary", "horror", "terrifying", "jump scare", "jumpscare"
        )) mood = "scary";

        else if (containsAny(text,
                "רגוע", "זורם", "צ'יל", "נעים",
                "chill", "relaxing", "calm", "easy"
        )) mood = "chill";

        else if (containsAny(text,
                "מעורר השראה", "מוטיבציה", "להצליח",
                "inspiring", "motivation", "inspirational"
        )) mood = "inspiring";

        else if (containsAny(text,
                "טוויסט", "מבלבל", "מוח", "פסיכי",
                "mind bending", "mind-bending", "twist", "confusing"
        )) mood = "mind_bending";


        // ====== GENRE ======
        // בסיס לפי מילות מפתח
        String genre = "drama";

        if (containsAny(text,
                "קומדיה", "מצחיק",
                "comedy", "funny"
        )) genre = "comedy";

        else if (containsAny(text,
                "אקשן", "פעולה", "פיצוצים",
                "action", "fight", "explosion"
        )) genre = "action";

        else if (containsAny(text,
                "רומנטי", "אהבה",
                "romance", "romantic", "love story"
        )) genre = "romance";

        else if (containsAny(text,
                "אימה", "מפחיד", "זומבים",
                "horror", "scary", "zombie"
        )) genre = "horror";

        else if (containsAny(text,
                "מתח", "מותח", "חקירה", "רצח",
                "thriller", "mystery", "crime", "detective", "murder"
        )) genre = "thriller";

        else if (containsAny(text,
                "מדע בדיוני", "חלל", "חייזרים",
                "sci-fi", "sci fi", "scifi", "space", "alien"
        )) genre = "sci_fi";

        else if (containsAny(text,
                "פנטזיה", "קסם", "דרקונים",
                "fantasy", "magic", "dragon"
        )) genre = "fantasy";

        else if (containsAny(text,
                "אנימציה", "מצויר", "דיסני", "פיקסאר",
                "animation", "animated", "pixar", "disney"
        )) genre = "animation";

        else if (containsAny(text,
                "משפחה", "ילדים",
                "family", "kids"
        )) genre = "family";


        // ====== חיזוק: אם mood=funny ועדיין drama -> להעדיף comedy
        if ("funny".equals(mood) && "drama".equals(genre)) genre = "comedy";
        if ("romantic".equals(mood) && "drama".equals(genre)) genre = "romance";
        if ("scary".equals(mood) && "drama".equals(genre)) genre = "horror";
        if ("tense".equals(mood) && "drama".equals(genre)) genre = "thriller";

        return new Result(mood, genre);
    }

    private static String normalize(String s) {
        return s.toLowerCase(Locale.ROOT)
                .replace("׳", "'")
                .replaceAll("[^\\p{L}\\p{N}\\s'\\-]", " ")
                .replaceAll("\\s+", " ")
                .trim();
    }

    private static boolean containsAny(String text, String... words) {
        for (String w : words) {
            if (w == null) continue;
            String ww = w.toLowerCase(Locale.ROOT);
            if (text.contains(ww)) return true;
        }
        return false;
    }
}
