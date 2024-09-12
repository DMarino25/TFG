package com.example.GameApp.FragFolder;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.GameApp.ApiController;
import com.example.GameApp.ClassObjectes.Cover;
import com.example.GameApp.CoverAdapter;
import com.example.GameApp.ClassObjectes.Game;
import com.example.GameApp.IGDBApi;
import com.example.GameApp.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;
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
    private static final String TAG = "OauthAct";

    public FragHome() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_frag_home, container, false);

        recyclerView = v.findViewById(R.id.recyclerView);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(getContext(), 2);
        recyclerView.setLayoutManager(gridLayoutManager);

        IGDBApi apiService = ApiController.getClient().create(IGDBApi.class);


        String coversQuery = "fields id,game,height,image_id,url,width,checksum; limit 50;";
        RequestBody coversRequestBody = RequestBody.create(coversQuery, MediaType.parse("text/plain"));


        Call<List<Cover>> coversCall = apiService.getCovers(coversRequestBody);
        coversCall.enqueue(new Callback<List<Cover>>() {
            @Override
            public void onResponse(Call<List<Cover>> call, Response<List<Cover>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    coverList = response.body();


                    List<Integer> gameIds = new ArrayList<>();
                    for (Cover cover : coverList) {
                        gameIds.add(cover.getGame());
                    }


                    String gamesQuery = "fields id,name; where id = (" + TextUtils.join(",", gameIds) + ");";
                    RequestBody gamesRequestBody = RequestBody.create(gamesQuery, MediaType.parse("text/plain"));

                    Call<List<Game>> gamesCall = apiService.getGames(gamesRequestBody);
                    gamesCall.enqueue(new Callback<List<Game>>() {
                        @Override
                        public void onResponse(Call<List<Game>> call, Response<List<Game>> response) {
                            if (response.isSuccessful() && response.body() != null) {
                                for (Cover cover : coverList) {
                                    for (Game game : response.body()) {
                                        if (cover.getGame() == game.getId()) {
                                            cover.setGameName(game.getName()); // Asignar el nombre
                                            break;
                                        }
                                        else{
                                            cover.setGameName("Nom no disponible");
                                        }
                                    }
                                }

                                // Una vez tenemos las portadas y los nombres, asignamos el adaptador
                                coverAdapter = new CoverAdapter(coverList);
                                recyclerView.setAdapter(coverAdapter);
                            } else {
                                Log.e(TAG, "Games response not successful or empty");
                            }
                        }

                        @Override
                        public void onFailure(Call<List<Game>> call, Throwable t) {
                            Log.e(TAG, "Games API call failed: " + t.getMessage());
                        }
                    });

                } else {
                    Log.e(TAG, "Covers response not successful or empty");
                }
            }

            @Override
            public void onFailure(Call<List<Cover>> call, Throwable t) {
                Log.e(TAG, "Covers API call failed: " + t.getMessage());
            }
        });

        return v;
    }


    public void showToastMessage(View view) {
        // Texto del EditTextString searchQuery = cercaText.getText().toString();

        // Mostrar "toast" con el texto
       /* if (searchQuery.isEmpty()) {
            Toast.makeText(getContext(), "Por favor, ingresa un nombre de juego.", Toast.LENGTH_SHORT).show();
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
                    Toast.makeText(getContext(), "Juego encontrado: " + gameName, Toast.LENGTH_LONG).show();

                    // Registrar la respuesta completa en el log
                    Gson gson = new Gson();
                    String jsonResponse = gson.toJson(response.body());
                    Log.d("API_RESPONSE_FULL_JSON", jsonResponse);
                } else {
                    Log.e("API_RESPONSE", "No se encontró ningún juego.");
                    Toast.makeText(getContext(), "No se encontró ningún juego.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<Game>> call, Throwable t) {
                Log.e("API_CALL", "Fallo la llamada a la API: " + t.getMessage());
                Toast.makeText(getContext(), "Error al buscar el juego.", Toast.LENGTH_SHORT).show();
            }
        });*/
    }
}