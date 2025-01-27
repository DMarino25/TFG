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
import java.util.HashSet;
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
        String conversationId = conversa.getConversationId();
        holder.setBoundConversationId(conversationId);

        List<String> participants = conversa.getParticipants();
        String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        String otherUserId = null;

        // Log para identificar en qué posición estamos
        Log.d("ConversationAdapter", "onBindViewHolder - position: " + position);

        // Recorremos los participantes para encontrar el "otro" usuario (que no sea el currentUser)
        for (String participantId : participants) {
            if (!participantId.equals(currentUserId)) {
                otherUserId = participantId;
                break;
            }
        }

        // Log del currentUserId y del otro, si existe
        Log.d("ConversationAdapter", "currentUserId: " + currentUserId);
        Log.d("ConversationAdapter", "otherUserId: " + otherUserId);

        final String finalOtherUserId = otherUserId;
        if (finalOtherUserId != null) {
            // Fetch del otro usuario
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            db.collection("users").document(finalOtherUserId)
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            // Obtenemos el nombre
                            String otherUserName = documentSnapshot.getString("name");
                            // Log con la info obtenida
                            Log.d("ConversationAdapter",
                                    "Documento para " + finalOtherUserId + " existe. Nombre: " + otherUserName);

                            if (holder.getBoundConversationId().equals(conversationId)) {
                                holder.nombreUsuarioTextView.setText(otherUserName);
                                holder.ultimoMensajeTextView.setText("Disponible");
                            } else {
                                Log.d("ConversationAdapter", "El holder ya está reciclado para otra conversación");
                            }
                        } else {
                            Log.d("ConversationAdapter",
                                    "No existe documento para userId " + finalOtherUserId);
                            if (holder.getBoundConversationId().equals(conversationId)) {
                                holder.nombreUsuarioTextView.setText("Usuari");
                                holder.ultimoMensajeTextView.setText("Usuari eliminat");
                            }
                        }
                    })
                    .addOnFailureListener(e -> {
                        Log.e("ConversationAdapter",
                                "Error al obtener documento de userId " + finalOtherUserId, e);
                        if (holder.getBoundConversationId().equals(conversationId)) {
                            holder.nombreUsuarioTextView.setText("Usuari");
                            holder.ultimoMensajeTextView.setText("Usuari eliminat");
                        }
                    });
        } else {
            // Si no hay otro userId, utilizamos el currentUser (caso raro pero contemplado)
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            db.collection("users").document(currentUserId)
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            String name = documentSnapshot.getString("name");
                            Log.d("ConversationAdapter",
                                    "Documento para currentUserId " + currentUserId + " existe. Nombre: " + name);

                            if (holder.getBoundConversationId().equals(conversationId)) {
                                holder.nombreUsuarioTextView.setText(name);
                                holder.ultimoMensajeTextView.setText("Disponible");
                            }
                        } else {
                            Log.d("ConversationAdapter",
                                    "No existe documento para currentUserId " + currentUserId);
                            if (holder.getBoundConversationId().equals(conversationId)) {
                                holder.nombreUsuarioTextView.setText("Usuari");
                                holder.ultimoMensajeTextView.setText("Usuari eliminat");
                            }
                        }
                    })
                    .addOnFailureListener(e -> {
                        Log.e("ConversationAdapter",
                                "Error al obtener documento de currentUserId " + currentUserId, e);
                        if (holder.getBoundConversationId().equals(conversationId)) {
                            holder.nombreUsuarioTextView.setText("Usuari");
                            holder.ultimoMensajeTextView.setText("Usuari eliminat");
                        }
                    });
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
                    //intent.putExtra("uid", llistaConverses.get(position).getUserIdReceiver());
                    intent.putExtra("conversationId", conversationId);
                    context.startActivity(intent);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return llistaConverses.size();
    }

    public void updateList(ArrayList<Conversation> novaLlista) {
        HashSet<String> conversationIds = new HashSet<>();
        ArrayList<Conversation> filteredList = new ArrayList<>();

        for (Conversation conversa : novaLlista) {
            if (!conversationIds.contains(conversa.getConversationId())) {
                conversationIds.add(conversa.getConversationId());
                filteredList.add(conversa);
            }
        }
        this.llistaConverses.clear();
        this.llistaConverses.addAll(novaLlista);
        notifyDataSetChanged();
    }

    public class ConversationViewHolder extends RecyclerView.ViewHolder {
        private TextView nombreUsuarioTextView;
        private TextView ultimoMensajeTextView;
        private LinearLayout convLayout;
        private String boundConversationId;

        public ConversationViewHolder(View itemView) {
            super(itemView);
            nombreUsuarioTextView = itemView.findViewById(R.id.nomUser);
            convLayout = itemView.findViewById(R.id.layoutCon);
            ultimoMensajeTextView = itemView.findViewById(R.id.ultimoMensajeTextView);
        }
        public void setBoundConversationId(String conversationId) {
            this.boundConversationId = conversationId;
        }

        // Getter para recuperarlo en el callback
        public String getBoundConversationId() {
            return boundConversationId;
        }
    }
}
