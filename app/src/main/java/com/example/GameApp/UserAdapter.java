package com.example.GameApp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
// Importa los paquetes necesarios
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.GameApp.ClassObjectes.User;

import java.util.ArrayList;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.UserViewHolder> {

    private ArrayList<User> llistaUsuaris;
    private Context context;
    private OnUserClickListener listener;

    // Interfaz para manejar el clic en el botón "Conversa"
    public interface OnUserClickListener {
        void onConversaClick(User usuari);
    }

    // Constructor del adaptador
    public UserAdapter(Context context, ArrayList<User> llistaUsuaris, OnUserClickListener listener) {
        this.context = context;
        this.llistaUsuaris = llistaUsuaris;
        this.listener = listener;
    }

    @Override
    public UserViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View vista = LayoutInflater.from(context).inflate(R.layout.message_item, parent, false);
        return new UserViewHolder(vista);
    }

    @Override
    public void onBindViewHolder(UserViewHolder holder, int position) {
        final User usuari = llistaUsuaris.get(position);
        holder.nomTextView.setText(usuari.getName());

        // Cargar imagen del usuario usando Glide
        if (usuari.getPhotoUrl() != null && !usuari.getPhotoUrl().isEmpty()) {
            Glide.with(context).load(usuari.getPhotoUrl()).into(holder.fotoImageView);
        } else {
            holder.fotoImageView.setImageResource(R.drawable.ic_launcher_background);
        }

        // Manejar clic en el botón "Conversa"
        holder.conversaButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (listener != null) {
                    listener.onConversaClick(usuari);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return llistaUsuaris.size();
    }

    // Método para actualizar la lista de usuarios
    public void updateList(ArrayList<User> novaLlista) {
        this.llistaUsuaris.clear();
        this.llistaUsuaris.addAll(novaLlista);
        notifyDataSetChanged();
    }

    public class UserViewHolder extends RecyclerView.ViewHolder {
        private TextView nomTextView;
        private ImageView fotoImageView;
        private Button conversaButton;

        public UserViewHolder(View itemView) {
            super(itemView);
            nomTextView = itemView.findViewById(R.id.authorTextView);
            fotoImageView = itemView.findViewById(R.id.authorImageView);
            conversaButton = itemView.findViewById(R.id.conversa);
        }
    }
}
