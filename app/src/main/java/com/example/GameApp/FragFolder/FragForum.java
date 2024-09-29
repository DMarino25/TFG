package com.example.GameApp.FragFolder;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import com.example.GameApp.ClassObjectes.Forum;
import com.example.GameApp.ForumAdapter;
import com.example.GameApp.R;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

public class FragForum extends Fragment {

    private RecyclerView recyclerView;
    private static ForumAdapter forumAdapter;
    private List<Forum> forumList;
    private static FirebaseFirestore db;
    private FloatingActionButton fabCreateForum;

    public FragForum() {
        // Constructor vacío
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_frag_forum, container, false);

        recyclerView = view.findViewById(R.id.recyclerViewForums);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // Inicializamos la lista de foros y el adaptador
        forumList = new ArrayList<>();
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

        // Botón flotante para crear un nuevo foro
        fabCreateForum = view.findViewById(R.id.createForumButton);
        fabCreateForum.setOnClickListener(v -> showCreateForumDialog());

        loadForumsFromFirestore();
        return view;
    }

    private void loadForumsFromFirestore() {
        // SELECT * FROM forums ORDER BY lastModifiedDate DESC;
        db.collection("forums")
                .orderBy("lastModifiedDate", Query.Direction.DESCENDING)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Log.d("FragForum", "Data fetch successful");

                        // Limpiar la lista de foros para evitar duplicados
                        forumList.clear();

                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Forum forum = document.toObject(Forum.class);
                            Log.d("FragForum", "Forum fetched: " + forum.toString());

                            // Obtener la fecha y formatearla
                            Timestamp lastModifiedDate = forum.getLastModifiedDate();
                            String formattedDate = formatLastModifiedDate(lastModifiedDate);
                            forum.setFormattedDate(formattedDate);
                            /*if (lastModifiedDate != null) {
                                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                                String formattedDate = sdf.format(lastModifiedDate.toDate());
                                forum.setFormattedDate(formattedDate);
                            }*/

                            // Establecemos el ID del documento para futuras referencias
                            forum.setId(document.getId());

                            // Añadimos el foro a la lista
                            forumList.add(forum);

                            Log.d("FragForum", "Forum added: " + forum.getTitle() + " by " + forum.getUserName());
                        }

                        // Notificar al adaptador que los datos han cambiado
                        forumAdapter.notifyDataSetChanged();
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
        // Obtén el userId actual
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        // Consulta la información del usuario actual en la colección "users"
        db.collection("users")
                .document(userId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        // Obtén userName y userProfilePhoto del documento de usuario
                        String userName = documentSnapshot.getString("name");
                        String userProfilePhoto = documentSnapshot.getString("photoUrl");

                        // Crea el mapa del nuevo foro con la información del usuario
                        Map<String, Object> forum = new HashMap<>();
                        forum.put("userName", userName);
                        forum.put("userProfilePhoto", userProfilePhoto);
                        forum.put("title", title);
                        forum.put("description", description);
                        forum.put("creationDate", Timestamp.now());
                        forum.put("lastModifiedDate", Timestamp.now());
                        forum.put("commentCount", 0);
                        forum.put("likeCount", 0);
                        forum.put("dislikeCount", 0);
                        forum.put("userLikes", new HashMap<String, Boolean>());

                        // Agrega el nuevo foro a la colección "forums"
                        db.collection("forums").add(forum).addOnSuccessListener(documentReference -> {
                            Log.d("FragForum", "Foro creado con ID: " + documentReference.getId());
                            loadForumsFromFirestore();  // Recargar foros después de crear uno nuevo
                        });
                    } else {
                        Log.e("FragForum", "Usuario no encontrado en la colección 'users'");
                    }
                });
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
