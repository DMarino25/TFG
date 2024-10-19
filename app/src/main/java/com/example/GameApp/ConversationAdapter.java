package com.example.GameApp;

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

import java.util.ArrayList;
import java.util.List;

public class ConversationAdapter extends RecyclerView.Adapter<ConversationAdapter.ConversationViewHolder> {

    private ArrayList<Conversation> llistaConverses ;

    private Context context;
    private  OnItemClickListener onClickListener;

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
                                holder.nombreUsuarioTextView.setText(otherUserName);
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
                }
                else {
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
                    context.startActivity(intent);
                    //lastItem = holder;
                    //setSelectedItem(position);
                }
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
           // ultimoMensajeTextView = itemView.findViewById(R.id.ultimoMensajeTextView);
        }
    }
}
