package com.example.GameApp.FragFolder;

import static java.security.AccessController.getContext;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.GameApp.BuildConfig;
import com.example.GameApp.ClassObjectes.Forum;
import com.example.GameApp.GameDetails;
import com.example.GameApp.main.MainActivity;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import com.example.GameApp.ClassObjectes.Comment;
import com.example.GameApp.CommentsAdapter;
import com.example.GameApp.R;
import com.google.firebase.firestore.QuerySnapshot;

import org.json.JSONException;
import org.json.JSONObject;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ForumDetailsActivity extends AppCompatActivity {

    private TextView forumTitleTextView;
    private TextView forumDescriptionTextView;
    private ImageView forumAuthorImageView;
    private TextView forumAuthorTextView;
    private TextView forumDateTextView, replyText;
    private ListenerRegistration banListener;
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

        // Obtener el forumId pasado desde la actividad anterior
        forumId = getIntent().getStringExtra("forumId");

        // Configurar RecyclerView
        commentList = new ArrayList<>();
        commentsAdapter = new CommentsAdapter(commentList, forumId);
        commentsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        commentsRecyclerView.setAdapter(commentsAdapter);

        replyText.setOnClickListener(v -> {
            // Lógica para abrir un campo de texto y permitir al usuario añadir un comentario
            showReplyDialog(forumId);
        });
        forumAuthorImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Objects.equals(forumAuthorTextView.getText().toString(), "Usuari eliminat")){
                    showUserInfoDialog(ForumDetailsActivity.this, null);
                }
                else {
                    FirebaseFirestore.getInstance().collection("users")
                            .whereEqualTo("name", forumAuthorTextView.getText().toString())
                            .get()
                            .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                                @Override
                                public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                                    if (!queryDocumentSnapshots.isEmpty()) {
                                        DocumentSnapshot userDocument = queryDocumentSnapshots.getDocuments().get(0);
                                        showUserInfoDialog(ForumDetailsActivity.this, userDocument);
                                    }
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    // Toast.makeText(ForumDetailsActivity.this, "Error al carregar informació de l'autor", Toast.LENGTH_SHORT).show();
                                }
                            });
                }
            }
        });

        // Cargar detalles del foro y comentarios
        loadForumDetails();
        setupCommentListener();
    }
    @Override
    protected void onStart() {
        super.onStart();

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            String email = currentUser.getEmail();

            banListener = FirebaseFirestore.getInstance()
                    .collection("bannedUsers")
                    .whereEqualTo("email", email)
                    .addSnapshotListener((querySnapshot, e) -> {
                        if (e != null) {
                            return;
                        }
                        if (querySnapshot != null && !querySnapshot.isEmpty()) {
                            FirebaseAuth.getInstance().signOut();
                            Toast.makeText(ForumDetailsActivity.this,"Usuari bloquejat",Toast.LENGTH_LONG).show();

                            Intent intent = new Intent(ForumDetailsActivity.this, MainActivity.class);
                            startActivity(intent);
                            finish();
                        }
                    });
        }
    }
    @Override
    protected void onStop() {
        super.onStop();
        if (banListener != null) {
            banListener.remove();
            banListener = null;
        }
    }
    private void showUserInfoDialog(Context context, DocumentSnapshot documentSnapshot) {
        // Inflar el layout personalizado para el diálogo
        View dialogView = LayoutInflater.from(context).inflate(R.layout.info_user, null);
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setView(dialogView);

        // Referencias a las vistas del diseño
        ImageView userPP = dialogView.findViewById(R.id.imageView);
        TextView userName = dialogView.findViewById(R.id.nameInfo);
        TextView userDescription = dialogView.findViewById(R.id.editTextText3);
        TextView gameName = dialogView.findViewById(R.id.textView4);
        ImageView gameImage = dialogView.findViewById(R.id.imageView3);

        if (documentSnapshot == null || ! documentSnapshot.exists()){
            userName.setText("Usuari eliminat");
            userDescription.setText("Usuari eliminat");
            userDescription.setTypeface(userDescription.getTypeface(), Typeface.ITALIC);
            gameName.setText("Usuari eliminat");
            gameName.setTypeface(gameName.getTypeface(), Typeface.ITALIC);
        }
        else{
            String name = documentSnapshot.getString("name");
            String description = documentSnapshot.getString("description");
            String photoUrl = documentSnapshot.getString("photoUrl");
            String favoriteGame = documentSnapshot.getString("gameFav");
            String gameImageUrl = documentSnapshot.getString("gameFavImg");

            userName.setText(name);
            if (description != null && !description.isEmpty()) {
                userDescription.setText(description);
            } else {
                userDescription.setText("No s'ha afegit descripció");
                userDescription.setTypeface(userDescription.getTypeface(), Typeface.ITALIC);
            }
            gameName.setText(favoriteGame);
            if (favoriteGame != null && !favoriteGame.isEmpty()) {
                gameName.setText(favoriteGame);
            } else {
                gameName.setText("No s'ha afegit joc favorit");
                gameName.setTypeface(gameName.getTypeface(), Typeface.ITALIC);
            }

            if (photoUrl != null && !photoUrl.isEmpty()) {
                Glide.with(context).load(photoUrl).circleCrop().into(userPP);
            } else {
                userPP.setImageResource(R.mipmap.ic_launcher);
            }

            if (gameImageUrl != null && !gameImageUrl.isEmpty()) {
                Glide.with(context).load(gameImageUrl).into(gameImage);
            } else {
                gameImage.setImageResource(R.mipmap.ic_launcher);
            }
        }

        // Crear y mostrar el diálogo
        AlertDialog dialog = builder.create();
        dialog.show();
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

        if(!Objects.equals(authorName, "Usuari eliminat")){
            // Cargar la imagen de perfil con Glide
            Glide.with(this)
                    .load(userProfilePhoto)
                    .circleCrop()
                    .into(forumAuthorImageView);
        } else {
            Glide.with(this)
                    .load(R.drawable.block_user)
                    .circleCrop()
                    .into(forumAuthorImageView);
        }

    }

    //Popup para insertar un comentario
    private void showReplyDialog(String forumId) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Afegir un comentari");

        // Añadir un campo de texto para el comentario
        final EditText input = new EditText(this);
        input.setHint("Escriu el teu comentari...");
        builder.setView(input);

        // Botón de "Añadir"
        builder.setPositiveButton("Afegir", (dialog, which) -> {
            String commentText = input.getText().toString();
            if (!commentText.isEmpty()) {
                Log.d("ToxicityCheck", "commentText: " + commentText);
                checkCommentToxicity(commentText, forumId);
                // Reset reply states for all existing comments
                for (Comment comment : commentList) {
                    comment.setRepliesVisible(false);
                }
                commentsAdapter.notifyDataSetChanged(); // Refresh RecyclerView
            }
        });

        // Botón de "Cancelar"
        builder.setNegativeButton("Cancel·lar", (dialog, which) -> dialog.cancel());
        builder.show();
    }

    //Añadir un comentario a Firebase
    private void addCommentToFirestore(String commentText, String forumId) {
        String userId = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();
        // New comment
        Map<String, Object> commentMap = new HashMap<>();
        commentMap.put("commentText", commentText);
        commentMap.put("commentUserNameId", userId);
        commentMap.put("lastModifiedDate", new Timestamp(new Date()));

        // Add new comment to the existent forum
        db.collection("forums")
            .document(forumId)
            .collection("comments")
            .add(commentMap)
            .addOnSuccessListener(documentReference -> {
                Log.d("FragForum", "Added comment ID: " + documentReference.getId());
            })
            .addOnFailureListener(e -> {
                Log.e("FragForum", "Error adding the new comment", e);
            });
    }

    //Obtener los comentarios de un foro
    private void setupCommentListener() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("forums")
                .document(forumId)
                .collection("comments")
                .orderBy("lastModifiedDate", Query.Direction.DESCENDING) // Ordenar por fecha en orden descendente
                .addSnapshotListener((snapshots, e) -> {
                    if (e != null) {
                        Log.e("ForumDetails", "Error al obtener los comentarios: ", e);
                        return;
                    }

                    if (snapshots != null && !snapshots.isEmpty()) {
                        // Temporary list for comments
                        List<Comment> tempCommentList = new ArrayList<>();
                        commentList.clear();

                        for (DocumentSnapshot document : snapshots.getDocuments()) {
                            Comment comment = document.toObject(Comment.class);
                            assert comment != null;
                            comment.setId(document.getId());
                            comment.setForumId(forumId);

                            // Fetch user details for each comment
                            String commentUserNameId = comment.getCommentUserNameId();
                            db.collection("users")
                                    .document(commentUserNameId)
                                    .get()
                                    .addOnSuccessListener(userDocument -> {
                                        if (userDocument.exists()) {
                                            // Set the username and profile picture
                                            String userName = userDocument.getString("name");
                                            String userProfilePhoto = userDocument.getString("photoUrl");
                                            comment.setCommentUserName(userName);
                                            comment.setCommentUserPicture(userProfilePhoto);

                                            // Add the comment to the temporary list
                                            tempCommentList.add(comment);

                                            // Check if all comments are processed
                                            if (tempCommentList.size() == snapshots.getDocuments().size()) {
                                                // Sort comments explicitly by lastModifiedDate (DESCENDING)
                                                Collections.sort(tempCommentList, (c1, c2) ->
                                                        c2.getLastModifiedDate().compareTo(c1.getLastModifiedDate())
                                                );

                                                // Update the main comment list and notify adapter
                                                commentList.addAll(tempCommentList);
                                                commentsAdapter.notifyDataSetChanged();
                                            }
                                        } else {
                                            comment.setCommentUserName("Usuari eliminat");
                                            comment.setCommentUserPicture("");
                                            tempCommentList.add(comment);
                                            // Check if all comments are processed
                                            if (tempCommentList.size() == snapshots.getDocuments().size()) {
                                                // Sort comments explicitly by lastModifiedDate (DESCENDING)
                                                Collections.sort(tempCommentList, (c1, c2) ->
                                                        c2.getLastModifiedDate().compareTo(c1.getLastModifiedDate())
                                                );

                                                // Update the main comment list and notify adapter
                                                commentList.addAll(tempCommentList);
                                                commentsAdapter.notifyDataSetChanged();
                                            }
                                            Log.e("ForumDetails", "User document does not exist for ID: " + commentUserNameId);
                                        }
                                    })
                                    .addOnFailureListener(ex -> Log.e("ForumDetails", "Error fetching user details: " + ex.getMessage()));
                        }
                    }
                });
    }


    //Construir el body del Comentario para la llamada de Perspective
    private void checkCommentToxicity(String commentText, String forumId) {
        // Crear un nuevo hilo para evitar bloquear la UI
        new Thread(() -> {
            OkHttpClient client = new OkHttpClient();
            String apiKey = BuildConfig.API_PERSPECTIVE; // API Key

            // URL de la API Perspective
            String url = "https://commentanalyzer.googleapis.com/v1alpha1/comments:analyze?key=" + apiKey;

            // Crear el cuerpo de la solicitud JSON
            String json = "{\n" +
                    "  'comment': { 'text': '" + commentText + "' },\n" +
                    "  'requestedAttributes': { 'TOXICITY': {} }\n" +
                    "}";
            Log.d("ToxicityCheck", "json: " + json);
            RequestBody body = RequestBody.create(json, MediaType.parse("application/json; charset=utf-8"));
            Request request = new Request.Builder()
                    .url(url)
                    .post(body)
                    .build();
            try {
                Response response = client.newCall(request).execute();
                if (response.isSuccessful()) {
                    String responseData = response.body().string();
                    // Procesar la respuesta
                    Log.d("ToxicityCheck", "responseData: " + responseData);
                    handleToxicityResponse(responseData, commentText, forumId);
                }else{
                    addCommentToFirestore(commentText, forumId);
                    Log.d("ToxicityCheck", "response is not successful: " + response);
                }
            } catch (IOException e) {
                Log.e("PerspectiveAPI", "Error en la conexión: ", e);
            }
        }).start();
    }

    //Analizar si el comentario es tóxico
    private void handleToxicityResponse(String responseData, String commentText, String forumId) {
        // Analizar la respuesta JSON para determinar si el comentario es tóxico
        try {
            JSONObject jsonObject = new JSONObject(responseData);
            JSONObject attribute = jsonObject.getJSONObject("attributeScores");
            JSONObject toxicityScore = attribute.getJSONObject("TOXICITY");

            // Acceder al valor dentro del objeto summaryScore
            JSONObject summaryScore = toxicityScore.getJSONObject("summaryScore");
            double score = summaryScore.getDouble("value");

            Log.d("ToxicityCheck", "Puntuación de toxicidad: " + score);
            // Umbral para considerar el comentario como tóxico
            if (score < 0.7) {
                // El comentario no es tóxico, se añade
                addCommentToFirestore(commentText, forumId);
            } else {
                runOnUiThread(() -> Toast.makeText(ForumDetailsActivity.this, getString(R.string.ForumDetailsActivityPerspective), Toast.LENGTH_SHORT).show());
                // El comentario es tóxico, manejar la respuesta según sea necesario
                Log.d("ToxicityCheck", "El comentario es tóxico y no se añadirá.");
            }
        } catch (JSONException e) {
            Log.e("ToxicityCheckError", "Error al analizar la respuesta: ", e);
        }
    }

    //Popup para insertar una respuesta a un comentario
    public void showReplyDialogFromAdapter(Comment comment) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Respondre comentari");

        // Crear un campo de texto para la respuesta
        final EditText input = new EditText(this);
        builder.setView(input);

        // Botón de "Responder"
        builder.setPositiveButton("Respondre", (dialog, which) -> {
            String replyText = input.getText().toString();
            if (!replyText.isEmpty()) {
                checkReplyToxicity(replyText, comment.getId());  // Comprobar toxicidad antes de añadir la respuesta
            }
        });

        // Botón de "Cancelar"
        builder.setNegativeButton("Cancel·lar", (dialog, which) -> dialog.cancel());
        builder.show();
    }

    //Construir el body de la respuesta del comentario para la llamada de Perspective
    private void checkReplyToxicity(String commentText, String commentId) {
        new Thread(() -> {
            OkHttpClient client = new OkHttpClient();
            String apiKey = BuildConfig.API_PERSPECTIVE;
            // URL API Perspective
            String url = "https://commentanalyzer.googleapis.com/v1alpha1/comments:analyze?key=" + apiKey;

            // JSON body
            String json = "{\n" +
                    "  'comment': { 'text': '" + commentText + "' },\n" +
                    "  'requestedAttributes': { 'TOXICITY': {} }\n" +
                    "}";

            RequestBody body = RequestBody.create(json, MediaType.parse("application/json; charset=utf-8"));
            Request request = new Request.Builder()
                    .url(url)
                    .post(body)
                    .build();

            try {
                Response response = client.newCall(request).execute();
                if (response.isSuccessful()) {
                    String responseData = response.body().string();
                    handleToxicityReply(responseData, commentText, commentId);
                }else{
                    addReplyToComment(commentText, commentId);
                    Log.d("ToxicityCheck", "Response is not successful: " + response);
                }
            } catch (IOException e) {
                Log.e("PerspectiveAPI", "Connexion lost: ", e);
            }
        }).start();
    }

    //Controlar que la respuesta no sea tóxico con la puntuación de Perspective
    private void handleToxicityReply(String responseData, String commentText, String commentId) {
        // Analizar la respuesta JSON para determinar si el comentario es tóxico
        try {
            JSONObject jsonObject = new JSONObject(responseData);
            JSONObject attribute = jsonObject.getJSONObject("attributeScores");
            JSONObject toxicityScore = attribute.getJSONObject("TOXICITY");

            // Acceder al valor dentro del objeto summaryScore
            JSONObject summaryScore = toxicityScore.getJSONObject("summaryScore");
            double score = summaryScore.getDouble("value");

            Log.d("ToxicityCheck", "Puntuación de toxicidad: " + score);
            // Umbral para considerar el comentario como tóxico
            if (score < 0.7) {
                // El comentario no es tóxico, se añade
                addReplyToComment(commentText, commentId);
            } else {
                runOnUiThread(() -> Toast.makeText(ForumDetailsActivity.this, getString(R.string.ForumDetailsActivityPerspective), Toast.LENGTH_SHORT).show());
                // El comentario es tóxico, manejar la respuesta según sea necesario
                Log.d("ToxicityCheck", "El comentario es tóxico y no se añadirá.");
            }
        } catch (JSONException e) {
            Log.e("ToxicityCheckError", "Error al analizar la respuesta: ", e);
        }
    }

    //Añade la respuesta del comentario en Firebase
    private void addReplyToComment(String replyText, String commentId) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        String userId = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();
        forumId = getIntent().getStringExtra("forumId");

        // New reply
        Map<String, Object> reply = new HashMap<>();
        reply.put("replyText", replyText);
        reply.put("replyUserNameId", userId);
        reply.put("replyDate", new Timestamp(new Date()));

        // Add new reply to the existent comment
        db.collection("forums")
            .document(forumId)
            .collection("comments")
            .document(commentId)
            .collection("replies")
            .add(reply)
            .addOnSuccessListener(documentReference -> {
                Log.d("ForumDetails", "Reply added successfully");
            })
            .addOnFailureListener(e -> {
                Log.e("ForumDetails", "Error adding the new reply", e);
            });
    }
}
