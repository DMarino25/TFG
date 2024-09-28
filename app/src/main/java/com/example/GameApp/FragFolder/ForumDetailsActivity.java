package com.example.GameApp.FragFolder;

import android.app.AlertDialog;
import android.os.Bundle;
import android.util.Log;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.GameApp.ClassObjectes.Forum;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
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
    private TextView forumDateTextView, replyText;
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
        replyText = findViewById(R.id.replyText);

        // Configurar RecyclerView
        commentList = new ArrayList<>();
        commentsAdapter = new CommentsAdapter(commentList);
        commentsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        commentsRecyclerView.setAdapter(commentsAdapter);

        // Obtener el forumId pasado desde la actividad anterior
        forumId = getIntent().getStringExtra("forumId");

        replyText.setOnClickListener(v -> {
            // Lógica para abrir un campo de texto y permitir al usuario añadir un comentario
            showReplyDialog(forumId);
        });

        // Cargar detalles del foro y comentarios
        loadForumDetails();
        loadComments();
    }

    private void loadForumDetails() {
        // Cargar detalles del foro desde Firebase Firestore
        String title = getIntent().getStringExtra("forumTitle");
        String description = getIntent().getStringExtra("forumDescription");
        String authorName = getIntent().getStringExtra("userName");
        String userProfilePhoto = getIntent().getStringExtra("userProfilePhoto");
        String date = getIntent().getStringExtra("lastModifiedDate");

        // Actualizar las vistas con los detalles del foro
        forumTitleTextView.setText(title);
        forumDescriptionTextView.setText(description);
        forumAuthorTextView.setText(authorName);
        forumDateTextView.setText(date);

        // Cargar la imagen de perfil con Glide
        Glide.with(this)
                .load(userProfilePhoto)
                .circleCrop()
                .into(forumAuthorImageView);
    }

    private void loadComments() {
        // Cargar los comentarios desde el subdocumento "comments" dentro de cada "forum"
        db.collection("forums").document(forumId).collection("comments").get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        String commentUserName = document.getString("commentUserName");
                        String userProfilePhoto = document.getString("commentUserPicture");
                        String commentText = document.getString("commentText");

                        // Crear un nuevo objeto Comment con la información del usuario y el comentario
                        Comment comment = new Comment(commentUserName, userProfilePhoto, commentText, Timestamp.now());
                        commentList.add(comment);

                        // Notificar al adaptador que los datos han cambiado
                        commentsAdapter.notifyDataSetChanged();
                    }
                });
    }

    private void showReplyDialog(String forumId) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Añadir un comentario");

        // Añadir un campo de texto para el comentario
        final EditText input = new EditText(this);
        input.setHint("Escribe tu comentario...");
        builder.setView(input);

        // Botón de "Añadir"
        builder.setPositiveButton("Añadir", (dialog, which) -> {
            String commentText = input.getText().toString();
            if (!commentText.isEmpty()) {
                // Lógica para añadir el comentario a Firestore
                addCommentToFirestore(commentText, forumId);
            }
        });

        // Botón de "Cancelar"
        builder.setNegativeButton("Cancelar", (dialog, which) -> dialog.cancel());

        builder.show();
    }

    private void addCommentToFirestore(String commentText, String forumId) {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        // Consulta la información del usuario actual en la colección "users"
        db.collection("users")
                .document(userId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        // Obtén userName y userProfilePhoto del documento de usuario
                        String userName = documentSnapshot.getString("userName");
                        String userProfilePhoto = documentSnapshot.getString("userProfilePhoto");

                        // Crear el nuevo comentario
                        Comment newComment = new Comment(userName, userProfilePhoto, commentText, Timestamp.now());

                        // Añadir el comentario a la subcolección "comments" dentro del documento del foro
                        db.collection("forums")
                                .document(forumId)
                                .collection("comments") // Aquí creas la subcolección
                                .add(newComment) // Añade el comentario a la subcolección
                                .addOnSuccessListener(documentReference -> {
                                    // El comentario se añadió con éxito
                                    Log.d("FragForum", "Comentario añadido con ID: " + documentReference.getId());
                                })
                                .addOnFailureListener(e -> {
                                    // Ocurrió un error
                                    Log.e("FragForum", "Error al añadir comentario", e);
                                });
                    }
                });
    }


}
