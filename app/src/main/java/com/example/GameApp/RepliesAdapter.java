package com.example.GameApp;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Typeface;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.GameApp.ClassObjectes.Comment;
import com.example.GameApp.ClassObjectes.Reply;
import com.example.GameApp.R;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RepliesAdapter extends RecyclerView.Adapter<RepliesAdapter.ReplyViewHolder> {

    private List<Reply> replyList;

    public RepliesAdapter(List<Reply> replyList) {
        this.replyList = replyList;
    }

    @NonNull
    @Override
    public ReplyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.reply_item, parent, false);
        return new ReplyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ReplyViewHolder holder, int position) {
        Reply reply = replyList.get(position);

        // Fetch the user details dynamically
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        String replyUserNameId = reply.getReplyUserNameId();

        db.collection("users")
                .document(replyUserNameId)
                .get()
                .addOnSuccessListener(userDocument -> {
                    if (userDocument.exists()) {
                        // Populate user details
                        String userName = userDocument.getString("name");
                        String userProfilePicture = userDocument.getString("photoUrl");

                        reply.setReplyUserName(userName);
                        reply.setReplyUserPicture(userProfilePicture);

                        // Update UI
                        holder.replyUserNameTextView.setText(reply.getReplyUserName());
                        Glide.with(holder.itemView.getContext())
                                .load(reply.getReplyUserPicture())
                                .circleCrop()
                                .into(holder.replyUserImageView);
                    } else {
                        reply.setReplyUserName("Usuari eliminat");
                        reply.setReplyUserPicture("");
                        holder.replyUserNameTextView.setText(reply.getReplyUserName());
                        Glide.with(holder.itemView.getContext())
                                .load(R.drawable.block_user)
                                .circleCrop()
                                .into(holder.replyUserImageView);
                        Log.e("ReplyAdapter", "User not found for ID: " + replyUserNameId);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("ReplyAdapter", "Error fetching user details: " + e.getMessage());
                    holder.replyUserNameTextView.setText("Usuari desconegut");
                });

        // Set reply text
        holder.replyTextView.setText(reply.getReplyText());

        // Show PopupMenu on long click
        holder.itemView.setOnLongClickListener(v -> {
            holder.showPopupMenu(holder.itemView, reply, holder.itemView.getContext());
            return true;
        });
        holder.replyUserImageView.setOnClickListener(v -> {
            Context context = holder.itemView.getContext();
            holder.showUserInfoDialog(context, reply);
        });
    }


    @Override
    public int getItemCount() {
        return replyList.size();
    }

    static class ReplyViewHolder extends RecyclerView.ViewHolder {
        ImageView replyUserImageView;
        TextView replyUserNameTextView;
        TextView replyTextView;

        public ReplyViewHolder(@NonNull View itemView) {
            super(itemView);
            replyUserImageView = itemView.findViewById(R.id.replyUserImageView);
            replyUserNameTextView = itemView.findViewById(R.id.replyUserNameTextView);
            replyTextView = itemView.findViewById(R.id.replyTextView);
        }

        private void showPopupMenu(View view, Reply reply, Context context) {
            // Obtengo el  desde el layout del comentario
            TextView replyTextView = view.findViewById(R.id.replyTextView);

            // Creo el PopupMenu anclado al TextView "Responder"
            PopupMenu popupMenu = new PopupMenu(context, replyTextView);
            popupMenu.inflate(R.menu.comment_menu);
            popupMenu.setForceShowIcon(true);   //Icon
            // Acciones al hacer click en los elementos del menú
            popupMenu.setOnMenuItemClickListener(item -> {
                if (item.getItemId() == R.id.report_comment) {
                    reportReply(reply, context);
                    return true;
                }
                return false;
            });

            // Mostrar el PopupMenu
            popupMenu.show();
        }
        private void showUserInfoDialog(Context context, Reply reply) {
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

            FirebaseFirestore.getInstance().collection("users")
                    .document(reply.getReplyUserNameId())
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            String name = documentSnapshot.getString("name");
                            String description = documentSnapshot.getString("description");
                            String photoUrl = documentSnapshot.getString("photoUrl");
                            String favoriteGame = documentSnapshot.getString("gameFav");
                            String gameImageUrl = documentSnapshot.getString("gameFavImg");

                            userName.setText(name);
                            if (description != null && !description.isEmpty()) {
                                userDescription.setText(description);
                            } else {
                                userDescription.setText("No s'ha afegit joc descripció");
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
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(context, "Error al carregar informació de l'usuari", Toast.LENGTH_SHORT).show();
                    });

            // Crear y mostrar el diálogo
            AlertDialog dialog = builder.create();
            dialog.show();
        }

        private void reportReply(Reply reply, Context context) {
            FirebaseFirestore db = FirebaseFirestore.getInstance();

            // Obtener el campo "replyUserNameId" del documento correspondiente en la subcolección "replies"
            db.collection("forums")
                    .document(reply.getForumId())  // Asegúrate de tener el ID del foro en el objeto Reply
                    .collection("comments")
                    .document(reply.getCommentId()) // ID del comentario
                    .collection("replies")
                    .document(reply.getId()) // ID de la respuesta
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            // Obtener el campo "replyUserNameId" del documento
                            String replyUserNameId = documentSnapshot.getString("replyUserNameId");

                            if (replyUserNameId != null) {
                                // Verificar si ya existe un reporte para esta respuesta por parte del usuario actual
                                String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
                                db.collection("reports")
                                        .whereEqualTo("replyId", reply.getId())
                                        .whereEqualTo("reporterId", currentUserId)
                                        .get()
                                        .addOnSuccessListener(querySnapshot -> {
                                            if (!querySnapshot.isEmpty()) {
                                                // Ya existe un reporte, mostrar mensaje
                                                Log.d("reportReply", "Este mensaje ya ha sido reportado.");
                                                Toast.makeText(
                                                        itemView.getContext(),
                                                        context.getString(R.string.RepliesAdapterReported),
                                                        Toast.LENGTH_SHORT
                                                ).show();
                                            } else {
                                                // No existe reporte, proceder a crearlo
                                                Map<String, Object> report = new HashMap<>();
                                                report.put("replyId", reply.getId());                                               // Reply reportada
                                                report.put("reporterId", currentUserId);                                            // Usuario que reporta
                                                report.put("reportDate", new Timestamp(new Date()));                                // Fecha del reporte
                                                report.put("userId", replyUserNameId);                                              // ID del usuario de la respuesta
                                                report.put("commentId", reply.getCommentId());                                      // ID del comentario relacionado
                                                report.put("solved", false);

                                                // Añadir el reporte a la colección "reports"
                                                db.collection("reports")
                                                        .add(report)
                                                        .addOnSuccessListener(documentReference ->
                                                                Log.d("reportReply", "Comentario reportado con ID: " + documentReference.getId())
                                                        )
                                                        .addOnFailureListener(e ->
                                                                Log.e("reportReply", "Error al reportar comentario", e)
                                                        );
                                                Toast.makeText(itemView.getContext(), context.getString(R.string.RepliesAdapterReport), Toast.LENGTH_SHORT).show();
                                            }
                                        })
                                        .addOnFailureListener(e ->
                                                Log.e("reportReply", "Error al verificar si el mensaje ya fue reportado", e)
                                        );
                            } else {
                                Log.e("reportReply", "El campo 'replyUserNameId' no existe en el documento.");
                            }
                        } else {
                            Log.e("reportReply", "El documento de la respuesta no existe.");
                        }
                    })
                    .addOnFailureListener(e ->
                            Log.e("reportReply", "Error al obtener el documento de la respuesta", e)
                    );
        }

    }

}
