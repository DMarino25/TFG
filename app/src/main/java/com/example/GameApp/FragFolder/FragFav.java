package com.example.GameApp.FragFolder;

import android.os.Bundle;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageView;
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
import java.util.List;

public class FragFav extends Fragment {

    private RecyclerView favList;
    private FavAdapter favAdapter;
    private ArrayList<FavoriteGame> favoriteGames; // Lista completa de favoritos
    private FirebaseFirestore firestore;
    private FirebaseUser currentUser;
    private TextView noFavoritesText;
    private ListenerRegistration favoritesListener;
    private EditText cercadora;
    private ImageView go;

    public FragFav() {
        // Constructor vacío requerido
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_frag_fav, container, false);

        firestore = FirebaseFirestore.getInstance();
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        go = v.findViewById(R.id.go2);
        cercadora = v.findViewById(R.id.cerca2);

        if (currentUser == null) {
            Toast.makeText(getContext(), getString(R.string.FragFavUserNull), Toast.LENGTH_LONG).show();
            return v;
        }

        favList = v.findViewById(R.id.favList);
        noFavoritesText = v.findViewById(R.id.no_favorites_text);

        favoriteGames = new ArrayList<>();
        favList.setLayoutManager(new LinearLayoutManager(getContext()));
        favAdapter = new FavAdapter(getContext(), new ArrayList<FavoriteGame>(),true,null);
        favList.setAdapter(favAdapter);

        go.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String searchQuery = cercadora.getText().toString().trim();
                filtraJocs(searchQuery);
            }
        });
        cercadora.setOnEditorActionListener((View,actionId,event) ->{
            if(actionId == EditorInfo.IME_ACTION_SEARCH || actionId == EditorInfo.IME_ACTION_DONE){
                go.performClick();
                return true;
            }
            return false;
        });
        requireActivity().getOnBackPressedDispatcher().addCallback(getViewLifecycleOwner(), new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                new AlertDialog.Builder(requireContext())
                        .setTitle("Sortir de l'aplicació")
                        .setMessage("Estàs segur que vols sortir o desloguejar?")
                        .setPositiveButton("Sí", (dialog, which) -> {
                            requireActivity().finishAffinity();
                        })
                        .setNegativeButton("No", (dialog, which) -> dialog.dismiss())
                        .show();
            }
        });

        return v;
    }

    @Override
    public void onStart() {
        super.onStart();
        if (currentUser != null) {
            favoritesListener = firestore.collection("users").document(currentUser.getUid()).collection("favorits")
                    .addSnapshotListener(new com.google.firebase.firestore.EventListener<QuerySnapshot>() {
                        @Override
                        public void onEvent(QuerySnapshot snapshots, com.google.firebase.firestore.FirebaseFirestoreException e) {
                            if (e != null) {
                                Toast.makeText(getContext(), getString(R.string.FragFavError), Toast.LENGTH_LONG).show();
                                return;
                            }

                            if (snapshots != null) {
                                favoriteGames.clear();
                                for (DocumentSnapshot document : snapshots.getDocuments()) {
                                    FavoriteGame favoriteGame = document.toObject(FavoriteGame.class);
                                    favoriteGames.add(favoriteGame);
                                }

                                Collections.sort(favoriteGames, new java.util.Comparator<FavoriteGame>() {
                                    @Override
                                    public int compare(FavoriteGame o1, FavoriteGame o2) {
                                        return Integer.compare(o2.getRating(), o1.getRating());
                                    }
                                });

                                String currentSearchQuery = cercadora.getText().toString().trim();
                                filtraJocs(currentSearchQuery);

                                checkIfFavoritesEmpty();
                            }
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
        if (favAdapter.getItemCount() == 0) {
            favList.setVisibility(View.GONE);
            noFavoritesText.setVisibility(View.VISIBLE);
        } else {
            favList.setVisibility(View.VISIBLE);
            noFavoritesText.setVisibility(View.GONE);
        }
    }

    private void filtraJocs(String query) {
        ArrayList<FavoriteGame> filteredList = new ArrayList<>();

        if (TextUtils.isEmpty(query)) {
            filteredList.addAll(favoriteGames);
        } else {
            for (FavoriteGame game : favoriteGames) {
                if (game.getTitle() != null && game.getTitle().toLowerCase().contains(query.toLowerCase())) {
                    filteredList.add(game);
                }
            }
        }
        favAdapter.updateList(filteredList);

        checkIfFavoritesEmpty();
    }
}
