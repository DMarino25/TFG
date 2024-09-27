package com.example.GameApp.FragFolder;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

import com.example.GameApp.ClassObjectes.Comment;
import com.example.GameApp.CommentsAdapter;
import com.example.GameApp.R;

public class ForumDetailsActivity extends AppCompatActivity {

    private TextView forumTitleTextView;
    private TextView forumDescriptionTextView;
    private ImageView forumAuthorImageView;
    private TextView forumAuthorTextView;
    private TextView forumDateTextView;
    private RecyclerView commentsRecyclerView;
    private CommentsAdapter commentsAdapter;
    private List<Comment> commentList;

    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private String forumId; // Debes pasar este valor desde la actividad anterior

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forum_details);

        // Inicializar vistas
        forumTitleTextView = findViewById(R.id.forumTitleTextView);
        forumDescriptionTextView = findViewById(R.id.forumDescriptionTextView);
        forumAuthorImageView = findViewById(R.id.forumAuthorImageView);
        forumAuthorTextView = findViewById(R.id.forumAuthorTextView);
        forumDateTextView = findViewById(R.id.forumDateTextView);
        commentsRecyclerView = findViewById(R.id.commentsRecyclerView);

        // Configurar RecyclerView
        commentList = new ArrayList<>();
        commentsAdapter = new CommentsAdapter(commentList);
        commentsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        commentsRecyclerView.setAdapter(commentsAdapter);

        // Obtener el forumId pasado desde la actividad anterior
        forumId = getIntent().getStringExtra("forumId");

        // Cargar detalles del foro y comentarios
        loadForumDetails();
        loadComments();
    }

    private void loadForumDetails() {
        // Cargar detalles del foro desde Firebase Firestore
        db.collection("forums").document(forumId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        // Obtener los detalles del foro
                        String title = documentSnapshot.getString("title");
                        String description = documentSnapshot.getString("description");
                        String authorName = documentSnapshot.getString("userId");
                        String authorImage = documentSnapshot.getString("authorImage");
                        String date = documentSnapshot.getString("date");

                        // Actualizar las vistas con los detalles del foro
                        forumTitleTextView.setText(title);
                        forumDescriptionTextView.setText(description);
                        forumAuthorTextView.setText(authorName);
                        forumDateTextView.setText(date);

                        // Cargar la imagen de perfil con Glide
                        Glide.with(this)
                                .load(authorImage)
                                .circleCrop()
                                .into(forumAuthorImageView);
                    }
                });
    }

    private void loadComments() {
        // Cargar los comentarios desde Firebase Firestore
        CollectionReference commentsRef = db.collection("comments");

        commentsRef.whereEqualTo("forumId", forumId).get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        String authorName = document.getString("userId");
                        String authorImage = document.getString("authorImage");
                        String commentText = document.getString("content");

                        // Crear un nuevo objeto Comment
                        Comment comment = new Comment(authorName, authorImage, commentText);
                        commentList.add(comment);
                    }

                    // Notificar al adaptador que los datos han cambiado
                    commentsAdapter.notifyDataSetChanged();
                });
    }
}
