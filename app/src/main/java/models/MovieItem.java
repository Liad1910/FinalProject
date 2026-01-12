package models; // תעדכני לפי ה-package שלך

public class MovieItem {
    public String id;        // tmdbId או documentId
    public String title;
    public String posterUrl;
    public String genreKey;  // "comedy", "action"...
    public String source;    // "tmdb" / "user"
    public double rating;    // אופציונלי

    // חובה ל-Firestore לפעמים: constructor ריק
    public MovieItem() {}

    public MovieItem(String id, String title, String posterUrl, String genreKey, String source, double rating) {
        this.id = id;
        this.title = title;
        this.posterUrl = posterUrl;
        this.genreKey = genreKey;
        this.source = source;
        this.rating = rating;
    }
}
