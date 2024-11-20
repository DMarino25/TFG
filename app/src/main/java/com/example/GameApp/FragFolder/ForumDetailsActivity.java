package com.example.GameApp.FragFolder;

import static java.security.AccessController.getContext;

import android.app.AlertDialog;
import android.os.Bundle;
import android.util.Log;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

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
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import com.example.GameApp.ClassObjectes.Comment;
import com.example.GameApp.CommentsAdapter;
import com.example.GameApp.R;

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

        // Cargar detalles del foro y comentarios
        loadForumDetails();
        setupCommentListener();
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

    /*private void loadComments() {
        // Cargar los comentarios desde el subdocumento "comments" dentro de cada "forum"
        db.collection("forums").document(forumId).collection("comments").get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        String commentUserName = document.getString("commentUserName");
                        String userProfilePhoto = document.getString("commentUserPicture");
                        String commentText = document.getString("commentText");

                        // Crear un nuevo objeto Comment con la información del usuario y el comentario
                        Comment comment = new Comment(commentUserName, userProfilePhoto, commentText, Timestamp.now());
                        comment.setId(document.getId());
                        Log.d("ToxicityCheck", "comment.setId(document.getId()): " + document.getId());
                        commentList.add(comment);

                        // Notificar al adaptador que los datos han cambiado
                        commentsAdapter.notifyDataSetChanged();
                    }
                });
    }*/

    //Popup para insertar un comentario
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
                Log.d("ToxicityCheck", "commentText: " + commentText);
                checkCommentToxicity(commentText, forumId);
            }
        });

        // Botón de "Cancelar"
        builder.setNegativeButton("Cancelar", (dialog, which) -> dialog.cancel());
        builder.show();
    }

    //Añadir un comentario a Firebase
    private void addCommentToFirestore(String commentText, String forumId) {
        String userId = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();
        // Consulta la información del usuario actual en la colección "users"
        db.collection("users")
            .document(userId)
            .get()
            .addOnSuccessListener(documentSnapshot -> {
                if (documentSnapshot.exists()) {
                    // Obtén name y photoUrl del documento de usuario
                    String name = documentSnapshot.getString("name");
                    String photoUrl = documentSnapshot.getString("photoUrl");

                    // Crear el nuevo comentario
                    Map<String, Object> commentMap = new HashMap<>();
                    commentMap.put("commentText", commentText);
                    commentMap.put("commentUserName", name);
                    commentMap.put("commentUserPicture", photoUrl);
                    commentMap.put("lastModifiedDate", new Timestamp(new Date()));

                    // Añadir el comentario a la subcolección "comments" dentro del documento del foro
                    db.collection("forums")
                        .document(forumId)
                        .collection("comments") // Aquí creas la subcolección
                        .add(commentMap) // Añade el comentario a la subcolección
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
                    commentList.clear();
                    for (DocumentSnapshot document : snapshots.getDocuments()) {
                        Comment comment = document.toObject(Comment.class);
                        comment.setId(document.getId());
                        commentList.add(comment);
                    }
                    commentsAdapter.notifyDataSetChanged(); // Actualizar el RecyclerView
                }
            });
    }

    //Construir el body del Comentario para la llamada de Perspective
    private void checkCommentToxicity(String commentText, String forumId) {
        // Crear un nuevo hilo para evitar bloquear la UI
        new Thread(() -> {
            OkHttpClient client = new OkHttpClient();
            String apiKey = "AIzaSyCNGsAwGpJF2DOTricV1hFDCLuixbpEFpU"; // API Key

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
                    runOnUiThread(() -> Toast.makeText(ForumDetailsActivity.this, "[DEBUG] No se ha podido hacer la llamada a Perspective, pero añadimos comentario", Toast.LENGTH_SHORT).show());
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
                runOnUiThread(() -> Toast.makeText(ForumDetailsActivity.this, "Missatge no pèrmes.", Toast.LENGTH_SHORT).show());
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
        builder.setTitle("Responder comentario");

        // Crear un campo de texto para la respuesta
        final EditText input = new EditText(this);
        builder.setView(input);

        // Botón de "Responder"
        builder.setPositiveButton("Responder", (dialog, which) -> {
            String replyText = input.getText().toString();
            if (!replyText.isEmpty()) {
                checkReplyToxicity(replyText, comment.getId());  // Comprobar toxicidad antes de añadir la respuesta
            }
        });

        // Botón de "Cancelar"
        builder.setNegativeButton("Cancelar", (dialog, which) -> dialog.cancel());
        builder.show();
    }

    //Construir el body de la respuesta del comentario para la llamada de Perspective
    private void checkReplyToxicity(String commentText, String commentId) {
        // Crear un nuevo hilo para evitar bloquear la UI
        new Thread(() -> {
            OkHttpClient client = new OkHttpClient();
            String apiKey = "AIzaSyCNGsAwGpJF2DOTricV1hFDCLuixbpEFpU"; // API Key

            // URL de la API Perspective
            String url = "https://commentanalyzer.googleapis.com/v1alpha1/comments:analyze?key=" + apiKey;

            // Crear el cuerpo de la solicitud JSON
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
                    // Procesar la respuesta
                    handleToxicityReply(responseData, commentText, commentId);
                }else{
                    runOnUiThread(() -> Toast.makeText(ForumDetailsActivity.this, "[DEBUG] No se ha podido hacer la llamada a Perspective, pero añadimos respuesta", Toast.LENGTH_SHORT).show());
                    addReplyToComment(commentText, commentId);
                    Log.d("ToxicityCheck", "response is not successful: " + response);
                }
            } catch (IOException e) {
                Log.e("PerspectiveAPI", "Error en la conexión: ", e);
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
                //commentText = commentText + " Puntuación de toxicidad: " + score;
                addReplyToComment(commentText, commentId);
            } else {
                runOnUiThread(() -> Toast.makeText(ForumDetailsActivity.this, "Missatge no pèrmes.", Toast.LENGTH_SHORT).show());
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
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        forumId = getIntent().getStringExtra("forumId");

        // Obtener los datos del usuario desde la colección "users"
        db.collection("users")
            .document(userId)
            .get()
            .addOnSuccessListener(documentSnapshot -> {
                if (documentSnapshot.exists()) {
                    // Obtener los datos del usuario de Firestore
                    String userName = documentSnapshot.getString("name"); // Cambia "userName" según el campo que uses
                    String userProfilePic = documentSnapshot.getString("photoUrl"); // Obtener la URL de la foto de perfil desde Firestore

                    // Crear el objeto Reply con los datos necesarios
                    Map<String, Object> reply = new HashMap<>();
                    reply.put("replyText", replyText);
                    reply.put("replyUserNameId", userId);
                    reply.put("replyUserName", userName);
                    reply.put("replyUserPicture", userProfilePic);
                    reply.put("replyDate", new Timestamp(new Date()));

                    // Añadir la respuesta a la subcolección "replies" dentro del comentario
                    db.collection("forums")
                        .document(forumId)
                        .collection("comments")
                        .document(commentId)
                        .collection("replies")
                        .add(reply)
                        .addOnSuccessListener(documentReference -> {
                            // Éxito al añadir la respuesta
                            Log.d("ForumDetails", "Respuesta añadida correctamente");
                        })
                        .addOnFailureListener(e -> {
                            // Error al añadir la respuesta
                            Log.e("ForumDetails", "Error al añadir la respuesta", e);
                        });
                } else {
                    Log.e("ForumDetails", "Usuario no encontrado en la colección 'users'");
                }
            })
            .addOnFailureListener(e -> {
                Log.e("ForumDetails", "Error al obtener los datos del usuario", e);
            });
    }


}
