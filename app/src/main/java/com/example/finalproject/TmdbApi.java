package com.example.finalproject;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface TmdbApi {

    @GET("search/movie")
    Call<TmdbSearchResponse> searchMovie(
            @Query("api_key") String apiKey,
            @Query("query") String query,
            @Query("year") Integer year
    );

    @GET("search/tv")
    Call<TmdbSearchResponse> searchTv(
            @Query("api_key") String apiKey,
            @Query("query") String query,
            @Query("first_air_date_year") Integer year
    );
}
