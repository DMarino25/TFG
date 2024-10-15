package com.example.GameApp;

import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.GameApp.ClassObjectes.Reply;
import com.example.GameApp.R;

import java.util.List;

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
    }
}
