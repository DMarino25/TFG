package com.example.GameApp;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.GameApp.main.MainActivity;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.gson.Gson;
import com.squareup.picasso.Picasso;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class OauthAct extends AppCompatActivity {

    private GoogleSignInClient mGoogleSignInClient;
    private static final String TAG = "OauthAct";

    private TextView gameName;
    private ImageView gameArtwork;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_oauth);
        /*gameName = findViewById(R.id.game_name);
        gameArtwork = findViewById(R.id.game_artwork);

        IGDBApi apiService = ApiController.getClient().create(IGDBApi.class);

        // Construir la consulta para obtener los detalles del juego
        String gameQuery = "fields name, artworks; where id = 1; limit 1;";
        RequestBody gameRequestBody = RequestBody.create(gameQuery, MediaType.parse("text/plain"));

        Call<List<Game>> gameCall = apiService.getGames(gameRequestBody);

        gameCall.enqueue(new Callback<List<Game>>() {
            @Override
            public void onResponse(Call<List<Game>> call, Response<List<Game>> response) {
                if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                    Game game = response.body().get(0);
                    gameName.setText(game.getName()); // Mostrar el nombre del juego

                    // Si hay artworks, obtener el primero
                    if (game.getArtworks() != null && !game.getArtworks().isEmpty()) {
                        int artworkId = game.getArtworks().get(0);

                        // Hacer otra llamada para obtener los detalles del Artwork
                        String artworkQuery = "fields alpha_channel,animated,checksum,game,height,image_id,url,width; where id = " + artworkId + ";";
                        RequestBody artworkRequestBody = RequestBody.create(artworkQuery, MediaType.parse("text/plain"));

                        Call<List<Artwork>> artworkCall = apiService.getArtwork(artworkRequestBody);

                        artworkCall.enqueue(new Callback<List<Artwork>>() {
                            @Override
                            public void onResponse(Call<List<Artwork>> call, Response<List<Artwork>> artworkResponse) {
                                if (artworkResponse.isSuccessful() && artworkResponse.body() != null && !artworkResponse.body().isEmpty()) {
                                    Artwork artwork = artworkResponse.body().get(0);

                                    // Cargar la imagen usando Picasso
                                    String fullUrl = "https:" + artwork.getUrl();
                                    Picasso.get().load(fullUrl).into(gameArtwork);
                                } else {
                                    Log.e(TAG, "Artwork response not successful or empty");
                                }
                            }

                            @Override
                            public void onFailure(Call<List<Artwork>> call, Throwable t) {
                                Log.e(TAG, "Artwork API call failed: " + t.getMessage());
                            }
                        });
                    }
                } else {
                    Log.e(TAG, "Game response not successful or empty");
                }
            }

            @Override
            public void onFailure(Call<List<Game>> call, Throwable t) {
                Log.e(TAG, "Game API call failed: " + t.getMessage());
            }
        });

        // Configura el Google Sign-In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken("840661236439-u1vskjpgspjrdqisaunnb2gkd7pa8eij.apps.googleusercontent.com")
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        Button btnLogout = findViewById(R.id.btn_logout);
        btnLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                signOut();
            }
        });*/
    }

    private void signOut() {
        mGoogleSignInClient.signOut()
                .addOnCompleteListener(this, task -> {
                    // Cerrar sesión exitosa, vuelve a MainActivity
                    Log.d(TAG, "Sign out successful");
                    Intent intent = new Intent(OauthAct.this, MainActivity.class);
                    startActivity(intent);
                    finish();
                });
    }
}
