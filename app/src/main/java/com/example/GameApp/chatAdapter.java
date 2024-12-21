package com.example.GameApp;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.GameApp.ClassObjectes.Chat;
import com.google.firebase.auth.FirebaseAuth;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class chatAdapter extends RecyclerView.Adapter<chatAdapter.chatViewHolder> {
    private List<Chat> mensajes;
    public String currentUser;

    private Context context;
    private TextView textoUsers;
    private TextView dateat;

    public static final int enviado = 1;
    public static final int recibido = 2;


    public chatViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType){
        View vista;
        if (viewType == enviado) {
            vista = LayoutInflater.from(context).inflate(R.layout.chat_item_enviado, parent, false);
        } else {
            vista = LayoutInflater.from(context).inflate(R.layout.chat_item_recibido, parent, false);
        }
        return new chatViewHolder(vista);
    }
    public chatAdapter(Context context, ArrayList<Chat> mensajes) {
        this.context = context;
        this.mensajes = mensajes;
        this.currentUser = FirebaseAuth.getInstance().getCurrentUser().getUid();
    }
    @Override
    public void onBindViewHolder(chatAdapter.chatViewHolder holder, int position) {
        Chat mensaje = mensajes.get(position);
        holder.textoUsers.setText(mensaje.getMessageText());
        //holder.dateat.setText(new SimpleDateFormat("HH:mm").format(new Date(mensaje.getTimestamp())));
    }
    public int getItemCount() {
        return mensajes.size();
    }

    public int getItemViewType (int position){
        Chat mensaje = mensajes.get(position);
        if(mensaje.getSenderId().equals(currentUser)){
            return enviado;
        }
        else{
            return recibido;
        }


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
