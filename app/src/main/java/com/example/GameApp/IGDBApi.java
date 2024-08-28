package com.example.GameApp;

import com.example.GameApp.ClassObjectes.Artwork;
import com.example.GameApp.ClassObjectes.Cover;
import com.example.GameApp.ClassObjectes.Game;

import java.util.List;

import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface IGDBApi {
    @Headers({
            "Client-ID: b9y1tvf5218fua1xj6h9abthj0fmdm",
            "Authorization: Bearer mzgh1skxwrndovrl17j3x1e91r5s8b",
            "Content-Type: text/plain"
    })
    @POST("games")
    Call<List<Game>> getGames(@Body RequestBody body);
    @Headers({
            "Client-ID: b9y1tvf5218fua1xj6h9abthj0fmdm",
            "Authorization: Bearer mzgh1skxwrndovrl17j3x1e91r5s8b",
            "Content-Type: text/plain"
    })

    @POST("covers")
    Call<List<Cover>> getCovers(@Body RequestBody body);
    @Headers({
            "Client-ID: b9y1tvf5218fua1xj6h9abthj0fmdm",
            "Authorization: Bearer mzgh1skxwrndovrl17j3x1e91r5s8b",
            "Content-Type: text/plain"
    })
    @POST("artworks")
    Call<List<Artwork>> getArtwork(@Body RequestBody body);

}
