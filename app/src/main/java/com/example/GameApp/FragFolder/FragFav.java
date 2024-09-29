package com.example.GameApp.FragFolder;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.example.GameApp.ClassObjectes.FavoriteGame;
import com.example.GameApp.FavAdapter;
import com.example.GameApp.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.*;

import java.util.ArrayList;
import java.util.Collections;

public class FragFav extends Fragment {

    private RecyclerView favList;
    private FavAdapter favAdapter;
    private ArrayList<FavoriteGame> favoriteGames;
    private FirebaseFirestore firestore;
    private FirebaseUser currentUser;
    private TextView noFavoritesText;
    private ListenerRegistration favoritesListener; // Nueva variable

    public FragFav() {
        // Constructor vacío requerido
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_frag_fav, container, false);

        firestore = FirebaseFirestore.getInstance();
        currentUser = FirebaseAuth.getInstance().getCurrentUser();

        if (currentUser == null) {
            Toast.makeText(getContext(), "Por favor, inicia sesión para ver tus favoritos.", Toast.LENGTH_LONG).show();
            return v;
        }

        favList = v.findViewById(R.id.favList);
        noFavoritesText = v.findViewById(R.id.no_favorites_text);

        favoriteGames = new ArrayList<>();

        favList.setLayoutManager(new LinearLayoutManager(getContext()));
        favAdapter = new FavAdapter(getContext(), favoriteGames);
        favList.setAdapter(favAdapter);

        // Ya no es necesario llamar a loadFavorites()
        // loadFavorites();

        return v;
    }

    @Override
    public void onStart() {
        super.onStart();
        if (currentUser != null) {
            favoritesListener = firestore.collection("users").document(currentUser.getUid()).collection("favorits")
                    .addSnapshotListener((snapshots, e) -> {
                        if (e != null) {
                            Toast.makeText(getContext(), "Error al obtener los favoritos.", Toast.LENGTH_LONG).show();
                            return;
                        }

                        if (snapshots != null) {
                            favoriteGames.clear();
                            for (DocumentSnapshot document : snapshots.getDocuments()) {
                                FavoriteGame favoriteGame = document.toObject(FavoriteGame.class);
                                favoriteGames.add(favoriteGame);
                            }

                            // Ordenar la lista por rating de mayor a menor
                            Collections.sort(favoriteGames, (o1, o2) -> Integer.compare(o2.getRating(), o1.getRating()));

                            favAdapter.notifyDataSetChanged();

                            // Comprobar si la lista está vacía
                            checkIfFavoritesEmpty();
                        }
                    });
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (favoritesListener != null) {
            favoritesListener.remove();
            favoritesListener = null;
        }
    }

    private void checkIfFavoritesEmpty() {
        if (favoriteGames.isEmpty()) {
            favList.setVisibility(View.GONE);
            noFavoritesText.setVisibility(View.VISIBLE);
        } else {
            favList.setVisibility(View.VISIBLE);
            noFavoritesText.setVisibility(View.GONE);
        }
    }


}
