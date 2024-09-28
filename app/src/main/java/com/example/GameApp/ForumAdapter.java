package com.example.GameApp;

import static com.example.GameApp.FragFolder.FragForum.updateForumLikesInFirestore;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.ImageView;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
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

        // Actualizar los contadores
        holder.likeCount.setText(String.valueOf(forum.getLikeCount()));
        holder.dislikeCount.setText(String.valueOf(forum.getDislikeCount()));

        // Controlar si el usuario ya ha dado like o dislike
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        Boolean userLikeState = forum.getUserLikes().get(userId);

        // Actualizar colores para mostrar el estado actual
        updateLikeDislikeUI(holder, userLikeState);


        // Lógica para el botón de "Like"
        holder.likeButton.setOnClickListener(v -> {
            updateForumLikesInFirestore(forum.getId(), true, updatedForum -> {
                forum.setLikeCount(updatedForum.getLikeCount());
                forum.setDislikeCount(updatedForum.getDislikeCount());
                forum.setUserLikes(updatedForum.getUserLikes());

                // Actualizar la UI después de que Firestore confirme los cambios
                holder.likeCount.setText(String.valueOf(forum.getLikeCount()));
                holder.dislikeCount.setText(String.valueOf(forum.getDislikeCount()));
                setLikeDislikeColors(holder, forum);  // Actualizar colores
            });
        });

        // Lógica para el botón de "Dislike"
        holder.dislikeButton.setOnClickListener(v -> {
            updateForumLikesInFirestore(forum.getId(), false, updatedForum -> {
                forum.setLikeCount(updatedForum.getLikeCount());
                forum.setDislikeCount(updatedForum.getDislikeCount());
                forum.setUserLikes(updatedForum.getUserLikes());

                // Actualizar la UI después de que Firestore confirme los cambios
                holder.likeCount.setText(String.valueOf(forum.getLikeCount()));
                holder.dislikeCount.setText(String.valueOf(forum.getDislikeCount()));
                setLikeDislikeColors(holder, forum);  // Actualizar colores
            });
        });

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
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        Boolean userLikeStatus = forum.getUserLikes().get(userId);

        updateLikeDislikeUI(holder, userLikeStatus);
    }

    // Método para actualizar la UI según el estado del like/dislike
    private void updateLikeDislikeUI(@NonNull ForumViewHolder holder, Boolean userLikeState) {
        if (userLikeState != null && userLikeState) {
            holder.likeButton.setColorFilter(ContextCompat.getColor(holder.itemView.getContext(), R.color.green));
            holder.dislikeButton.setColorFilter(ContextCompat.getColor(holder.itemView.getContext(), R.color.gray));
        } else if (userLikeState != null && !userLikeState) {
            holder.likeButton.setColorFilter(ContextCompat.getColor(holder.itemView.getContext(), R.color.gray));
            holder.dislikeButton.setColorFilter(ContextCompat.getColor(holder.itemView.getContext(), R.color.red));
        } else {
            holder.likeButton.setColorFilter(ContextCompat.getColor(holder.itemView.getContext(), R.color.gray));
            holder.dislikeButton.setColorFilter(ContextCompat.getColor(holder.itemView.getContext(), R.color.gray));
        }
    }
}