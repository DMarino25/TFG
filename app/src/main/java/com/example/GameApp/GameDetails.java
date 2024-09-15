package com.example.GameApp;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.GameApp.ClassObjectes.Cover;
import com.example.GameApp.ClassObjectes.Game;
import com.example.GameApp.ClassObjectes.Genres;
import com.example.GameApp.ClassObjectes.Platforms;
import com.google.android.material.imageview.ShapeableImageView;

import java.util.ArrayList;
import java.util.List;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class GameDetails extends AppCompatActivity {

    private static final String TAG = "GameDetails";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game_details);

        // Referencias a las vistas en el layout
        ShapeableImageView coverImage = findViewById(R.id.portada);
        TextView descripcio = findViewById(R.id.descripció);
        TextView titol = findViewById(R.id.titol);
        ImageView back = findViewById(R.id.flecha);
        TextView genres = findViewById(R.id.genre);
        TextView platforms = findViewById(R.id.platform);

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        int coverId = getIntent().getIntExtra("coverId", -1);

        // Verificar si se ha recibido correctamente el coverId
        if (coverId == -1) {
            Toast.makeText(this, "Error: No se pudo cargar la información del juego.", Toast.LENGTH_LONG).show();
            return;
        }

        Log.d(TAG, "Cover ID recibido: " + coverId);

        IGDBApi apiService = ApiController.getClient().create(IGDBApi.class);

        // Paso 1: obtener información del cover para obtener el gameId
        String coverQuery = "fields game; where id = " + coverId + ";";
        RequestBody coverRequestBody = RequestBody.create(coverQuery, MediaType.parse("text/plain"));

        Call<List<Cover>> coverCall = apiService.getCovers(coverRequestBody);
        coverCall.enqueue(new Callback<List<Cover>>() {
            @Override
            public void onResponse(Call<List<Cover>> call, Response<List<Cover>> response) {
                if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                    int gameId = response.body().get(0).getGame();

                    Log.d(TAG, "Game ID obtenido: " + gameId);
                    getGameDetails(gameId, coverImage, titol, descripcio, genres, platforms);
                } else {
                    Log.e(TAG, "Cover details not successful or empty");
                    Toast.makeText(GameDetails.this, "No se pudieron cargar los detalles del cover.", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<List<Cover>> call, Throwable t) {
                Log.e(TAG, "API call for cover failed: " + t.getMessage());
                Toast.makeText(GameDetails.this, "Error al obtener los detalles del cover.", Toast.LENGTH_LONG).show();
            }
        });
    }

    private void getGameDetails(int gameId, ShapeableImageView coverImage, TextView titol, TextView descripcio, TextView genresView, TextView platforms) {
        IGDBApi apiService = ApiController.getClient().create(IGDBApi.class);

        // Usando StringBuilder para construir la consulta
        StringBuilder gameQueryBuilder = new StringBuilder();
        gameQueryBuilder.append("fields name, summary, cover.url, genres, platforms; "); // Solicitar también géneros
        gameQueryBuilder.append("where id = ").append(gameId).append(";");

        String gameQuery = gameQueryBuilder.toString();

        RequestBody gameRequestBody = RequestBody.create(gameQuery, MediaType.parse("text/plain"));

        Call<List<Game>> gameCall = apiService.getGames(gameRequestBody);
        gameCall.enqueue(new Callback<List<Game>>() {
            @Override
            public void onResponse(Call<List<Game>> call, Response<List<Game>> response) {
                if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                    Game game = response.body().get(0); // Obtener el primer resultado de la lista
                    if (game.getName() != null){
                        titol.setText(game.getName());
                    }
                    else{
                        if (game.getName() == null){
                            titol.setText("Titol no disponible");
                        }
                        if (game.getSummary() == null){
                            descripcio.setText("Descripció no disponible");
                        }

                    }
                    if (game.getSummary() != null){
                        descripcio.setText(game.getSummary());
                    }

                    if (game.getCover() != null && game.getCover().getUrl() != null) {
                        String imageUrl = "https:" + game.getCover().getUrl(); // URL completa de la portada
                        Glide.with(GameDetails.this).load(imageUrl).into(coverImage);
                    }

                    if (game.getGenres() != null && !game.getGenres().isEmpty()) {
                        getGenresNames(game.getGenres(), genresView);
                    }
                    if (game.getPlatforms() != null && !game.getPlatforms().isEmpty()) {
                        getPlatformNames(game.getPlatforms(), platforms);
                    }

                } else {
                    Log.e(TAG, "Game details not successful or empty");
                    Toast.makeText(GameDetails.this, "No se pudieron cargar los detalles del juego.", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<List<Game>> call, Throwable t) {
                Log.e(TAG, "API call for game failed: " + t.getMessage());
                Toast.makeText(GameDetails.this, "Error al obtener los detalles del juego.", Toast.LENGTH_LONG).show();
            }
        });
    }

    private void getGenresNames(List<Integer> genreIds, TextView genresView) {
        IGDBApi apiService = ApiController.getClient().create(IGDBApi.class);

        // Construir la consulta para obtener los nombres de los géneros
        StringBuilder genreQueryBuilder = new StringBuilder();
        genreQueryBuilder.append("fields name; where id = (");
        genreQueryBuilder.append(android.text.TextUtils.join(",", genreIds)); // Convertir la lista de IDs en una cadena separada por comas
        genreQueryBuilder.append(");");

        String genreQuery = genreQueryBuilder.toString();
        Log.d(TAG, "Genres Query: " + genreQuery); // Log para verificar la consulta de géneros

        RequestBody genreRequestBody = RequestBody.create(genreQuery, MediaType.parse("text/plain"));

        Call<List<Genres>> genreCall = apiService.getGenres(genreRequestBody); // Usar una llamada para obtener los géneros
        genreCall.enqueue(new Callback<List<Genres>>() {
            @Override
            public void onResponse(Call<List<Genres>> call, Response<List<Genres>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Genres> genreList = response.body();
                    List<String> genreNames = new ArrayList<>();

                    for (Genres genre : genreList) {
                        genreNames.add(genre.getName());
                    }

                    // Log para verificar los nombres de los géneros obtenidos
                    Log.d(TAG, "Genres obtained: " + genreNames.toString());

                    if (genreNames != null && !genreNames.isEmpty()) {
                        genresView.setText("🎮 Gèneres: " + android.text.TextUtils.join(", ", genreNames));
                    } else {
                        genresView.setText("Gèneres no disponibles");
                        Log.d(TAG, "No genre names available.");
                    }

                } else {
                    Log.e(TAG, "Genre details not successful or empty");
                }
            }

            @Override
            public void onFailure(Call<List<Genres>> call, Throwable t) {
                Log.e(TAG, "API call for genres failed: " + t.getMessage());
            }
        });
    }

    private void getPlatformNames(List<Integer> platformIds, TextView platformsView) {
        IGDBApi apiService = ApiController.getClient().create(IGDBApi.class);

        // Construir la consulta para obtener los nombres de las plataformas
        StringBuilder platformQueryBuilder = new StringBuilder();
        platformQueryBuilder.append("fields name; where id = (");
        platformQueryBuilder.append(android.text.TextUtils.join(",", platformIds));
        platformQueryBuilder.append(");");

        String platformQuery = platformQueryBuilder.toString();
        Log.d(TAG, "Platforms Query: " + platformQuery); // Log para verificar la consulta de plataformas

        RequestBody platformRequestBody = RequestBody.create(platformQuery, MediaType.parse("text/plain"));

        Call<List<Platforms>> platformCall = apiService.getPlatforms(platformRequestBody);
        platformCall.enqueue(new Callback<List<Platforms>>() {
            @Override
            public void onResponse(Call<List<Platforms>> call, Response<List<Platforms>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Platforms> platformList = response.body();
                    List<String> platformNames = new ArrayList<>();

                    for (Platforms platform : platformList) {
                        platformNames.add(platform.getName());
                    }

                    // Log para verificar los nombres de las plataformas obtenidos
                    Log.d(TAG, "Platforms obtained: " + platformNames.toString());

                    // Mostrar las plataformas en el TextView
                    if (platformNames != null && !platformNames.isEmpty()) {
                        platformsView.setText("🕹️ Plataformas: " + android.text.TextUtils.join(", ", platformNames));
                    } else {
                        platformsView.setText("Plataformas no disponibles");
                        Log.d(TAG, "No platform names available.");
                    }
                } else {
                    platformsView.setText("No está disponible");
                    Log.e(TAG, "Platform details not successful or empty");
                }
            }

            @Override
            public void onFailure(Call<List<Platforms>> call, Throwable t) {
                Log.e(TAG, "API call for platforms failed: " + t.getMessage());
            }
        });
    }

}
