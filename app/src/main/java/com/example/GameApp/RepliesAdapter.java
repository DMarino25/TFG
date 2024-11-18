package com.example.GameApp;

import android.content.Context;
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

        holder.replyUserNameTextView.setText(reply.getReplyUserName());
        holder.replyTextView.setText(reply.getReplyText());

        Glide.with(holder.itemView.getContext())
                .load(reply.getReplyUserPicture())
                .circleCrop()
                .into(holder.replyUserImageView);

        // Mostrar el PopupMenu al mantener pulsado la respuesta
        holder.itemView.setOnLongClickListener(v -> {
            holder.showPopupMenu(holder.itemView, reply, holder.itemView.getContext());
            return true;
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
                    reportComment(reply);
                    Toast.makeText(context, "Respuesta reportada", Toast.LENGTH_SHORT).show();
                    return true;
                }
                return false;
            });

            // Mostrar el PopupMenu
            popupMenu.show();
        }

        private void reportComment(Reply reply) {
            // Lógica para reportar la respuesta
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            Map<String, Object> report = new HashMap<>();
            report.put("replyId", reply.getId());                                           //Comment reported
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
