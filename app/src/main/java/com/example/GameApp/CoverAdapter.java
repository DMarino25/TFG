package com.example.GameApp;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.GameApp.ClassObjectes.Cover;
import com.example.GameApp.ClassObjectes.Game;

import java.util.List;
import java.util.Map;

public class CoverAdapter extends RecyclerView.Adapter<CoverAdapter.CoverViewHolder> {
    private List<Cover> coverList;

    public CoverAdapter(List<Cover> coverList) {
        this.coverList = coverList;
    }

    @NonNull
    @Override
    public CoverViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.cover_item, parent, false);
        return new CoverViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CoverViewHolder holder, int position) {
        Cover cover = coverList.get(position);

        // Cargar la imagen de la portada
        Glide.with(holder.itemView.getContext())
                .load("https:" + cover.getUrl())
                .into(holder.coverImageView);

        // Mostrar el nombre del juego
        holder.gameName.setText(cover.getGameName());
    }

    @Override
    public int getItemCount() {
        return coverList.size();
    }

    class CoverViewHolder extends RecyclerView.ViewHolder {
        ImageView coverImageView;
        TextView gameName;

        public CoverViewHolder(@NonNull View itemView) {
            super(itemView);
            coverImageView = itemView.findViewById(R.id.coverImageView);
            gameName = itemView.findViewById(R.id.gameTitle);
        }
    }
}
