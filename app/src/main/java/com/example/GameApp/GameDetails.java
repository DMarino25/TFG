package com.example.GameApp;

import androidx.appcompat.app.AppCompatActivity;

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
import com.google.android.material.imageview.ShapeableImageView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class GameDetails extends AppCompatActivity {

    private static final String TAG = "GameDetails";
    private ImageView starUnselected;
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
            Toast.makeText(this, "Por favor, inicia sesión para gestionar favoritos.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        // Referencias a las vistas en el layout
        ShapeableImageView coverImage = findViewById(R.id.portada);
        TextView descripcio = findViewById(R.id.descripció);
        TextView titol = findViewById(R.id.titol);
        ImageView back = findViewById(R.id.flecha);
        LinearLayout ratingStarsContainer = findViewById(R.id.rating_stars_container);

        back.setOnClickListener(v -> finish());

        int coverId = getIntent().getIntExtra("coverId", -1);

        // Verificar si se ha recibido correctamente el coverId
        if (coverId == -1) {
            Toast.makeText(this, "Error: No se pudo cargar la información del juego.", Toast.LENGTH_LONG).show();
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

        Call<List<Cover>> coverCall = apiService.getCovers(coverRequestBody);
        coverCall.enqueue(new Callback<List<Cover>>() {
            @Override
            public void onResponse(Call<List<Cover>> call, Response<List<Cover>> response) {
                if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                    int gameId = response.body().get(0).getGame();
                    getGameDetails(gameId, coverImage, titol, descripcio);
                } else {
                    Toast.makeText(GameDetails.this, "No se pudieron cargar los detalles del cover.", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<List<Cover>> call, Throwable t) {
                Toast.makeText(GameDetails.this, "Error al obtener los detalles del cover.", Toast.LENGTH_LONG).show();
            }
        });
    }

    private void getGameDetails(int gameId, ShapeableImageView coverImage, TextView titol, TextView descripcio) {
        IGDBApi apiService = ApiController.getClient().create(IGDBApi.class);
        String gameQuery = "fields name, summary, first_release_date, cover.url; where id = " + gameId + ";";
        RequestBody gameRequestBody = RequestBody.create(gameQuery, MediaType.parse("text/plain"));

        Call<List<Game>> gameCall = apiService.getGames(gameRequestBody);
        gameCall.enqueue(new Callback<List<Game>>() {
            @Override
            public void onResponse(Call<List<Game>> call, Response<List<Game>> response) {
                if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                    currentGame = response.body().get(0); // Guardar el juego actual
                    titol.setText(currentGame.getName());
                    descripcio.setText(currentGame.getSummary() != null ? currentGame.getSummary() : "Descripción no disponible");
                    if (currentGame.getCover() != null && currentGame.getCover().getUrl() != null) {
                        String imageUrl = "https:" + currentGame.getCover().getUrl();
                        Glide.with(GameDetails.this).load(imageUrl).into(coverImage);
                    }
                    checkIfFavorite(); // Verificar si el juego ya está en favoritos
                } else {
                    Toast.makeText(GameDetails.this, "No se pudieron cargar los detalles del juego.", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<List<Game>> call, Throwable t) {
                Toast.makeText(GameDetails.this, "Error al obtener los detalles del juego.", Toast.LENGTH_LONG).show();
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
                        setRatingStars(currentRating); // Establecer la visualización de las estrellas
                        cambiaRatingStar(null); // Mostrar las estrellas de calificación
                    } else {
                        // El juego no está en favoritos
                        isStarSelected = false;
                        favoriteId = null;
                        starSelected.setVisibility(View.GONE);
                        starUnselected.setVisibility(View.VISIBLE);
                        setRatingStars(0); // Reiniciar las estrellas de calificación a 0
                    }
                });
    }

    private void toggleFavorite() {
        if (currentGame == null) {
            Toast.makeText(this, "No se puede agregar a favoritos. Información del juego no cargada.", Toast.LENGTH_LONG).show();
            return;
        }

        if (isStarSelected) {
            if (favoriteId != null) {
                firestore.collection("users").document(currentUser.getUid()).collection("favorits")
                        .document(favoriteId).delete()
                        .addOnSuccessListener(aVoid -> {
                            favoriteId = null;
                            Toast.makeText(GameDetails.this, "Eliminado de favoritos", Toast.LENGTH_SHORT).show();
                            isStarSelected = false;
                            setRatingStars(0); // Restablecer el rating visualmente a 0
                            cambiaStar(); // Cambiar la estrella seleccionada
                        })
                        .addOnFailureListener(e -> Toast.makeText(GameDetails.this, "Error al eliminar de favoritos", Toast.LENGTH_SHORT).show());
            }
        } else {
            Map<String, Object> favoriteData = new HashMap<>();
            favoriteData.put("title", currentGame.getName());
            favoriteData.put("cover_url", currentGame.getCover() != null ? "https:" + currentGame.getCover().getUrl() : "");
            favoriteData.put("rating", currentRating);

            firestore.collection("users").document(currentUser.getUid()).collection("favorits")
                    .add(favoriteData)
                    .addOnSuccessListener(documentReference -> {
                        favoriteId = documentReference.getId();
                        Toast.makeText(GameDetails.this, "Añadido a favoritos", Toast.LENGTH_SHORT).show();
                        isStarSelected = true;
                        cambiaStar(); // Cambiar la estrella seleccionada
                    })
                    .addOnFailureListener(e -> Toast.makeText(GameDetails.this, "Error al agregar a favoritos", Toast.LENGTH_SHORT).show());
        }
    }

    private void cambiaStar() {
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
        Toast.makeText(this, "Has valorado el juego con " + currentRating + " estrellas", Toast.LENGTH_SHORT).show();
        setRatingStars(currentRating); // Actualizar visualización de estrellas

        if (favoriteId != null) {
            firestore.collection("users").document(currentUser.getUid()).collection("favorits")
                    .document(favoriteId).update("rating", currentRating)
                    .addOnSuccessListener(aVoid -> Log.d(TAG, "Rating actualizado en favoritos"))
                    .addOnFailureListener(e -> Toast.makeText(GameDetails.this, "Error al actualizar el rating", Toast.LENGTH_SHORT).show());
        }
    }
}
