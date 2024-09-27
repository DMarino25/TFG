package com.example.GameApp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.GameApp.ClassObjectes.Cover;
import com.example.GameApp.FragFolder.FragAjust;
import com.example.GameApp.FragFolder.FragFav;
import com.example.GameApp.FragFolder.FragForum;
import com.example.GameApp.FragFolder.FragHome;
import com.example.GameApp.main.MainActivity;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;

import java.util.ArrayList;
import java.util.List;

public class OauthAct extends AppCompatActivity {

    private GoogleSignInClient mGoogleSignInClient;
    private List<Cover> coverList = new ArrayList<>();

    private BottomNavigationView bottomNavigationView;
    private ImageButton missatge;
    private Fragment fragments[];
    RecyclerView recyclerView;
    private CoverAdapter coverAdapter;
    private static final String TAG = "OauthAct";

    private TextView gameName;

    private EditText cercaText;
    private ImageView gameArtwork;
    private float dX, dY;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Fragmentos

        fragments = new Fragment[4];

        fragments[0]= new FragHome();
        fragments[1] = new FragFav();
        fragments[2] = new FragForum();
        fragments[3] = new FragAjust();
        setContentView(R.layout.activity_oauth);

        bottomNavigationView = findViewById(R.id.menuNav);
        missatge = findViewById(R.id.missatges);





        // Configurando el GridLayoutManager para 2 columnas


        //cercaText = findViewById(R.id.rgsshwj5frv4);

       /* gameName = findViewById(R.id.game_name);
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


    private void openFragment(Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.fragmentContainerView2, fragment);
        fragmentTransaction.commit();
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
