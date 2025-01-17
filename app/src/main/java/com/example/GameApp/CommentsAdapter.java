package com.example.GameApp;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.AlertDialog;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.GameApp.ClassObjectes.Comment;
import com.example.GameApp.ClassObjectes.Reply;
import com.example.GameApp.FragFolder.ForumDetailsActivity;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class CommentsAdapter extends RecyclerView.Adapter<CommentsAdapter.CommentViewHolder> {

    private static List<Comment> commentList;
    private String forumId;

    public CommentsAdapter(List<Comment> commentList, String forumId) {
        this.commentList = commentList;
        this.forumId = forumId;
    }

    @NonNull
    @Override
    public CommentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.comment_item, parent, false);
        return new CommentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CommentViewHolder holder, int position) {
        Comment comment = commentList.get(position);

        holder.authorTextView.setText(comment.getCommentUserName());
        holder.commentTextView.setText(comment.getCommentText());
        holder.toggleRepliesTextView.setText(comment.areRepliesVisible() ? "Ocultar" : "Veure respostes");

        if(!Objects.equals(comment.getCommentUserName(), "Usuari eliminat")) {
            // Cargar la imagen de perfil con Glide
            Glide.with(holder.itemView.getContext())
                    .load(comment.getCommentUserPicture())
                    .circleCrop()
                    .into(holder.authorImageView);
        } else {
            Glide.with(holder.itemView.getContext())
                    .load(R.drawable.block_user)
                    .circleCrop()
                    .into(holder.authorImageView);
        }
        // Mostrar el PopupMenu al mantener pulsado el comentario
        holder.itemView.setOnLongClickListener(v -> {
            holder.showPopupMenu(holder.itemView, comment, holder.itemView.getContext());
            return true;
        });

        List<Reply> replyList = new ArrayList<>();
        RepliesAdapter repliesAdapter = new RepliesAdapter(replyList);

        holder.repliesRecyclerView.setLayoutManager(new LinearLayoutManager(holder.itemView.getContext()));
        holder.repliesRecyclerView.setAdapter(repliesAdapter);

        holder.getReplies(forumId, comment.getId(), replyList, repliesAdapter);

        holder.toggleRepliesTextView.setOnClickListener(v -> {
            boolean isCurrentlyVisible = comment.areRepliesVisible();
            comment.setRepliesVisible(!isCurrentlyVisible);
            holder.toggleRepliesTextView.setText(!isCurrentlyVisible ? "Ocultar" : "Veure respostes");
            if (!isCurrentlyVisible) {
                // Mostrar respuestas con animación
                holder.repliesRecyclerView.setVisibility(View.VISIBLE);
                holder.repliesRecyclerView.setAlpha(0f);
                holder.repliesRecyclerView.animate()
                        .alpha(1f)
                        .setDuration(300)
                        .setListener(null);
            } else {
                // Ocultar respuestas con animación
                holder.repliesRecyclerView.animate()
                        .alpha(0f)
                        .setDuration(300)
                        .setListener(new AnimatorListenerAdapter() {
                            @Override
                            public void onAnimationEnd(Animator animation) {
                                holder.repliesRecyclerView.setVisibility(View.GONE);
                            }
                        });
            }
        });
    }

    @Override
    public int getItemCount() {
        return commentList.size();
    }

    static class CommentViewHolder extends RecyclerView.ViewHolder {

        ImageView authorImageView;
        TextView authorTextView;
        TextView commentTextView;
        TextView commentReplyText;
        TextView toggleRepliesTextView;
        RecyclerView repliesRecyclerView;

        public CommentViewHolder(@NonNull View itemView) {
            super(itemView);
            authorImageView = itemView.findViewById(R.id.authorImageView);
            authorTextView = itemView.findViewById(R.id.authorTextView);
            commentTextView = itemView.findViewById(R.id.commentTextView);
            commentReplyText = itemView.findViewById(R.id.commentReplyText);
            repliesRecyclerView = itemView.findViewById(R.id.repliesRecyclerView);
            toggleRepliesTextView = itemView.findViewById(R.id.toggleRepliesTextView);

            // Escucha para cuando el usuario haga clic en "Responder"
            commentReplyText.setOnClickListener(v -> {
                // Lógica para abrir el campo de texto y permitir al usuario responder al comentario
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    Comment comment = commentList.get(position);
                    ((ForumDetailsActivity) itemView.getContext()).showReplyDialogFromAdapter(comment); // Aquí llamas a la función que permite responder
                }
            });
        }

        private void getReplies(String forumId, String commentId, final List<Reply> replyList, final RepliesAdapter repliesAdapter) {
            FirebaseFirestore db = FirebaseFirestore.getInstance();

            db.collection("forums")
                    .document(forumId)
                    .collection("comments")
                    .document(commentId)
                    .collection("replies")  // Subcolección de respuestas
                    .orderBy("replyDate", Query.Direction.DESCENDING) // Ordenar las respuestas por fecha
                    .addSnapshotListener((queryDocumentSnapshots, e) -> {
                        if (e != null) {
                            Log.e("getReplies", "Error al obtener las respuestas: ", e);
                            return;
                        }

                        if (queryDocumentSnapshots != null) {
                            replyList.clear();  // Limpiar la lista de respuestas antes de agregar las nuevas
                            for (DocumentSnapshot document : queryDocumentSnapshots.getDocuments()) {
                                Reply reply = document.toObject(Reply.class); // Crear la respuesta desde los datos del documento
                                if (reply != null) {
                                    reply.setId(document.getId()); // Establecer el ID del documento
                                    reply.setCommentId(commentId); // Asignar el commentId a la respuesta
                                    reply.setForumId(forumId);
                                    replyList.add(reply); // Agregar la respuesta a la lista
                                }
                            }
                            repliesAdapter.notifyDataSetChanged();  // Notificar al adaptador de cambios
                        }
                    });
        }

        /*private void showPopupMenu(View view, Comment comment, Context context) {
            // Crear el PopupMenu para las opciones
            PopupMenu popupMenu = new PopupMenu(view.getContext(), view);
            popupMenu.inflate(R.menu.comment_menu); // Asegúrate de tener un menú definido
            popupMenu.setOnMenuItemClickListener(item -> {
                if (item.getItemId() == R.id.report_comment) {
                    reportComment(comment); // Lógica para reportar un comentario
                    Toast.makeText(context, "Comentario reportado", Toast.LENGTH_SHORT).show();
                    return true;
                }
                return false;
            });
            popupMenu.show();
        }*/

        private void showPopupMenu(View view, Comment comment, Context context) {
            // Obtengo el TextView "Responder" desde el layout del comentario
            TextView replyTextView = view.findViewById(R.id.commentReplyText);

            // Creo el PopupMenu anclado al TextView "Responder"
            PopupMenu popupMenu = new PopupMenu(context, replyTextView);
            popupMenu.inflate(R.menu.comment_menu);
            popupMenu.setForceShowIcon(true);   //Icon
            // Acciones al hacer click en los elementos del menú
            popupMenu.setOnMenuItemClickListener(item -> {
                if (item.getItemId() == R.id.report_comment) {
                    reportComment(comment, context);
                    return true;
                }
                return false;
            });

            // Mostrar el PopupMenu
            popupMenu.show();
        }

        private void reportComment(Comment comment, Context context) {
            FirebaseFirestore db = FirebaseFirestore.getInstance();

            // ID del usuario actual
            String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();

            // Obtener el campo "commentUserNameId" del documento correspondiente en la colección de comentarios
            db.collection("forums")
                    .document(comment.getForumId())  // Asegúrate de tener el ID del foro en el objeto Comment
                    .collection("comments")
                    .document(comment.getId()) // ID del comentario
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            // Obtener el campo "commentUserNameId" del documento
                            String commentUserNameId = documentSnapshot.getString("commentUserNameId");

                            if (commentUserNameId != null) {
                                // Verificar si el comentario ya ha sido reportado por este usuario
                                db.collection("reports")
                                        .whereEqualTo("commentId", comment.getId())  // Filtrar por ID del comentario
                                        .whereEqualTo("reporterId", currentUserId)   // Filtrar por ID del usuario que reporta
                                        .get()
                                        .addOnSuccessListener(querySnapshot -> {
                                            if (!querySnapshot.isEmpty()) {
                                                // Ya existe un reporte para este comentario por parte de este usuario
                                                Log.d("reportComment", "Este comentario ya ha sido reportado.");
                                                Toast.makeText(
                                                        itemView.getContext(),
                                                        context.getString(R.string.CommentsAdapterReported),
                                                        Toast.LENGTH_SHORT
                                                ).show();
                                            } else {
                                                // No existe reporte previo, proceder a crearlo
                                                Map<String, Object> report = new HashMap<>();
                                                report.put("commentId", comment.getId());                                           // ID del comentario reportado
                                                report.put("reporterId", currentUserId);                                            // Usuario que reporta
                                                report.put("userId", commentUserNameId);                                            // ID del usuario del comentario
                                                report.put("reportDate", new Timestamp(new Date()));                                // Fecha del reporte
                                                report.put("solved", false);

                                                // Añadir el reporte a la colección "reports"
                                                db.collection("reports")
                                                        .add(report)
                                                        .addOnSuccessListener(documentReference -> {
                                                            Log.d("reportComment", "Comentario reportado con ID: " + documentReference.getId());
                                                            Toast.makeText(itemView.getContext(), context.getString(R.string.CommentsAdapterReport), Toast.LENGTH_SHORT).show();
                                                        })
                                                        .addOnFailureListener(e ->
                                                                Log.e("reportComment", "Error al reportar comentario", e)
                                                        );
                                            }
                                        })
                                        .addOnFailureListener(e ->
                                                Log.e("reportComment", "Error al verificar si el comentario ya fue reportado", e)
                                        );
                            } else {
                                Log.e("reportComment", "El campo 'commentUserNameId' no existe en el documento.");
                            }
                        } else {
                            Log.e("reportComment", "El documento del comentario no existe.");
                        }
                    })
                    .addOnFailureListener(e ->
                            Log.e("reportComment", "Error al obtener el documento del comentario", e)
                    );
        }


    }

}
