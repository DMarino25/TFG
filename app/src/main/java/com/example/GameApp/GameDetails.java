package com.example.GameApp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.GameApp.ClassObjectes.Cover;
import com.example.GameApp.ClassObjectes.Game;
import com.example.GameApp.ClassObjectes.Genres;
import com.example.GameApp.ClassObjectes.InvolvedCompanies;
import com.example.GameApp.ClassObjectes.Platforms;
import com.example.GameApp.ClassObjectes.Keywords;
import com.example.GameApp.main.MainActivity;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class GameDetails extends AppCompatActivity {

    private static final String TAG = "GameDetails";
    private ImageView starUnselected;
    private ListenerRegistration banListener;
    private ImageView starSelected;
    private boolean isStarSelected = false;
    private int currentRating = 0;  // Rating actual del juego
    private String favoriteId = null;  // ID del favorito en Firestore
    private FirebaseFirestore firestore;
    private FirebaseUser currentUser;
    private Game currentGame;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game_details);

        // Inicializar Firestore y obtener el usuario actual
        firestore = FirebaseFirestore.getInstance();
        currentUser = FirebaseAuth.getInstance().getCurrentUser();

        if (currentUser == null) {
            Toast.makeText(this, getString(R.string.GameDetailsUserNull), Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        // Referencias a las vistas en el layout
        ShapeableImageView coverImage = findViewById(R.id.portada);
        TextView descripcio = findViewById(R.id.descripció);
        TextView titol = findViewById(R.id.titol);
        ImageView back = findViewById(R.id.flecha);
        LinearLayout ratingStarsContainer = findViewById(R.id.rating_stars_container);



        TextView releaseDateTextView = findViewById(R.id.launching);
        TextView genresTextView = findViewById(R.id.genre);
        TextView platformsTextView = findViewById(R.id.platform);
        TextView keywordsTextView = findViewById(R.id.tags);
        TextView developerTextView = findViewById(R.id.developers);

        back.setOnClickListener(v -> finish());

        int coverId = getIntent().getIntExtra("coverId", -1);

        // Verificar si se ha recibido correctamente el coverId
        if (coverId == -1) {
            Toast.makeText(this, getString(R.string.GameDetailsCoverNotFound), Toast.LENGTH_LONG).show();
            return;
        }

        starUnselected = findViewById(R.id.star_unselected);
        starSelected = findViewById(R.id.star_selected);

        // Configuración inicial de visibilidad de las estrellas
        starUnselected.setVisibility(View.VISIBLE);
        starSelected.setVisibility(View.GONE);
        ratingStarsContainer.setVisibility(View.GONE); // Las estrellas de valoración están ocultas inicialmente

        starUnselected.setOnClickListener(v -> toggleFavorite());
        starSelected.setOnClickListener(v -> toggleFavorite());

        Log.d(TAG, "Cover ID recibido: " + coverId);

        IGDBApi apiService = ApiController.getClient().create(IGDBApi.class);

        // Paso 1: obtener información del cover para obtener el gameId
        String coverQuery = "fields game; where id = " + coverId + ";";
        RequestBody coverRequestBody = RequestBody.create(coverQuery, MediaType.parse("text/plain"));

        Call<List<Cover>> coverCall = apiService.getCovers(coverRequestBody, BuildConfig.CLIENT_ID, "Bearer " + BuildConfig.AUTH_TOKEN);
        coverCall.enqueue(new Callback<List<Cover>>() {
            @Override
            public void onResponse(Call<List<Cover>> call, Response<List<Cover>> response) {
                if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                    int gameId = response.body().get(0).getGame();
                    getGameDetails(gameId, coverImage, titol, descripcio, releaseDateTextView, genresTextView, platformsTextView, keywordsTextView, developerTextView);
                } else {
                    Toast.makeText(GameDetails.this, getString(R.string.GameDetailsLoadError), Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<List<Cover>> call, Throwable t) {
                Toast.makeText(GameDetails.this, getString(R.string.GameDetailsFailure), Toast.LENGTH_LONG).show();
            }
        });
    }
    @Override
    protected void onStart() {
        super.onStart();

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            String email = currentUser.getEmail();

            banListener = FirebaseFirestore.getInstance()
                    .collection("bannedUsers")
                    .whereEqualTo("email", email)
                    .addSnapshotListener((querySnapshot, e) -> {
                        if (e != null) {
                            return;
                        }
                        if (querySnapshot != null && !querySnapshot.isEmpty()) {
                            // El usuario está baneado
                            FirebaseAuth.getInstance().signOut();
                            Toast.makeText(GameDetails.this,"Usuari bloquejat",Toast.LENGTH_LONG).show();

                            Intent intent = new Intent(GameDetails.this, MainActivity.class);
                            startActivity(intent);
                            finish();
                        }
                    });
        }
    }
    @Override
    protected void onStop() {
        super.onStop();
        if (banListener != null) {
            banListener.remove();
            banListener = null;
        }
    }

    private void getGameDetails(int gameId, ShapeableImageView coverImage, TextView titol, TextView descripcio,
                                TextView releaseDateTextView, TextView genresTextView, TextView platformsTextView,
                                TextView keywordsTextView, TextView developerTextView) {
        IGDBApi apiService = ApiController.getClient().create(IGDBApi.class);
        String gameQuery = "fields name, summary, first_release_date, cover.url, genres.name, platforms.name, keywords.name, involved_companies.company.name, involved_companies.developer; where id = " + gameId + ";";
        RequestBody gameRequestBody = RequestBody.create(gameQuery, MediaType.parse("text/plain"));

        Call<List<Game>> gameCall = apiService.getGames(gameRequestBody, BuildConfig.CLIENT_ID, "Bearer " + BuildConfig.AUTH_TOKEN);
        gameCall.enqueue(new Callback<List<Game>>() {
            @Override
            public void onResponse(Call<List<Game>> call, Response<List<Game>> response) {
                if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                    currentGame = response.body().get(0); // Guardar el juego actual
                    titol.setText(currentGame.getName());
                    descripcio.setText(currentGame.getSummary() != null ? currentGame.getSummary() : "Descripció no disponible");

                    // Mostrar fecha de lanzamiento
                    if (currentGame.getFirstReleaseDate() != 0) {
                        Date releaseDate = new Date(currentGame.getFirstReleaseDate() * 1000L);
                        SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault());
                        releaseDateTextView.setText("Data de llançament: " + sdf.format(releaseDate));
                    } else {
                        releaseDateTextView.setText("Data de llançament: No disponible");
                    }

                    // Mostrar géneros
                    if (currentGame.getGenres() != null && !currentGame.getGenres().isEmpty()) {
                        StringBuilder genresBuilder = new StringBuilder();
                        for (Genres genre : currentGame.getGenres()) {
                            genresBuilder.append(genre.getName()).append(", ");
                        }
                        // Eliminar la última coma y espacio
                        genresTextView.setText("Gèneres: " + genresBuilder.substring(0, genresBuilder.length() - 2));
                    } else {
                        genresTextView.setText("Gèneres: No disponible");
                    }

                    // Mostrar plataformas
                    if (currentGame.getPlatforms() != null && !currentGame.getPlatforms().isEmpty()) {
                        StringBuilder platformsBuilder = new StringBuilder();
                        for (Platforms platform : currentGame.getPlatforms()) {
                            platformsBuilder.append(platform.getName()).append(", ");
                        }
                        platformsTextView.setText("Plataformes: " + platformsBuilder.substring(0, platformsBuilder.length() - 2));
                    } else {
                        platformsTextView.setText("Plataformes: No disponible");
                    }

                    // Mostrar keywords (tags)
                    if (currentGame.getKeywords() != null && !currentGame.getKeywords().isEmpty()) {
                        StringBuilder keywordsBuilder = new StringBuilder();
                        for (Keywords keyword : currentGame.getKeywords()) {
                            keywordsBuilder.append(keyword.getName()).append(", ");
                        }
                        keywordsTextView.setText("Tags: " + keywordsBuilder.substring(0, keywordsBuilder.length() - 2));
                    } else {
                        keywordsTextView.setText("Tags: No disponible");
                    }

                    // Mostrar desarrollador
                    if (currentGame.getInvolvedCompanies() != null && !currentGame.getInvolvedCompanies().isEmpty()) {
                        StringBuilder developersBuilder = new StringBuilder();
                        for (InvolvedCompanies company : currentGame.getInvolvedCompanies()) {
                            if (company.isDeveloper()) {
                                developersBuilder.append(company.getCompany().getName()).append(", ");
                            }
                        }
                        if (developersBuilder.length() > 0) {
                            developerTextView.setText("Desenvolupador: " + developersBuilder.substring(0, developersBuilder.length() - 2));
                        } else {
                            developerTextView.setText("Desenvolupador: No disponible");
                        }
                    } else {
                        developerTextView.setText("Desenvolupador: No disponible");
                    }

                    if (currentGame.getCover() != null && currentGame.getCover().getUrl() != null) {
                        String imageId = CoverUtils.extractImageId(currentGame.getCover().getUrl());
                        String imageUrl = CoverUtils.constructImageUrl(imageId, "t_1080p");
                        Glide.with(GameDetails.this).load(imageUrl).into(coverImage);
                    }
                    checkIfFavorite();
                } else {
                    Toast.makeText(GameDetails.this, getString(R.string.GameDetailsLoadError), Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<List<Game>> call, Throwable t) {
                Toast.makeText(GameDetails.this, getString(R.string.GameDetailsFailure), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void checkIfFavorite() {
        firestore.collection("users").document(currentUser.getUid()).collection("favorits")
                .whereEqualTo("title", currentGame.getName())
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null && !task.getResult().isEmpty()) {
                        // El juego ya está en favoritos
                        isStarSelected = true;
                        favoriteId = task.getResult().getDocuments().get(0).getId();
                        starSelected.setVisibility(View.VISIBLE);
                        starUnselected.setVisibility(View.GONE);
                        currentRating = task.getResult().getDocuments().get(0).getLong("rating").intValue(); // Obtener rating actual
                        setRatingStars(currentRating);
                        cambiaRatingStar(null);
                    } else {
                        // El juego no está en favoritos
                        isStarSelected = false;
                        favoriteId = null;
                        starSelected.setVisibility(View.GONE);
                        starUnselected.setVisibility(View.VISIBLE);
                        setRatingStars(0);
                    }
                });
    }

    private void toggleFavorite() {
        if (currentGame == null) {
            Toast.makeText(this, getString(R.string.GameDetailsFavoriteNull), Toast.LENGTH_LONG).show();
            return;
        }

        if (isStarSelected) {
            if (favoriteId != null) {
                firestore.collection("users").document(currentUser.getUid()).collection("favorits")
                        .document(favoriteId).delete()
                        .addOnSuccessListener(aVoid -> {
                            favoriteId = null;
                            Toast.makeText(GameDetails.this, getString(R.string.GameDetailsEraseFav), Toast.LENGTH_SHORT).show();
                            isStarSelected = false;
                            setRatingStars(0);
                            changeStar();
                        })
                        .addOnFailureListener(e -> Toast.makeText(GameDetails.this, getString(R.string.GameDetailsEraseFavError), Toast.LENGTH_SHORT).show());
            }
        } else {
            Map<String, Object> favoriteData = new HashMap<>();
            favoriteData.put("title", currentGame.getName());
            favoriteData.put("cover_url", currentGame.getCover() != null ? "https:" + currentGame.getCover().getUrl() : "");
            favoriteData.put("rating", currentRating);
            favoriteData.put("coverId", currentGame.getCover().getId());

            firestore.collection("users").document(currentUser.getUid()).collection("favorits")
                    .add(favoriteData)
                    .addOnSuccessListener(documentReference -> {
                        favoriteId = documentReference.getId();
                        Toast.makeText(GameDetails.this, getString(R.string.GameDetailsAddFav), Toast.LENGTH_SHORT).show();
                        isStarSelected = true;
                        changeStar();
                    })
                    .addOnFailureListener(e -> Toast.makeText(GameDetails.this, getString(R.string.GameDetailsAddFavError), Toast.LENGTH_SHORT).show());
        }
    }

    private void changeStar() {
        if (isStarSelected) {
            starSelected.setVisibility(View.VISIBLE);
            starUnselected.setVisibility(View.GONE);
        } else {
            starSelected.setVisibility(View.GONE);
            starUnselected.setVisibility(View.VISIBLE);
        }
        cambiaRatingStar(starSelected);
    }

    private void cambiaRatingStar(View view) {
        LinearLayout ratingStarsContainer = findViewById(R.id.rating_stars_container);
        if (ratingStarsContainer.getVisibility() == View.GONE) {
            ratingStarsContainer.setVisibility(View.VISIBLE);
        } else {
            ratingStarsContainer.setVisibility(View.GONE);
        }
    }

    private void setRatingStars(int rating) {
        for (int i = 1; i <= 5; i++) {
            ImageView star = findViewById(getResources().getIdentifier("star_" + i, "id", getPackageName()));
            if (i <= rating) {
                star.setImageResource(R.drawable.star); // Estrella llena
            } else {
                star.setImageResource(R.drawable.estrella_con); // Estrella vacía
            }
        }
    }

    public void rateGame(View view) {
        currentRating = Integer.parseInt(view.getContentDescription().toString().split(" ")[1]);
        Toast.makeText(this, getString(R.string.GameDetailsRated), Toast.LENGTH_SHORT).show();
        setRatingStars(currentRating); // Actualizar visualización de estrellas

        if (favoriteId != null) {
            firestore.collection("users").document(currentUser.getUid()).collection("favorits")
                    .document(favoriteId).update("rating", currentRating)
                    .addOnSuccessListener(aVoid -> Log.d(TAG, "Rating actualizado en favoritos"))
                    .addOnFailureListener(e -> Toast.makeText(GameDetails.this, getString(R.string.GameDetailsRatedError), Toast.LENGTH_SHORT).show());
        }
    }
}
