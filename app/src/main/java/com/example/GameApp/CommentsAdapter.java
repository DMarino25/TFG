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

        // Cargar la imagen de perfil con Glide
        Glide.with(holder.itemView.getContext())
                .load(comment.getCommentUserPicture())
                .circleCrop()
                .into(holder.authorImageView);

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
            if (holder.repliesRecyclerView.getVisibility() == View.GONE) {
                // Mostrar respuestas con animación
                holder.repliesRecyclerView.setVisibility(View.VISIBLE);
                holder.repliesRecyclerView.setAlpha(0f);
                holder.repliesRecyclerView.animate()
                        .alpha(1f)
                        .setDuration(300)
                        .setListener(null);
                holder.toggleRepliesTextView.setText("Ocultar");
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
                holder.toggleRepliesTextView.setText("Mostrar");
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
                                reply.setId(document.getId()); // Establecer el ID del documento
                                replyList.add(reply); // Agregar la respuesta a la lista
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
                    reportComment(comment);
                    Toast.makeText(context, "Comentario reportado", Toast.LENGTH_SHORT).show();
                    return true;
                }
                return false;
            });

            // Mostrar el PopupMenu
            popupMenu.show();
        }

        private void reportComment(Comment comment) {
            // Lógica para reportar el comentario
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            Map<String, Object> report = new HashMap<>();
            report.put("commentId", comment.getId());                                           //Comment reported
            report.put("reporterId", FirebaseAuth.getInstance().getCurrentUser().getUid());     //Who reported
            report.put("reportDate", new Timestamp(new Date()));                                //Date

            db.collection("reports")
                    .add(report)
                    .addOnSuccessListener(documentReference ->
                            Log.d("CommentsAdapter", "Comentario reportado con ID: " + documentReference.getId())
                    )
                    .addOnFailureListener(e ->
                            Log.e("CommentsAdapter", "Error al reportar comentario", e)
                    );
        }
    }

}
