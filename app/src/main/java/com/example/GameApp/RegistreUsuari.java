package com.example.GameApp;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.GameApp.main.MainActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class RegistreUsuari extends AppCompatActivity {

    private FirebaseAuth auth;
    private FirebaseFirestore firestore;
    private ActivityResultLauncher<Intent> startRegisterAct;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registre_usuari);


        auth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();

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

                if (strCorreu.isEmpty() || strUserName.isEmpty() || strPassword.isEmpty()) {
                    Toast.makeText(RegistreUsuari.this, "Completa tots els camps", Toast.LENGTH_SHORT).show();
                } else {

                    auth.createUserWithEmailAndPassword(strCorreu, strPassword)
                            .addOnCompleteListener(RegistreUsuari.this, new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    if (task.isSuccessful()) {
                                        FirebaseUser user = auth.getCurrentUser();

                                        saveUserInFirestore(user, strUserName);

                                        Toast.makeText(RegistreUsuari.this, "Usuario creado", Toast.LENGTH_SHORT).show();

                                        // Volver a MainActivity
                                        Intent intent = new Intent(RegistreUsuari.this, MainActivity.class);
                                        startActivity(intent);
                                        finish();
                                    } else {
                                        Toast.makeText(RegistreUsuari.this, "Error al crear el usuario: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                                    }
                                }
                            });
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

    private void saveUserInFirestore(FirebaseUser user, String userName) {

        Map<String, Object> userMap = new HashMap<>();
        userMap.put("uid", user.getUid());
        userMap.put("name", userName);
        userMap.put("email", user.getEmail());

        firestore.collection("users").document(user.getUid())
                .set(userMap)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(RegistreUsuari.this, "Usuario guardado en Firestore", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(RegistreUsuari.this, "Error al guardar en Firestore", Toast.LENGTH_SHORT).show();
                });
    }
}
