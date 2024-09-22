package com.example.GameApp;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.GameApp.ClassObjectes.Companies;
import com.example.GameApp.ClassObjectes.Cover;
import com.example.GameApp.ClassObjectes.Game;
import com.example.GameApp.ClassObjectes.Genres;
import com.example.GameApp.ClassObjectes.InvolvedCompanies;
import com.example.GameApp.ClassObjectes.Keywords;
import com.example.GameApp.ClassObjectes.Platforms;
import com.google.android.material.imageview.ShapeableImageView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

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
        TextView genresView = findViewById(R.id.genre);
        TextView platformsView = findViewById(R.id.platform);
        TextView companiesView = findViewById(R.id.developers);
        TextView releaseView = findViewById(R.id.launching);
        TextView tagsView = findViewById(R.id.tags);

        back.setOnClickListener(v -> finish());

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
                    getGameDetails(gameId, coverImage, titol, descripcio, genresView, platformsView, companiesView, releaseView, tagsView);
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

    private void getGameDetails(int gameId, ShapeableImageView coverImage, TextView titol, TextView descripcio, TextView genresView, TextView platformsView, TextView companiesView, TextView releaseView, TextView tagsView) {
        IGDBApi apiService = ApiController.getClient().create(IGDBApi.class);

        // Modificar la consulta para obtener los nombres directamente
        StringBuilder gameQueryBuilder = new StringBuilder();
        gameQueryBuilder.append("fields name, summary,first_release_date, cover.url, genres.name, platforms.name, involved_companies.company.name, involved_companies.developer, keywords.name; ");
        gameQueryBuilder.append("where id = ").append(gameId).append(";");

        String gameQuery = gameQueryBuilder.toString();

        RequestBody gameRequestBody = RequestBody.create(gameQuery, MediaType.parse("text/plain"));

        Call<List<Game>> gameCall = apiService.getGames(gameRequestBody);
        gameCall.enqueue(new Callback<List<Game>>() {
            @Override
            public void onResponse(Call<List<Game>> call, Response<List<Game>> response) {
                if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                    Game game = response.body().get(0); // Obtener el primer resultado de la lista

                    // Manejo simplificado de nombres y descripciones
                    titol.setText(game.getName() != null ? game.getName() : "Títul no disponible");
                    descripcio.setText(game.getSummary() != null ? game.getSummary() : "Descripció no disponible");

                    if (game.getCover() != null && game.getCover().getUrl() != null) {
                        String imageUrl = "https:" + game.getCover().getUrl(); // URL completa de la portada
                        Glide.with(GameDetails.this).load(imageUrl).into(coverImage);
                    }
                    if (game.getFirstReleaseDate() > 0) {
                        long timestamp = game.getFirstReleaseDate() * 1000L;
                        Date date = new Date(timestamp);
                        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                        String releaseDate = sdf.format(date);
                        releaseView.setText("📅 Data de llançament: " + releaseDate);
                    } else {
                        releaseView.setText("Data de llançament no disponible");
                    }

                    // Manejar géneros
                    if (game.getGenres() != null && !game.getGenres().isEmpty()) {
                        List<String> genreNames = new ArrayList<>();
                        for (Genres genre : game.getGenres()) {
                            genreNames.add(genre.getName());
                        }
                        genresView.setText("🎮 Gèneres: " + android.text.TextUtils.join(", ", genreNames));
                    } else {
                        genresView.setText("Gèneres no disponibles");
                    }

                    // Manejar plataformas
                    if (game.getPlatforms() != null && !game.getPlatforms().isEmpty()) {
                        List<String> platformNames = new ArrayList<>();
                        for (Platforms platform : game.getPlatforms()) {
                            platformNames.add(platform.getName());
                        }
                        platformsView.setText("🕹️ Plataformes: " + android.text.TextUtils.join(", ", platformNames));
                    } else {
                        platformsView.setText("Plataformes no disponibles");
                    }
                    if (game.getKeywords() != null && !game.getKeywords().isEmpty()) {
                        List<String> keywordNames = new ArrayList<>();
                        for (Keywords keyword : game.getKeywords()) {
                            keywordNames.add(keyword.getName());
                        }
                        tagsView.setText("🏷️ Tags: " + android.text.TextUtils.join(", ", keywordNames));
                    } else {
                        tagsView.setText("Tags no disponibles");
                    }

                    // Manejar compañías desarrolladoras
                    if (game.getInvolvedCompanies() != null && !game.getInvolvedCompanies().isEmpty()) {
                        List<String> developerNames = new ArrayList<>();
                        for (InvolvedCompanies ic : game.getInvolvedCompanies()) {
                            if (ic.isDeveloper() && ic.getCompany() != null) {
                                developerNames.add(ic.getCompany().getName());
                            }
                        }
                        if (!developerNames.isEmpty()) {
                            companiesView.setText("🏢 Desenvolupadors: " + android.text.TextUtils.join(", ", developerNames));
                        } else {
                            companiesView.setText("Desenvolupadors no disponibles");
                        }
                    } else {
                        companiesView.setText("Desenvolupadors no disponibles");
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
}
