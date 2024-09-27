package com.example.GameApp;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.GameApp.ClassObjectes.Comment;

import java.util.List;

public class CommentsAdapter extends RecyclerView.Adapter<CommentsAdapter.CommentViewHolder> {

    private List<Comment> commentList;

    public CommentsAdapter(List<Comment> commentList) {
        this.commentList = commentList;
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

        holder.authorTextView.setText(comment.getAuthorName());
        holder.commentTextView.setText(comment.getCommentText());

        // Cargar la imagen de perfil con Glide
        Glide.with(holder.itemView.getContext())
                .load(comment.getAuthorImage())
                .circleCrop()
                .into(holder.authorImageView);
    }

    @Override
    public int getItemCount() {
        return commentList.size();
    }

    static class CommentViewHolder extends RecyclerView.ViewHolder {

        ImageView authorImageView;
        TextView authorTextView;
        TextView commentTextView;

        public CommentViewHolder(@NonNull View itemView) {
            super(itemView);
            authorImageView = itemView.findViewById(R.id.authorImageView);
            authorTextView = itemView.findViewById(R.id.authorTextView);
            commentTextView = itemView.findViewById(R.id.commentTextView);
        }
    }
}
