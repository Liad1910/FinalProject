package com.example.finalproject;

import java.util.ArrayList;
import java.util.List;

public class ChatBotEngine {

    public enum State {
        GREETING,
        ASK_GENRE,
        SHOW_RECS,
        ASK_FEEDBACK,
        REFINE,
        DONE
    }

    private State state = State.GREETING;

    // זיכרון קטן על המשתמש
    public String genre;   // comedy/action/...
    public String mood;    // funny/chill/...
    public int round = 0;

    public String getOpeningMessage() {
        state = State.ASK_GENRE;
        return "היי! מה נשמע 😊 איזה ז׳אנר של סרט תרצה לראות היום?";
    }

    // מקבל טקסט מהמשתמש ומחזיר תגובת בוט אחת (טקסט)
    public String handleUser(String userText) {

        String t = userText == null ? "" : userText.toLowerCase();

        // סיום
        if (containsAny(t, "אהבתי", "כן", "סגור", "מעולה", "perfect", "i like", "liked", "yes")) {
            state = State.DONE;
            return "יאללה! שמחה שאהבת 🎬 רוצה שאפתח לך את הטריילר או את עמוד הסרט?";
        }

        // אם המשתמש אומר לא אהבתי
        if (containsAny(t, "לא", "לא אהבתי", "לא משהו", "nah", "nope", "didn't like")) {
            state = State.REFINE;
            return "סבבה 🙂 מה לשנות? יותר מצחיק / יותר מותח / בלי אימה / משהו רגוע?";
        }

        switch (state) {

            case ASK_GENRE:
            case REFINE:
                LocalTextClassifier.Result r = LocalTextClassifier.classify(userText);
                mood = r.mood;
                genre = r.genre;
                round++;

                state = State.SHOW_RECS;
                return "קלטתי 😉 אני הולכת על " + genre + " (" + mood + "). הנה 3 הצעות!";

            case SHOW_RECS:
            case ASK_FEEDBACK:
                state = State.ASK_FEEDBACK;
                return "אהבת משהו מההצעות? כתבי 'אהבתי' אם כן, או 'לא' אם לא.";

            default:
                return "רוצה להמשיך לחפש יחד? כתבי מה בא לך 🙂";
        }
    }

    private boolean containsAny(String text, String... arr) {
        for (String a : arr) if (text.contains(a)) return true;
        return false;
    }
}

