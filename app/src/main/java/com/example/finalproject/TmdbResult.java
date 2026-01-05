package com.example.finalproject;

import com.google.gson.annotations.SerializedName;

public class TmdbResult {
    public long id;

    @SerializedName("poster_path")
    public String posterPath;
}
