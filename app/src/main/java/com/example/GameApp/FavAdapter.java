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
import java.util.List;

public class FavAdapter extends RecyclerView.Adapter<FavAdapter.FavViewHolder> {

    private ArrayList<FavoriteGame> lgames;
    private Context context;
    private boolean yesDescription;
    private OnGameClickListener listener;

    public interface OnGameClickListener {
        void onGameClick(FavoriteGame game);
    }

    public FavAdapter(Context context, ArrayList<FavoriteGame> lgames, boolean yesDescription, OnGameClickListener listener ) {
        this.lgames = lgames;
        this.context = context;
        this.yesDescription= yesDescription;
        this.listener = listener;
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
            String imageId = CoverUtils.extractImageId(game.getCover_url());
            String imageUrl = CoverUtils.constructImageUrl(imageId, "t_1080p");
            Glide.with(context).load(imageUrl).into(holder.game_IMG);
        } else {
            holder.game_IMG.setImageResource(R.drawable.ic_launcher_background);
        }

        // Manejar clic en la tarjeta
        holder.itemView.setOnClickListener(v -> {
            if (yesDescription && game.getCoverId() != 0) {
                Intent intent = new Intent(context, GameDetails.class);
                intent.putExtra("coverId", game.getCoverId());
                context.startActivity(intent);
            }
            else if(!yesDescription && game.getCoverId() !=0){
                listener.onGameClick(game);
            }
            else {
                Toast.makeText(context, "No es poden mostrar els detalls del joc.", Toast.LENGTH_SHORT).show();
            }
        });
    }


    @Override
    public int getItemCount() {
        return lgames.size();
    }

    public void updateList(ArrayList<FavoriteGame> newList) {
        this.lgames.clear(); // Limpiar la lista actual
        this.lgames.addAll(newList); // Añadir los nuevos elementos
        notifyDataSetChanged(); // Notificar al adaptador que los datos han cambiado
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
