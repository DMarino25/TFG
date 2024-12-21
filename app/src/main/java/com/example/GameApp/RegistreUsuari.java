package com.example.GameApp;

import static androidx.constraintlayout.helper.widget.MotionEffect.TAG;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.GameApp.main.MainActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
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

        TextInputEditText correu = findViewById(R.id.emailInput);
        TextInputEditText nom = findViewById(R.id.usernameInput);
        TextInputEditText contrasenya = findViewById(R.id.passwordInput);
        TextView returnInici = findViewById(R.id.loginLink);
        MaterialButton registre = findViewById(R.id.registerButton);

        registre.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String strCorreu = correu.getText().toString();
                String strUserName = nom.getText().toString();
                String strPassword = contrasenya.getText().toString();

                if (strCorreu.isEmpty() || strUserName.isEmpty() || strPassword.isEmpty()) {
                    Toast.makeText(RegistreUsuari.this, getString(R.string.usrpwdEmpty), Toast.LENGTH_SHORT).show();
                } else {

                    auth.createUserWithEmailAndPassword(strCorreu, strPassword)
                            .addOnCompleteListener(RegistreUsuari.this, new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    if (task.isSuccessful()) {
                                        FirebaseUser user = auth.getCurrentUser();

                                        saveUserInFirestore(user, strUserName);

                                        Toast.makeText(RegistreUsuari.this, getString(R.string.RegistreUsuariCreated), Toast.LENGTH_SHORT).show();

                                        // Volver a MainActivity
                                        Intent intent = new Intent(RegistreUsuari.this, MainActivity.class);
                                        startActivity(intent);
                                        finish();
                                    } else {
                                        Toast.makeText(RegistreUsuari.this, getString(R.string.RegistreUsuariError), Toast.LENGTH_LONG).show();
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
        DocumentReference documentReference = firestore.collection("users").document(user.getUid());

        documentReference.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot documentSnapshot = task.getResult();
                    if (documentSnapshot != null && documentSnapshot.exists()) {
                        Log.d(TAG, "Usuari existent");
                    } else {
                        Map<String, Object> userMap = new HashMap<>();
                        userMap.put("uid", user.getUid());
                        userMap.put("name", userName);
                        userMap.put("email", user.getEmail());
                        userMap.put("photoUrl", user.getPhotoUrl() != null ? user.getPhotoUrl().toString() : null);
                        userMap.put("noGames", false);
                        userMap.put("noFav", false);
                        userMap.put("noFor", false);

                        documentReference.set(userMap).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void unused) {
                                Log.d(TAG, "Usuario guardado en Firestore con éxito");

                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.w(TAG, "Error al guardar el usuario en Firestore", e);

                            }
                        });
                    }
                } else {
                    Log.w(TAG, "Error al verificar si el usuario existe en Firestore", task.getException());
                }
            }

        });
    }

}
