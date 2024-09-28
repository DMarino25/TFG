package com.example.GameApp.FragFolder;

import android.os.Bundle;

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
import java.util.Comparator;

public class FragFav extends Fragment {

    private RecyclerView favList;
    private FavAdapter favAdapter;
    private ArrayList<FavoriteGame> favoriteGames;
    private FirebaseFirestore firestore;
    private FirebaseUser currentUser;
    private TextView noFavoritesText;

    public FragFav() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_frag_fav, container, false);


        firestore = FirebaseFirestore.getInstance();
        currentUser = FirebaseAuth.getInstance().getCurrentUser();

        if (currentUser == null) {
            Toast.makeText(getContext(), "Por favor, inicia sessió per a veure els teus favorits.", Toast.LENGTH_LONG).show();
            return v;
        }

        favList = v.findViewById(R.id.favList);
        noFavoritesText = v.findViewById(R.id.no_favorites_text);

        favoriteGames = new ArrayList<>();

        favList.setLayoutManager(new LinearLayoutManager(getContext()));
        favAdapter = new FavAdapter(getContext(), favoriteGames);
        favList.setAdapter(favAdapter);

        loadFavorites();

        return v;
    }

    private void loadFavorites() {
        firestore.collection("users").document(currentUser.getUid()).collection("favorits")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        favoriteGames.clear();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            FavoriteGame favoriteGame = document.toObject(FavoriteGame.class);
                            favoriteGames.add(favoriteGame);
                        }

                        // Ordenar la lista por rating de mayor a menor
                        Collections.sort(favoriteGames, new Comparator<FavoriteGame>() {
                            @Override
                            public int compare(FavoriteGame o1, FavoriteGame o2) {
                                return Integer.compare(o2.getRating(), o1.getRating());
                            }
                        });

                        favAdapter.notifyDataSetChanged();

                        if (favoriteGames.isEmpty()) {
                            favList.setVisibility(View.GONE);
                            noFavoritesText.setVisibility(View.VISIBLE);
                        } else {
                            favList.setVisibility(View.VISIBLE);
                            noFavoritesText.setVisibility(View.GONE);
                        }
                    } else {
                        Toast.makeText(getContext(), "Error al obtenir favorits.", Toast.LENGTH_LONG).show();
                    }
                });
    }
}
