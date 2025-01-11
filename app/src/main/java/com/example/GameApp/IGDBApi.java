package com.example.GameApp;

import com.example.GameApp.ClassObjectes.Artwork;
import com.example.GameApp.ClassObjectes.Companies;
import com.example.GameApp.ClassObjectes.Cover;
import com.example.GameApp.ClassObjectes.Game;
import com.example.GameApp.ClassObjectes.Genres;
import com.example.GameApp.ClassObjectes.InvolvedCompanies;
import com.example.GameApp.ClassObjectes.Platforms;

import java.util.List;

import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface IGDBApi {

    @Headers({
            "Content-Type: text/plain"
    })
    @POST("games")
    Call<List<Game>> getGames(
            @Body RequestBody body,
            @retrofit2.http.Header("Client-ID") String clientId,
            @retrofit2.http.Header("Authorization") String authToken
    );

    @Headers({
            "Content-Type: text/plain"
    })
    @POST("covers")
    Call<List<Cover>> getCovers(
            @Body RequestBody body,
            @retrofit2.http.Header("Client-ID") String clientId,
            @retrofit2.http.Header("Authorization") String authToken
    );

}
