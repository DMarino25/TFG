package com.example.GameApp;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.GameApp.ClassObjectes.FavoriteGame;

import java.util.ArrayList;

public class FavAdapter extends RecyclerView.Adapter<FavAdapter.FavViewHolder> {

    private ArrayList<FavoriteGame> lgames;
    private Context context;

    public FavAdapter(Context context, ArrayList<FavoriteGame> lgames) {
        this.lgames = lgames;
        this.context = context;
    }

    @Override
    public FavViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.fav_item, parent, false);
        return new FavViewHolder(view);
    }

    @Override
    public void onBindViewHolder(FavViewHolder holder, int position) {
        FavoriteGame game = lgames.get(position);
        holder.name.setText(game.getTitle());
        holder.rating.setText("Rating: " + game.getRating());

        // Cargar imagen usando Glide
        if (game.getCover_url() != null && !game.getCover_url().isEmpty()) {
            Glide.with(context).load(game.getCover_url()).into(holder.game_IMG);
        } else {
            holder.game_IMG.setImageResource(R.drawable.ic_launcher_background);
        }

        // Manejar clic en la tarjeta
        holder.itemView.setOnClickListener(v -> {
            if (game.getCoverId() != 0) {
                Intent intent = new Intent(context, GameDetails.class);
                intent.putExtra("coverId", game.getCoverId());
                context.startActivity(intent);
            } else {
                Toast.makeText(context, "No se puede mostrar los detalles del juego.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public int getItemCount() {
        return lgames.size();
    }

    public class FavViewHolder extends RecyclerView.ViewHolder {
        private TextView name;
        private TextView rating;
        private ImageView game_IMG;

        public FavViewHolder(View view) {
            super(view);
            name = view.findViewById(R.id.title_fav);
            rating = view.findViewById(R.id.rating_fav);
            game_IMG = view.findViewById(R.id.game_IMG);
        }
    }
}
