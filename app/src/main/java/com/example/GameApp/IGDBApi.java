package com.example.GameApp;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface IGDBApi {
    @Headers({
            "Client-ID: b9y1tvf5218fua1xj6h9abthj0fmdm",
            "Authorization: Bearer mzgh1skxwrndovrl17j3x1e91r5s8b",
            "Content-Type: application/json"
    })
    @POST("games")
    Call<List<Game>> getGames(@Body String body);
    @Headers({
            "Client-ID: b9y1tvf5218fua1xj6h9abthj0fmdm",
            "Authorization: Bearer mzgh1skxwrndovrl17j3x1e91r5s8b"
    })
    @POST("artworks")
    Call<List<Artwork>> getArtworks(@Body String body);
}
