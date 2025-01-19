package com.example.GameApp.FragFolder;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageView;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.GameApp.OnForumUpdatedListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

import com.example.GameApp.ClassObjectes.Forum;
import com.example.GameApp.ForumAdapter;
import com.example.GameApp.R;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

public class FragForum extends Fragment {

    private RecyclerView recyclerView;
    private static ForumAdapter forumAdapter;
    private List<Forum> forumList;

    private List<Forum> fullForumList; // Lista completa de Firestore
    private static FirebaseFirestore db;
    private FloatingActionButton fabCreateForum;

    private EditText cercadora;
    private String currentSearchQuery = "";
    private ImageView go;

    public FragForum() {
        // Constructor vacío
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_frag_forum, container, false);

        recyclerView = view.findViewById(R.id.recyclerViewForums);
        go = view.findViewById(R.id.go3);
        cercadora = view.findViewById(R.id.cerca3);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // Inicializamos la lista de foros y el adaptador
        forumList = new ArrayList<>();
        fullForumList = new ArrayList<>();
        forumAdapter = new ForumAdapter(forumList, forum -> {
            // Al hacer clic en un foro, abrimos la pantalla de detalles
            Intent intent = new Intent(getActivity(), ForumDetailsActivity.class);
            intent.putExtra("forumId", forum.getId());
            intent.putExtra("forumTitle", forum.getTitle());
            intent.putExtra("forumDescription", forum.getDescription());
            intent.putExtra("userName", forum.getUserName());
            intent.putExtra("userProfilePhoto", forum.getUserProfilePhoto());
            intent.putExtra("lastModifiedDate", forum.getFormattedDate());
            startActivity(intent);
        });

        recyclerView.setAdapter(forumAdapter);
        db = FirebaseFirestore.getInstance();
        go.setOnClickListener(v -> {
            currentSearchQuery = cercadora.getText().toString().trim();
            filtrarForums(currentSearchQuery);
        });
        cercadora.setOnEditorActionListener((View,actionId,event) ->{
            if(actionId == EditorInfo.IME_ACTION_SEARCH || actionId == EditorInfo.IME_ACTION_DONE){
                go.performClick();
                return true;
            }
            return false;
        });


        // Botón flotante para crear un nuevo foro
        fabCreateForum = view.findViewById(R.id.createForumButton);
        fabCreateForum.setOnClickListener(v -> showCreateForumDialog());

        loadForumsFromFirestore();
        requireActivity().getOnBackPressedDispatcher().addCallback(getViewLifecycleOwner(), new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                new androidx.appcompat.app.AlertDialog.Builder(requireContext())
                        .setTitle("Sortir de l'aplicació")
                        .setMessage("Estàs segur que vols sortir?")
                        .setPositiveButton("Sí", (dialog, which) -> {
                            requireActivity().finishAffinity();
                        })
                        .setNegativeButton("No", (dialog, which) -> dialog.dismiss())
                        .show();
            }
        });
        return view;
    }

    private void loadForumsFromFirestore() {
        // Temporary list to hold forums before sorting
        List<Forum> tempForumList = new ArrayList<>();

        db.collection("forums")
                .orderBy("lastModifiedDate", Query.Direction.DESCENDING)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Log.d("FragForum", "Data fetch successful");

                        // Clear both lists to avoid duplicates
                        fullForumList.clear();
                        forumList.clear();

                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Forum forum = document.toObject(Forum.class);
                            String userId = forum.getUserId();
                            Log.d("FragForum", "Forum fetched: " + forum);
                            Log.d("FragForum", "UserId: " + userId);

                            // Query user details
                            db.collection("users")
                                    .document(userId)
                                    .get()
                                    .addOnSuccessListener(userDocument -> {
                                        if (userDocument.exists()) {
                                            // Extract the user name and photo URL from the user document
                                            String userName = userDocument.getString("name");
                                            String userProfilePhoto = userDocument.getString("photoUrl");

                                            // Set these values to the forum object
                                            forum.setUserName(userName);
                                            forum.setUserProfilePhoto(userProfilePhoto);

                                            // Obtain and format the date
                                            Timestamp lastModifiedDate = forum.getLastModifiedDate();
                                            String formattedDate = formatLastModifiedDate(lastModifiedDate);
                                            forum.setFormattedDate(formattedDate);

                                            // Set the document ID for future references
                                            forum.setId(document.getId());

                                            // Add the forum to the temporary list
                                            tempForumList.add(forum);

                                            // Check if all forums are processed
                                            if (tempForumList.size() == task.getResult().size()) {
                                                // Sort the forums explicitly in DESCENDING order by lastModifiedDate
                                                tempForumList.sort((f1, f2) ->
                                                        f2.getLastModifiedDate().compareTo(f1.getLastModifiedDate()));

                                                // Update the fullForumList and apply the filter
                                                fullForumList.addAll(tempForumList);
                                                filtrarForums(currentSearchQuery);
                                            }
                                        } else {
                                            // Extract the user name and photo URL from the user document
                                            String userName = "Usuari eliminat";
                                            String userProfilePhoto = "";

                                            // Set these values to the forum object
                                            forum.setUserName(userName);
                                            forum.setUserProfilePhoto(userProfilePhoto);

                                            // Obtain and format the date
                                            Timestamp lastModifiedDate = forum.getLastModifiedDate();
                                            String formattedDate = formatLastModifiedDate(lastModifiedDate);
                                            forum.setFormattedDate(formattedDate);

                                            // Set the document ID for future references
                                            forum.setId(document.getId());

                                            // Add the forum to the temporary list
                                            tempForumList.add(forum);

                                            // Check if all forums are processed
                                            if (tempForumList.size() == task.getResult().size()) {
                                                // Sort the forums explicitly in DESCENDING order by lastModifiedDate
                                                tempForumList.sort((f1, f2) ->
                                                        f2.getLastModifiedDate().compareTo(f1.getLastModifiedDate()));

                                                // Update the fullForumList and apply the filter
                                                fullForumList.addAll(tempForumList);
                                                filtrarForums(currentSearchQuery);
                                            }
                                            Log.e("FragForum", "User document does not exist for ID: " + userId);
                                        }
                                    })
                                    .addOnFailureListener(e -> Log.e("FragForum", "Error fetching user details: " + e.getMessage()));
                        }
                    } else {
                        Log.e("FragForum", "Error fetching forums", task.getException());
                    }
                });
    }


    private void showCreateForumDialog() {
        // Crear y mostrar el popup para crear un foro nuevo
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_create_forum, null);
        TextInputEditText titleEditText = dialogView.findViewById(R.id.titleEditText);
        TextInputEditText descriptionEditText = dialogView.findViewById(R.id.descriptionEditText);

        new AlertDialog.Builder(getContext())

                .setView(dialogView)
                .setPositiveButton("Crear", (dialog, which) -> {
                    String title = titleEditText.getText().toString().trim();
                    String description = descriptionEditText.getText().toString().trim();

                    if (!title.isEmpty() && !description.isEmpty()) {
                        createForum(title, description);
                    }
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void createForum(String title, String description) {
        String userId = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();

        // Check users information
        db.collection("users")
                .document(userId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        // Create forum
                        Map<String, Object> forum = new HashMap<>();
                        forum.put("userId", userId);
                        forum.put("title", title);
                        forum.put("description", description);
                        forum.put("creationDate", Timestamp.now());
                        forum.put("lastModifiedDate", Timestamp.now());
                        forum.put("commentCount", 0);
                        forum.put("likeCount", 0);
                        forum.put("dislikeCount", 0);
                        forum.put("userLikes", new HashMap<String, Boolean>());

                        // Add forum to collection
                        db.collection("forums").add(forum).addOnSuccessListener(documentReference -> {
                            Log.d("FragForum", "Forum created ID: " + documentReference.getId());
                            loadForumsFromFirestore();
                        });
                    } else {
                        Log.e("FragForum", "User not found in collection users");
                    }
                });
    }
    private void filtrarForums(String query) {
        forumList.clear();

        if (TextUtils.isEmpty(query)) {
            forumList.addAll(fullForumList);
        } else {
            for (Forum forum : fullForumList) {
                if (forum.getTitle() != null && forum.getTitle().toLowerCase().contains(query.toLowerCase())) {
                    forumList.add(forum);
                }
            }
        }
        forumAdapter.notifyDataSetChanged();
    }

    public static void updateForumLikesInFirestore(String forumId, boolean isLike, OnForumUpdatedListener listener) {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        db.collection("forums").document(forumId).get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                Forum forum = documentSnapshot.toObject(Forum.class);
                Map<String, Boolean> userLikes = forum.getUserLikes();

                // Obtener los contadores actuales
                int likeCount = forum.getLikeCount();
                int dislikeCount = forum.getDislikeCount();

                if (userLikes.containsKey(userId)) {
                    boolean currentState = userLikes.get(userId);
                    if (currentState && isLike) {
                        userLikes.remove(userId);
                        likeCount--;
                    } else if (!currentState && !isLike) {
                        userLikes.remove(userId);
                        dislikeCount--;
                    } else if (currentState && !isLike) {
                        userLikes.put(userId, false);
                        likeCount--;
                        dislikeCount++;
                    } else if (!currentState && isLike) {
                        userLikes.put(userId, true);
                        likeCount++;
                        dislikeCount--;
                    }
                } else {
                    userLikes.put(userId, isLike);
                    if (isLike) likeCount++;
                    else dislikeCount++;
                }

                forum.setLikeCount(likeCount);
                forum.setDislikeCount(dislikeCount);
                forum.setUserLikes(userLikes);

                Map<String, Object> updateData = new HashMap<>();
                updateData.put("likeCount", likeCount);
                updateData.put("dislikeCount", dislikeCount);
                updateData.put("userLikes", userLikes);

                // Actualizar solo los campos especificados en Firestore
                db.collection("forums")
                        .document(forumId)
                        .update(updateData)
                        .addOnSuccessListener(aVoid -> {
                            listener.onForumUpdated(forum);  // Devolver el foro actualizado a la UI
                        })
                        .addOnFailureListener(e -> {
                            // Error en la actualización
                        });

            }
        });
    }

    public static String formatLastModifiedDate(Timestamp lastModifiedDate) {
        if (lastModifiedDate != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            return sdf.format(lastModifiedDate.toDate());
        }
        return null; // Retorna null si lastModifiedDate es null
    }



}
