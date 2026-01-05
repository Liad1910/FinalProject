package com.example.finalproject;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class TmdbSearchResponse {
    @SerializedName("results")
    public List<TmdbResult> results;

    public static class TmdbResult {
        @SerializedName("id")
        public long id;

        @SerializedName("poster_path")
        public String posterPath; // למשל "/abc123.jpg"
    }
}
