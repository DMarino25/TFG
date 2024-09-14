package com.example.GameApp;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.GameApp.main.MainActivity;

public class RegistreUsuari extends AppCompatActivity {

    private ActivityResultLauncher<Intent> startRegisterAct;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registre_usuari);

        EditText correu = findViewById(R.id.nouCorreu);
        EditText nom = findViewById(R.id.nouUsuari);
        EditText contrasenya = findViewById(R.id.novaContrasenya);
        TextView returnInici = findViewById(R.id.returnInici);
        LinearLayout registre = findViewById(R.id.registre);


        registre.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String strCorreu = correu.getText().toString();
                String strUserName = nom.getText().toString();
                String strPassword = contrasenya.getText().toString();
                if (strCorreu.isEmpty() || strUserName.isEmpty() || strPassword.isEmpty()){

                    Toast.makeText(RegistreUsuari.this, "Completa tots els camps", Toast.LENGTH_SHORT).show();
                }

            }

        });
        returnInici.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(v.getContext(), MainActivity.class);
                startRegisterAct.launch(intent);

            }
        });
        startRegisterAct = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK) {

                    }
                }
        );



    }
}