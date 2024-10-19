package com.example.GameApp;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.GameApp.ClassObjectes.Chat;
import com.example.GameApp.ClassObjectes.Conversation;
import com.example.GameApp.ClassObjectes.User;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class chatAdapter extends RecyclerView.Adapter<chatAdapter.chatViewHolder> {
    private List<Chat> mensajes;
    private Context context;
    private TextView textoUsers;
    private TextView dateat;

    public chatViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType){
        View vista = LayoutInflater.from(context).inflate(R.layout.chat_item, parent, false);
        return new chatAdapter.chatViewHolder(vista);
    }
    public chatAdapter(Context context, ArrayList<Chat> mensajes) {
        this.context = context;
        this.mensajes = mensajes;
    }
    @Override
    public void onBindViewHolder(chatAdapter.chatViewHolder holder, int position) {
        Chat mensaje = mensajes.get(position);
        holder.textoUsers.setText(mensaje.getMessageText());
        holder.dateat.setText(new SimpleDateFormat("HH:mm").format(new Date(mensaje.getTimestamp())));
    }
    public int getItemCount() {
        return mensajes.size();
    }

    public class chatViewHolder extends RecyclerView.ViewHolder {
        private TextView textoUsers;
        private TextView dateat;
        private LinearLayout convLayout;

        public chatViewHolder(View itemView) {
            super(itemView);
            textoUsers = itemView.findViewById(R.id.mensajetexto);
            dateat = itemView.findViewById(R.id.textohora);
        }
    }
}
