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
        String userId =bundle.getString("uid");
        String conversationId = bundle.getString("conversationId");
        TextView Rname = findViewById(R.id.ReceiverName);
        ImageView send = findViewById(R.id.send);
        EditText Write = findViewById(R.id.write);

        firestore.collection("users").document(userId).get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        if (documentSnapshot.exists()) {
                            currentUsername = documentSnapshot.getString("name");
                            Rname.setText(currentUsername);
                        }
                        else{
                            Toast.makeText(getApplicationContext(), "Nom no trobat a la base de dades", Toast.LENGTH_SHORT).show();
                        }

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                    }
                });
            send.setOnClickListener(new View.OnClickListener() {

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