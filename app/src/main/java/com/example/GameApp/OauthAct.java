package com.example.GameApp;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.GameApp.main.MainActivity;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.gson.Gson;
import com.squareup.picasso.Picasso;

import java.util.List;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class OauthAct extends AppCompatActivity {

    private GoogleSignInClient mGoogleSignInClient;
    private static final String TAG = "OauthAct";

    private TextView gameName;

    private EditText cercaText;
    private ImageView gameArtwork;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_oauth);

        cercaText = findViewById(R.id.rgsshwj5frv4);

       /* gameName = findViewById(R.id.game_name);
        gameArtwork = findViewById(R.id.game_artwork);

        IGDBApi apiService = ApiController.getClient().create(IGDBApi.class);
        String query = "fields name, artworks; where id = 1; limit 1;";
        Call<List<Game>> call = apiService.getGames(query);

        call.enqueue(new Callback<List<Game>>() {
            @Override
            public void onResponse(Call<List<Game>> call, Response<List<Game>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    // Convierte la respuesta completa en una cadena JSON y la registra
                    Gson gson = new Gson();
                    String jsonResponse = gson.toJson(response.body());
                    Log.d("API_RESPONSE_FULL_JSON", jsonResponse);

                    if (!response.body().isEmpty()) {
                        Game game = response.body().get(0);
                        Log.d("API_RESPONSE", "Game name: " + game.getName());
                        gameName.setText(game.getName());
                    } else {
                        Log.e("API_RESPONSE", "Response body is empty");
                    }
                } else {
                    Log.e("API_RESPONSE", "Response not successful");
                }
            }

            @Override
            public void onFailure(Call<List<Game>> call, Throwable t) {
                Log.e("API_CALL", "API call failed: " + t.getMessage());
            }
        });

        // Configura el Google Sign-In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken("840661236439-u1vskjpgspjrdqisaunnb2gkd7pa8eij.apps.googleusercontent.com") // Usa el mismo ID de cliente
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

    public void showToastMessage(View view) {
        // Texto del EditText
        String searchQuery = cercaText.getText().toString();

        // Mostrar "toast" con el texto
        if (searchQuery.isEmpty()) {
            Toast.makeText(this, "Por favor, ingresa un nombre de juego.", Toast.LENGTH_SHORT).show();
            return;
        }

        IGDBApi apiService = ApiController.getClient().create(IGDBApi.class);

        String query = "fields name, artworks; where name = \"" + searchQuery + "\"; limit 1;";
        //String query = "fields name, artworks; where id = 1905; limit 1;";
        RequestBody gameRequestBody = RequestBody.create(query, MediaType.parse("text/plain"));
        Call<List<Game>> call = apiService.getGames(gameRequestBody);

        call.enqueue(new Callback<List<Game>>() {
            @Override
            public void onResponse(Call<List<Game>> call, Response<List<Game>> response) {
                if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                    // Obtener nombre del juego
                    Game game = response.body().get(0);
                    Log.d("API_RESPONSE", "Game name: " + game.getName());
                    String gameName = game.getName();

                    // Mostrar nombre del juego en Toast
                    Toast.makeText(OauthAct.this, "Juego encontrado: " + gameName, Toast.LENGTH_LONG).show();

                    // Registrar la respuesta completa en el log
                    Gson gson = new Gson();
                    String jsonResponse = gson.toJson(response.body());
                    Log.d("API_RESPONSE_FULL_JSON", jsonResponse);
                } else {
                    Log.e("API_RESPONSE", "No se encontró ningún juego.");
                    Toast.makeText(OauthAct.this, "No se encontró ningún juego.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<Game>> call, Throwable t) {
                Log.e("API_CALL", "Fallo la llamada a la API: " + t.getMessage());
                Toast.makeText(OauthAct.this, "Error al buscar el juego.", Toast.LENGTH_SHORT).show();
            }
        });
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
