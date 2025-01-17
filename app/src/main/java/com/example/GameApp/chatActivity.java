package com.example.GameApp;

import static androidx.constraintlayout.helper.widget.MotionEffect.TAG;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.GameApp.ClassObjectes.Chat;
import com.example.GameApp.main.MainActivity;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class chatActivity extends AppCompatActivity {
    private FirebaseFirestore firestore;
    private FirebaseAuth auth;
    private ArrayList<Chat> messageList;
    private FirebaseUser currentUser;
    private ListenerRegistration banListener;
    private String conversationId;
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
        currentUser = auth.getCurrentUser();

        Bundle bundle = getIntent().getExtras();
        assert bundle != null;
        conversationId = bundle.getString("conversationId");

        TextView ReceiverName = findViewById(R.id.ReceiverName);
        ImageView sendIcon = findViewById(R.id.send);
        ImageView ReceiverPicture = findViewById(R.id.ReceiverPicture);
        EditText Write = findViewById(R.id.write);
        LinearLayout info = findViewById(R.id.userInfo);

        // Load Receiver Information
        loadReceiverInfo(ReceiverName, ReceiverPicture);

        // Send message on button click
        sendIcon.setOnClickListener(v -> {
            String messageText = Write.getText().toString().trim();
            if (!messageText.isEmpty()) {
                sendMessage(messageText);
                Write.setText("");
            } else {
                Toast.makeText(getApplicationContext(), getString(R.string.chatActivityEmptyMessage), Toast.LENGTH_LONG).show();
            }
        });

        // Listen for chat updates
        firestore.collection("messages").document(conversationId).collection("chat")
                .orderBy("timestamp", Query.Direction.ASCENDING)
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot value,
                                        @Nullable FirebaseFirestoreException e) {
                        if (e != null) {
                            Log.w(TAG, "Listen failed.", e);
                            return;
                        }

                        if (value != null) {
                            messageList.clear();
                            for (DocumentSnapshot doc : value.getDocuments()) {
                                Map<String, Object> messageData = doc.getData();
                                if (messageData != null) {
                                    Chat message = new Chat();
                                    message.setSenderId((String) messageData.get("senderId"));
                                    message.setMessageText((String) messageData.get("messageText"));
                                    //message.setTimestamp((Timestamp) messageData.get("timestamp"));
                                    Object timestampObj = messageData.get("timestamp");
                                    if (timestampObj instanceof Timestamp) {
                                        message.setTimestamp((Timestamp) timestampObj);
                                    } else if (timestampObj instanceof Long) {
                                        message.setTimestamp(new Timestamp(new Date((Long) timestampObj)));
                                    }
                                    messageList.add(message);
                                }
                            }
                            chatAdapter.notifyDataSetChanged();
                            scrollToBottom();
                        }
                    }
                });

        // Show user info dialog
        info.setOnClickListener(v -> showUserInfoDialog());
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
                            Toast.makeText(chatActivity.this,"Usuari bloquejat",Toast.LENGTH_LONG).show();

                            Intent intent = new Intent(chatActivity.this, MainActivity.class);
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

    private void loadReceiverInfo(TextView ReceiverName, ImageView ReceiverPicture) {
        firestore.collection("messages").document(conversationId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        List<String> participants = (List<String>) documentSnapshot.get("participants");
                        if (participants != null && participants.size() > 1) {
                            String currentUserId = currentUser.getUid();
                            String receiverUserId = participants.get(0).equals(currentUserId) ? participants.get(1) : participants.get(0);

                            firestore.collection("users").document(receiverUserId).get()
                                    .addOnSuccessListener(userDocument -> {
                                        if (userDocument.exists()) {
                                            String receiverName = userDocument.getString("name");
                                            String receiverPicture = userDocument.getString("photoUrl");

                                            if (receiverPicture != null && !receiverPicture.isEmpty()) {
                                                Glide.with(getApplicationContext())
                                                        .load(receiverPicture)
                                                        .circleCrop()
                                                        .placeholder(R.mipmap.ic_launcher)
                                                        .into(ReceiverPicture);
                                            } else {
                                                ReceiverPicture.setImageResource(R.mipmap.ic_launcher);
                                            }
                                            ReceiverName.setText(receiverName);
                                        } else {
                                            Toast.makeText(getApplicationContext(), getString(R.string.chatActivityUserNotFound), Toast.LENGTH_SHORT).show();
                                        }
                                    })
                                    .addOnFailureListener(e -> Log.e(TAG, "Error fetching user info", e));
                        }
                    }
                })
                .addOnFailureListener(e -> Log.e(TAG, "Error fetching conversation", e));
    }

    private void sendMessage(String messageText) {
        Map<String, Object> newMsg = new HashMap<>();
        newMsg.put("senderId", currentUser.getUid());
        newMsg.put("messageText", messageText);
        newMsg.put("timestamp", new Timestamp(new Date()));

        firestore.collection("messages").document(conversationId).collection("chat")
                .add(newMsg)
                .addOnSuccessListener(documentReference -> {
                    Log.d(TAG, "Message added to chat: " + documentReference.getId());
                    scrollToBottom();
                })
                .addOnFailureListener(e -> Log.w(TAG, "Error adding message", e));
    }

    private void showUserInfoDialog() {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.info_user, null);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(dialogView);

        ImageView userPP = dialogView.findViewById(R.id.imageView);
        ImageView userFG = dialogView.findViewById(R.id.imageView3);
        TextView userName = dialogView.findViewById(R.id.nameInfo);
        TextView description = dialogView.findViewById(R.id.editTextText3);
        TextView nameFG = dialogView.findViewById(R.id.textView4);

        firestore.collection("messages").document(conversationId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        List<String> participants = (List<String>) documentSnapshot.get("participants");
                        if (participants != null && participants.size() > 1) {
                            String currentUserId = currentUser.getUid();
                            String receiverUserId = participants.get(0).equals(currentUserId) ? participants.get(1) : participants.get(0);

                            firestore.collection("users").document(receiverUserId).get()
                                    .addOnSuccessListener(userDocument -> {
                                        if (userDocument.exists()) {
                                            String receiverName = userDocument.getString("name");
                                            String receiverPicture = userDocument.getString("photoUrl");
                                            String receiverDescription = userDocument.getString("description");
                                            String receiverGame = userDocument.getString("gameFav");
                                            String receiverGameImg = userDocument.getString("gameFavImg");

                                            if (receiverPicture != null && !receiverPicture.isEmpty()) {
                                                Glide.with(getApplicationContext())
                                                        .load(receiverPicture)
                                                        .circleCrop()
                                                        .placeholder(R.mipmap.ic_launcher)
                                                        .into(userPP);
                                            } else {
                                                userPP.setImageResource(R.mipmap.ic_launcher);
                                            }

                                            if (receiverGameImg != null) {
                                                Glide.with(getApplicationContext())
                                                        .load(receiverGameImg)
                                                        .placeholder(R.mipmap.ic_launcher)
                                                        .into(userFG);
                                            } else {
                                                userFG.setImageResource(R.mipmap.ic_launcher);
                                            }

                                            userName.setText(receiverName);
                                            description.setText(receiverDescription);
                                            nameFG.setText(receiverGame);
                                        } else {
                                            Toast.makeText(getApplicationContext(), getString(R.string.chatActivityUserNotFound), Toast.LENGTH_SHORT).show();
                                        }
                                    })
                                    .addOnFailureListener(e -> Log.e(TAG, "Error fetching user info", e));
                        }
                    }
                })
                .addOnFailureListener(e -> Log.e(TAG, "Error fetching conversation", e));

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void scrollToBottom() {
        if (messageList.size() > 0) {
            recyclerView.scrollToPosition(messageList.size() - 1);
        }
    }
}
