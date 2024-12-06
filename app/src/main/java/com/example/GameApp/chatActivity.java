package com.example.GameApp;

import static androidx.constraintlayout.helper.widget.MotionEffect.TAG;
import static androidx.core.content.ContentProviderCompat.requireContext;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.GameApp.ClassObjectes.Chat;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class chatActivity extends AppCompatActivity {
    private FirebaseFirestore firestore;
    private FirebaseAuth auth;
    private ArrayList<Chat> messageList;
    private FirebaseUser currentUser;
    private String currentUsername = null;
    private chatAdapter chatAdapter;

    RecyclerView recyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        messageList = new ArrayList<>();
        chatAdapter = new chatAdapter(this, messageList);

        recyclerView = findViewById(R.id.recyChat);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(chatAdapter);

        auth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();
        currentUser = FirebaseAuth.getInstance().getCurrentUser();

        Bundle bundle = getIntent().getExtras();
        String conversationId = bundle.getString("conversationId");
        TextView ReceiverName = findViewById(R.id.ReceiverName);
        ImageView sendIcon = findViewById(R.id.send);
        ImageView ReceiverPicture = findViewById(R.id.ReceiverPicture);
        EditText Write = findViewById(R.id.write);

        firestore.collection("messages").document(conversationId).get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        if (documentSnapshot.exists()) {
                            List<String> participants = (List<String>) documentSnapshot.get("participants");
                            if (participants != null && participants.size() > 1) {
                                String currentUserId = currentUser.getUid();
                                String receiverUserId;
                                // Determine the peer ID
                                if (participants.get(0).equals(currentUserId)) {
                                    receiverUserId = participants.get(1); // Take the second ID
                                } else {
                                    receiverUserId = participants.get(0); // Take the first ID
                                }
                                // Fetch the name of the user from the "users" collection
                                firestore.collection("users").document(receiverUserId).get()
                                        .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                            @Override
                                            public void onSuccess(DocumentSnapshot userDocument) {
                                                if (userDocument.exists()) {
                                                    String receiverName = userDocument.getString("name");
                                                    String receiverPicture = userDocument.getString("photoUrl");
                                                    // Load receiver's picture into ReceiverPicture using Glide
                                                    if (receiverPicture != null && !receiverPicture.isEmpty()) {
                                                        Glide.with(getApplicationContext())
                                                                .load(receiverPicture)
                                                                .circleCrop()
                                                                .placeholder(R.mipmap.ic_launcher)
                                                                .into(ReceiverPicture);
                                                    } else {
                                                        // Handle case where there is no picture URL, e.g., use a default image
                                                        ReceiverPicture.setImageResource(R.mipmap.ic_launcher);
                                                    }
                                                    ReceiverName.setText(receiverName);
                                                } else {
                                                    Toast.makeText(getApplicationContext(), "Nom no trobat a la base de dades", Toast.LENGTH_SHORT).show();
                                                }
                                            }
                                        })
                                        .addOnFailureListener(e -> {
                                            Log.e("ChatMD", "Error al obtenir el nom de l'usuari", e);
                                        });
                            } else {
                                Toast.makeText(getApplicationContext(), "Participants no trobats a la conversa", Toast.LENGTH_SHORT).show();
                            }
                        }
                        else{
                            Toast.makeText(getApplicationContext(), "Conversa no trobada a la base de dades", Toast.LENGTH_SHORT).show();
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                    }
                });
            sendIcon.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick (View v) {
                        String messageText = Write.getText().toString().trim();
                        if (!messageText.isEmpty()) {

                            FirebaseFirestore db = FirebaseFirestore.getInstance();
                            String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();

                            // Crear un objeto Chat (mensaje)
                            Chat nuevoMensaje = new Chat();
                            nuevoMensaje.setSenderId(currentUserId);
                            nuevoMensaje.setMessageText(messageText);
                            nuevoMensaje.setTimestamp(System.currentTimeMillis());

                            // Agregar el mensaje a la subcolección `chat` dentro del documento de la conversación
                            db.collection("messages").document(conversationId).collection("chat")
                                    .add(nuevoMensaje)
                                    .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                        @Override
                                        public void onSuccess(DocumentReference documentReference) {
                                            Log.d(TAG, "Mensaje añadido a la subcolección 'chat': " + documentReference.getId());
                                        }
                                    })
                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            Log.w(TAG, "Error al agregar mensaje", e);
                                        }
                                    });
                            Write.setText("");
                        } else {
                            Toast.makeText(getApplicationContext(), "No has escrit res.", Toast.LENGTH_LONG).show();
                        }
                    }


            });




        firestore.collection("messages").document(conversationId).collection("chat")
                .orderBy("timestamp", Query.Direction.ASCENDING)
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot value,
                                        @Nullable FirebaseFirestoreException e) {
                        if (e != null) {
                            // Manejar error
                            return;
                        }

                        if (value != null) {
                            messageList.clear();
                            for (DocumentSnapshot doc : value.getDocuments()) {
                                Chat message = doc.toObject(Chat.class);
                                messageList.add(message);
                            }
                            chatAdapter.notifyDataSetChanged();
                        }
                    }
                });
    }
}