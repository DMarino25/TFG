package com.example.GameApp.FragFolder;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.GameApp.ApiController;
import com.example.GameApp.BuildConfig;
import com.example.GameApp.ClassObjectes.Cover;
import com.example.GameApp.CoverAdapter;
import com.example.GameApp.ClassObjectes.Game;
import com.example.GameApp.IGDBApi;
import com.example.GameApp.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link FragHome#newInstance} factory method to
 * create an instance of this fragment.
 */
public class FragHome extends Fragment {

    private List<Cover> coverList = new ArrayList<>();

    private RecyclerView recyclerView;
    private CoverAdapter coverAdapter;
    private static final String TAG = "FragHome"; // Ajuste del tag
    private IGDBApi apiService;
    private FirebaseAuth firebaseAuth;

    public FragHome() {
        // Constructor vacío requerido
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {


        View v = inflater.inflate(R.layout.fragment_frag_home, container, false);

        recyclerView = v.findViewById(R.id.recyclerView);
        EditText cercadora = v.findViewById(R.id.cerca1);
        ImageView go = v.findViewById(R.id.go1);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(getContext(), 2);
        recyclerView.setLayoutManager(gridLayoutManager);

        apiService = ApiController.getClient().create(IGDBApi.class);

        loadInitialGames();

        go.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String searchGame = cercadora.getText().toString().trim();
                if (!searchGame.isEmpty()) {

                    searchGames(searchGame);
                } else {
                    loadInitialGames();
                }
            }
        });
        cercadora.setOnKeyListener((View,keycode,event) ->{
            if(keycode == KeyEvent.KEYCODE_ENTER && event.getAction()== KeyEvent.ACTION_DOWN){
                go.performClick();
                return true;
            }
            return false;
        });
        return v;

    }

    private void loadInitialGames() {
        String coversQuery = "fields id,game,height,image_id,url,width,checksum; limit 50;";
        RequestBody coversRequestBody = RequestBody.create(coversQuery, MediaType.parse("text/plain"));

        Call<List<Cover>> coversCall = apiService.getCovers(coversRequestBody, BuildConfig.CLIENT_ID, "Bearer " + BuildConfig.AUTH_TOKEN);
        coversCall.enqueue(new Callback<List<Cover>>() {
            @Override
            public void onResponse(Call<List<Cover>> call, Response<List<Cover>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    coverList = response.body();

                    List<Integer> gameIds = new ArrayList<>();
                    for (Cover cover : coverList) {
                        gameIds.add(cover.getGame());
                    }

                    StringBuilder gamesQueryBuilder = new StringBuilder();
                    gamesQueryBuilder.append("fields id, name; where id = (");
                    gamesQueryBuilder.append(TextUtils.join(",", gameIds));
                    gamesQueryBuilder.append("); limit ").append(gameIds.size()).append(";");

                    String gamesQuery = gamesQueryBuilder.toString();
                    RequestBody gamesRequestBody = RequestBody.create(gamesQuery, MediaType.parse("text/plain"));

                    Call<List<Game>> gamesCall = apiService.getGames(gamesRequestBody, BuildConfig.CLIENT_ID, "Bearer " + BuildConfig.AUTH_TOKEN);
                    gamesCall.enqueue(new Callback<List<Game>>() {
                        @Override
                        public void onResponse(Call<List<Game>> call, Response<List<Game>> response) {
                            if (response.isSuccessful() && response.body() != null) {
                                List<Cover> filteredCoverList = new ArrayList<>();

                                for (Cover cover : coverList) {
                                    boolean gameFound = false;
                                    for (Game game : response.body()) {
                                        if (cover.getGame() == game.getId()) {
                                            if (game.getName() != null && !game.getName().isEmpty()) {
                                                cover.setGameName(game.getName());
                                            } else {
                                                cover.setGameName("Nom no disponible");
                                            }
                                            gameFound = true;
                                            break;
                                        }
                                    }

                                    if (gameFound && cover.getUrl() != null && !cover.getUrl().isEmpty()) {
                                        filteredCoverList.add(cover);
                                    }
                                }

                                Log.d(TAG, "Cantidad de juegos filtrados: " + filteredCoverList.size());

                                coverAdapter = new CoverAdapter(getContext(), filteredCoverList);
                                recyclerView.setAdapter(coverAdapter);

                            } else {
                                Log.e(TAG, "Respuesta de juegos no exitosa o vacía");
                            }
                        }

                        @Override
                        public void onFailure(Call<List<Game>> call, Throwable t) {
                            Log.e(TAG, "Falló la llamada a la API de juegos: " + t.getMessage());
                        }
                    });
                } else {
                    Log.e(TAG, "Respuesta de covers no exitosa o vacía");
                }
            }

            @Override
            public void onFailure(Call<List<Cover>> call, Throwable t) {
                Log.e(TAG, "Falló la llamada a la API de covers: " + t.getMessage());
            }
        });
    }

    private void searchGames(String searchGame) {
        String gamesQuery = "search \"" + searchGame + "\"; fields id,name; limit 50;";
        RequestBody gamesRequestBody = RequestBody.create(gamesQuery, MediaType.parse("text/plain"));

        Call<List<Game>> gamesCall = apiService.getGames(gamesRequestBody, BuildConfig.CLIENT_ID, "Bearer " + BuildConfig.AUTH_TOKEN);
        gamesCall.enqueue(new Callback<List<Game>>() {
            @Override
            public void onResponse(Call<List<Game>> call, Response<List<Game>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Game> gamesList = response.body();
                    List<Integer> gameIds = new ArrayList<>();
                    Map<Integer, String> gameNamesMap = new HashMap<>();

                    for (Game game : gamesList) {
                        gameIds.add(game.getId());
                        gameNamesMap.put(game.getId(), game.getName());
                    }

                    if (gameIds.isEmpty()) {
                        Toast.makeText(getContext(), "No se encontraron juegos", Toast.LENGTH_SHORT).show();
                        coverList.clear();
                        coverAdapter.notifyDataSetChanged();
                        return;
                    }

                    String coversQuery = "fields id,game,height,image_id,url,width,checksum; where game = (" + TextUtils.join(",", gameIds) + ");";
                    RequestBody coversRequestBody = RequestBody.create(coversQuery, MediaType.parse("text/plain"));

                    Call<List<Cover>> coversCall = apiService.getCovers(coversRequestBody, BuildConfig.CLIENT_ID, "Bearer " + BuildConfig.AUTH_TOKEN);
                    coversCall.enqueue(new Callback<List<Cover>>() {
                        @Override
                        public void onResponse(Call<List<Cover>> call, Response<List<Cover>> response) {
                            if (response.isSuccessful() && response.body() != null) {
                                coverList = response.body();
                                List<Cover> filteredCoverList = new ArrayList<>();

                                for (Cover cover : coverList) {
                                    int gameId = cover.getGame();
                                    String gameName = gameNamesMap.get(gameId);
                                    if (gameName != null && !gameName.isEmpty()) {
                                        cover.setGameName(gameName);
                                    } else {
                                        cover.setGameName("Nom no disponible");
                                    }

                                    if (cover.getUrl() != null && !cover.getUrl().isEmpty()) {
                                        filteredCoverList.add(cover);
                                    }
                                }

                                coverAdapter = new CoverAdapter(getContext(), filteredCoverList);
                                recyclerView.setAdapter(coverAdapter);

                            } else {
                                Log.e(TAG, "Resposta de covers no exitosa o buida");
                            }
                        }

                        @Override
                        public void onFailure(Call<List<Cover>> call, Throwable t) {
                            Log.e(TAG, "Falló la llamada a la API de covers: " + t.getMessage());
                        }
                    });
                } else {
                    Log.e(TAG, "Respuesta de búsqueda de juegos no exitosa o vacía");
                    Toast.makeText(getContext(), "No s'han trobat coincidencies", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<Game>> call, Throwable t) {
                Log.e(TAG, "Falló la llamada a la API de búsqueda de juegos: " + t.getMessage());
                Toast.makeText(getContext(), "Error al cercar jocs", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
