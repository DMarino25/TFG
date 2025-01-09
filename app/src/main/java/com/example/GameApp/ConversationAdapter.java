package com.example.GameApp;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
// Importa los paquetes necesarios
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.GameApp.ClassObjectes.Conversation;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class ConversationAdapter extends RecyclerView.Adapter<ConversationAdapter.ConversationViewHolder> {

    private ArrayList<Conversation> llistaConverses;
    private Context context;
    private OnItemClickListener onClickListener;

    public ConversationAdapter(Context context, ArrayList<Conversation> llistaConverses) {
        this.context = context;
        this.llistaConverses = llistaConverses;
    }

    public interface OnItemClickListener {
        void onItemClick(int position);
    }

    @Override
    public ConversationViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View vista = LayoutInflater.from(context).inflate(R.layout.conversation_item, parent, false);
        return new ConversationViewHolder(vista);
    }

    @Override
    public void onBindViewHolder(ConversationViewHolder holder, int position) {
        Conversation conversa = llistaConverses.get(position);
        List<String> participants = conversa.getParticipants();
        String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        String otherUserId = null;

        for (String participantId : participants) {
            if (!participantId.equals(currentUserId)) {
                otherUserId = participantId;
                break;
            }
        }

        final String finalOtherUserId = otherUserId;
        if (finalOtherUserId != null) {
            // Fetch the other user's name from Firestore
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            db.collection("users").document(otherUserId)
                    .get()
                    .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                        @Override
                        public void onSuccess(DocumentSnapshot documentSnapshot) {
                            if (documentSnapshot.exists()) {
                                String otherUserName = documentSnapshot.getString("name");
                                String myUserId = currentUserId;
                                holder.nombreUsuarioTextView.setText(otherUserName);

                                // Construct possible message document IDs
                                String messageDoc1 = finalOtherUserId + "_" + myUserId;
                                String messageDoc2 = myUserId + "_" + finalOtherUserId;

                                // Check for the existence of the messages document
                                db.collection("messages").document(messageDoc1).get()
                                        .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                            @Override
                                            public void onSuccess(DocumentSnapshot messageDoc1Snapshot) {
                                                if (messageDoc1Snapshot.exists()) {
                                                    fetchLatestMessage(db, messageDoc1, holder);
                                                } else {
                                                    db.collection("messages").document(messageDoc2).get()
                                                            .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                                                @Override
                                                                public void onSuccess(DocumentSnapshot messageDoc2Snapshot) {
                                                                    if (messageDoc2Snapshot.exists()) {
                                                                        fetchLatestMessage(db, messageDoc2, holder);
                                                                    } else {
                                                                        holder.ultimoMensajeTextView.setText("No hi ha cap missatge");
                                                                    }
                                                                }
                                                            })
                                                            .addOnFailureListener(new OnFailureListener() {
                                                                @Override
                                                                public void onFailure(@NonNull Exception e) {
                                                                    holder.ultimoMensajeTextView.setText("No hi ha cap missatge");
                                                                }
                                                            });
                                                }
                                            }
                                        })
                                        .addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                holder.ultimoMensajeTextView.setText("No hi ha cap missatge");
                                            }
                                        });
                            } else {
                                holder.nombreUsuarioTextView.setText("Usuario");
                            }
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            holder.nombreUsuarioTextView.setText("Usuario");
                        }
                    });
        } else {
            holder.nombreUsuarioTextView.setText("Usuario");
        }

        holder.convLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (onClickListener != null) {
                    onClickListener.onItemClick(position);
                } else {
                    String conversationId = conversa.getConversationId();
                    Log.d("ConversationAdapter", "conversationId: " + conversationId);

                    // Si el `conversationId` es null, mostrar un mensaje de error
                    if (conversationId == null) {
                        Log.e("ConversationAdapter", "El conversationId es nulo.");
                        return;
                    }
                    Intent intent = new Intent(context, chatActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent.putExtra("uid", llistaConverses.get(position).getUserIdReceiver());
                    intent.putExtra("conversationId", conversationId);
                    ((Activity) context).startActivityForResult(intent, 100);
                }
            }
        });
    }

    // Helper method to fetch the latest message from the chat subcollection
    public static void fetchLatestMessage(FirebaseFirestore db, String messageDocId, ConversationViewHolder holder) {
        db.collection("messages").document(messageDocId).collection("chat")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .limit(1)
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        if (!queryDocumentSnapshots.isEmpty()) {
                            DocumentSnapshot latestMessage = queryDocumentSnapshots.getDocuments().get(0);
                            String latestMessageText = latestMessage.getString("messageText");
                            holder.ultimoMensajeTextView.setText(latestMessageText);
                        } else {
                            holder.ultimoMensajeTextView.setText("No hi ha cap missatge");
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        holder.ultimoMensajeTextView.setText("No hi ha cap missatge");
                    }
                });
    }

    @Override
    public int getItemCount() {
        return llistaConverses.size();
    }

    public void updateList(ArrayList<Conversation> novaLlista) {
        this.llistaConverses.clear();
        this.llistaConverses.addAll(novaLlista);
        notifyDataSetChanged();
    }

    public class ConversationViewHolder extends RecyclerView.ViewHolder {
        private TextView nombreUsuarioTextView;
        private TextView ultimoMensajeTextView;
        private LinearLayout convLayout;

        public ConversationViewHolder(View itemView) {
            super(itemView);
            nombreUsuarioTextView = itemView.findViewById(R.id.nomUser);
            convLayout = itemView.findViewById(R.id.layoutCon);
            ultimoMensajeTextView = itemView.findViewById(R.id.ultimoMensajeTextView);
        }
    }
}
