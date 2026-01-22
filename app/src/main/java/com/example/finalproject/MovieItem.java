package com.example.finalproject;

import java.util.List;

public class MovieItem {
    public String id;
    public String title;
    public List<String> genres;

    // poster: או URL או drawable
    public String posterUrl;
    public int posterResId;

    public String trailerUrl;

    public boolean isUserTitle; // אם הגיע מ-Firestore
}
