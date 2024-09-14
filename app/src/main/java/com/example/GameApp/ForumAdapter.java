package com.example.GameApp;

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
        TextView titleTextView, descriptionTextView, userNameTextView, dateTextView;
        //TextView titleTextView, descriptionTextView, userNameTextView;
        ImageView userProfileImageView;
        public ForumViewHolder(@NonNull View itemView) {
            super(itemView);
            titleTextView = itemView.findViewById(R.id.titleTextView);
            descriptionTextView = itemView.findViewById(R.id.descriptionTextView);
            userNameTextView = itemView.findViewById(R.id.userNameTextView);
            dateTextView = itemView.findViewById(R.id.dateTextView);
            userProfileImageView = itemView.findViewById(R.id.userProfileImageView);
        }
    }
}