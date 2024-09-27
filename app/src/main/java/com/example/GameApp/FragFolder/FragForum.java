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
import com.google.firebase.firestore.QueryDocumentSnapshot;

public class FragForum extends Fragment {

    private RecyclerView recyclerView;
    private ForumAdapter forumAdapter;
    private List<Forum> forumList;
    private FirebaseFirestore db;
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
        db.collection("forums").get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                forumList.clear();  // Limpiar la lista antes de agregar los datos
                for (QueryDocumentSnapshot document : task.getResult()) {
                    Forum forum = document.toObject(Forum.class);
                    String userId = forum.getUserId();

                    // Formatear fecha
                    Timestamp lastModifiedDate = forum.getLastModifiedDate();
                    if (lastModifiedDate != null) {
                        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                        String formattedDate = sdf.format(lastModifiedDate.toDate());
                        forum.setFormattedDate(formattedDate);
                    }

                    // Consulta adicional para obtener los detalles del usuario
                    db.collection("users").whereEqualTo("userId", userId).get().addOnCompleteListener(userTask -> {
                        if (userTask.isSuccessful()) {
                            if (!userTask.getResult().isEmpty()) {
                                DocumentSnapshot userDocument = userTask.getResult().getDocuments().get(0);
                                String userName = userDocument.getString("userName");
                                String userProfilePhoto = userDocument.getString("userProfilePhoto");

                                forum.setUserName(userName);
                                forum.setUserProfilePhoto(userProfilePhoto);
                                forum.setId(document.getId());

                                forumList.add(forum);
                                forumAdapter.notifyDataSetChanged();
                            }
                        }
                    });
                }
            }
        });
    }

    private void showCreateForumDialog() {
        // Crear y mostrar el popup para crear un foro nuevo
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_create_forum, null);
        TextInputEditText titleEditText = dialogView.findViewById(R.id.titleEditText);
        TextInputEditText descriptionEditText = dialogView.findViewById(R.id.descriptionEditText);
        Log.d("FragForum", "FHOLA: ");

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
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        Map<String, Object> forum = new HashMap<>();
        forum.put("userId", userId);
        forum.put("title", title);
        forum.put("description", description);
        forum.put("creationDate", Timestamp.now());
        forum.put("commentCount", 0);

        db.collection("forums").add(forum).addOnSuccessListener(documentReference -> {
            Log.d("FragForum", "Foro creado con ID: " + documentReference.getId());
            loadForumsFromFirestore();
        });
    }
}
