package com.example.GameApp;

import static com.example.GameApp.FragFolder.FragForum.updateForumLikesInFirestore;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.ImageView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.GameApp.ClassObjectes.Forum;
import java.util.List;

import com.bumptech.glide.Glide;
import com.example.GameApp.FragFolder.FragForum;
import com.google.firebase.auth.FirebaseAuth;

public class ForumAdapter extends RecyclerView.Adapter<ForumAdapter.ForumViewHolder> {

    private List<Forum> forumList;
    private OnForumClickListener listener;

    public interface OnForumClickListener {
        void onForumClick(Forum forum);
    }

    public ForumAdapter(List<Forum> forumList, OnForumClickListener listener) {
        this.forumList = forumList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ForumViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.forum_item, parent, false);
        return new ForumViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ForumViewHolder holder, int position) {

        Forum forum = forumList.get(position);

        holder.titleTextView.setText(forum.getTitle());
        holder.descriptionTextView.setText(forum.getDescription());
        holder.userNameTextView.setText(forum.getUserName());
        holder.dateTextView.setText(forum.getFormattedDate());

        Glide.with(holder.itemView.getContext())
                .load(forum.getUserProfilePhoto())
                .circleCrop()
                .into(holder.userProfileImageView);

        // Setear los contadores de like/dislike
        holder.likeCount.setText(String.valueOf(forum.getLikeCount()));
        holder.dislikeCount.setText(String.valueOf(forum.getDislikeCount()));

        // Inicializar colores según los contadores
        //setLikeDislikeColors(holder, forum);

        // Lógica para los botones de like/dislike
        holder.likeButton.setOnClickListener(v -> {
            /*String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
            boolean isCurrentlyLiked = forum.getUserLikes().get(userId) != null && forum.getUserLikes().get(userId);

            // Actualizar el like/dislike
            if (isCurrentlyLiked) {
                // Si el usuario ya ha dado like, quitar el like
                //forum.getUserLikes().remove(userId);
                forum.setLikeCount(forum.getLikeCount() - 1);
            } else {
                // Si el usuario ha dado dislike, añadir el like y quitar dislike
                //forum.getUserLikes().put(userId, true);
                forum.setLikeCount(forum.getLikeCount() + 1);
                // Si también tiene dislike, quitar el dislike
                if (forum.getUserLikes().get(userId) != null && forum.getUserLikes().get(userId) == false) {
                    //forum.getUserLikes().remove(userId); // Quitar dislike
                    forum.setDislikeCount(forum.getDislikeCount() - 1);
                }
            }*/

            // Actualizar la interfaz
            //forumAdapter.notifyDataSetChanged();
            // Llamar a Firestore para guardar el estado
            updateForumLikesInFirestore(forum.getId(), true);

        });


        holder.dislikeButton.setOnClickListener(v -> {
            /*String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
            boolean isCurrentlyDisliked = forum.getUserLikes().get(userId) != null && !forum.getUserLikes().get(userId);

            // Actualizar el like/dislike
            if (!isCurrentlyDisliked) {
                // Si el usuario ya ha dado dislike, quitar el dislike
                //forum.getUserLikes().remove(userId);
                forum.setDislikeCount(forum.getDislikeCount() - 1);
            } else {
                // Si el usuario no ha dado dislike, añadir el dislike
                //forum.getUserLikes().put(userId, false);
                forum.setDislikeCount(forum.getDislikeCount() + 1);
                // Si también tiene like, quitar el like
                if (forum.getUserLikes().get(userId) != null && forum.getUserLikes().get(userId) == true) {
                    //forum.getUserLikes().remove(userId); // Quitar like
                    forum.setLikeCount(forum.getLikeCount() - 1);
                }
            }*/

            // Actualizar la interfaz
            //forumAdapter.notifyDataSetChanged();
            // Llamar a Firestore para guardar el estado
            updateForumLikesInFirestore(forum.getId(), false);
        });
        Log.d("FragForum", "Like:"+forum.getLikeCount()+"//Dislike:"+forum.getDislikeCount());
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onForumClick(forum);
            }
        });
    }

    @Override
    public int getItemCount() {
        return forumList.size();
    }

    static class ForumViewHolder extends RecyclerView.ViewHolder {
        TextView titleTextView, descriptionTextView, userNameTextView, dateTextView, likeCount, dislikeCount;
        //TextView titleTextView, descriptionTextView, userNameTextView;
        ImageView userProfileImageView, likeButton, dislikeButton;
        public ForumViewHolder(@NonNull View itemView) {
            super(itemView);
            titleTextView = itemView.findViewById(R.id.titleTextView);
            descriptionTextView = itemView.findViewById(R.id.descriptionTextView);
            userNameTextView = itemView.findViewById(R.id.userNameTextView);
            dateTextView = itemView.findViewById(R.id.dateTextView);
            userProfileImageView = itemView.findViewById(R.id.userProfileImageView);
            likeCount = itemView.findViewById(R.id.likeCount);
            dislikeCount = itemView.findViewById(R.id.dislikeCount);
            likeButton = itemView.findViewById(R.id.likeButton);
            dislikeButton = itemView.findViewById(R.id.dislikeButton);
        }
    }

    // Método para cambiar el color de los botones de like y dislike
    private void setLikeDislikeColors(@NonNull ForumViewHolder holder, Forum forum) {
        // Asigna el color dependiendo del estado del foro
        if (forum.getLikeCount() > 0) {
            //holder.likeButton.setColorFilter(Integer.parseInt("4CAF50"));
        } else {
            //holder.likeButton.setColorFilter(Integer.parseInt("808080"));
        }

        if (forum.getDislikeCount() > 0) {
            //holder.dislikeButton.setColorFilter(Integer.parseInt("F44336"));
        } else {
            //holder.dislikeButton.setColorFilter(Integer.parseInt("808080"));
        }
    }
}