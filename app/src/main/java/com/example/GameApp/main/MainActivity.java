package com.example.GameApp.main;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.GameApp.OauthAct;
import com.example.GameApp.R;
import com.example.GameApp.RegistreUsuari;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private ActivityResultLauncher<Intent> startRegisterAct;
    private GoogleSignInClient googleSignInClient;
    private FirebaseAuth auth;
    private TextInputEditText correu, contrasenya;
    private FirebaseFirestore firestore;  // Añadido Firestore

    private final ActivityResultLauncher<Intent> signInLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {

                if (result.getResultCode() == RESULT_OK) {

                    Intent data = result.getData();
                    Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
                    handleSignInResult(task);
                } else {
                    // Registro de fallo
                    Log.w(TAG, "signInResult: failed code=" + result.getResultCode());
                    Toast.makeText(MainActivity.this, getString(R.string.signInFailed), Toast.LENGTH_LONG).show();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Inicializamos Firebase y Firestore
        FirebaseApp.initializeApp(this);
        auth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();  // Inicialización de Firestore

        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser != null) {
            Intent intent = new Intent(MainActivity.this, OauthAct.class);
            startActivity(intent);
            finish();
        }

        // Configuramos Google Sign-In
        GoogleSignInOptions options = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        googleSignInClient = GoogleSignIn.getClient(this, options);

        MaterialButton iniciGoogle = findViewById(R.id.googleLoginButton);
        MaterialButton iniciNormal = findViewById(R.id.loginButton);
        TextView registre = findViewById(R.id.signUpLink);
        correu = findViewById(R.id.usernameInput);
        contrasenya = findViewById(R.id.passwordInput);
        iniciNormal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String strCorreu = correu.getText().toString();  // Aquí el nombre de usuario
                String strPassword = contrasenya.getText().toString();

                if (strCorreu.isEmpty() || strPassword.isEmpty()) {
                    Toast.makeText(MainActivity.this, getString(R.string.usrpwdEmpty), Toast.LENGTH_SHORT).show();
                } else {

                                    firestore.collection("bannedUsers")
                                            .whereEqualTo("email", strCorreu)
                                            .get()
                                            .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                                @Override
                                                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                                    if (task.isSuccessful()) {
                                                        if (task.getResult() != null && !task.getResult().isEmpty()) {
                                                            Toast.makeText(MainActivity.this, "Usari bloquejat", Toast.LENGTH_LONG).show();
                                                        } else {
                                                            auth.signInWithEmailAndPassword(strCorreu, strPassword)
                                                                    .addOnCompleteListener(MainActivity.this, new OnCompleteListener<AuthResult>() {
                                                                        @Override
                                                                        public void onComplete(@NonNull Task<AuthResult> task) {
                                                                            if (task.isSuccessful()) {
                                                                                FirebaseUser user = auth.getCurrentUser();
                                                                                if (user != null) {
                                                                                    Toast.makeText(MainActivity.this, getString(R.string.signInSuccess), Toast.LENGTH_SHORT).show();
                                                                                    Intent intent = new Intent(MainActivity.this, OauthAct.class);
                                                                                    startActivity(intent);
                                                                                }
                                                                            } else {

                                                                                Toast.makeText(MainActivity.this, getString(R.string.signInFailed), Toast.LENGTH_LONG).show();
                                                                            }
                                                                        }
                                                                    });
                                                        }

                                                    } else {
                                                        Toast.makeText(MainActivity.this, getString(R.string.usrMissing), Toast.LENGTH_SHORT).show();
                                                    }
                                                }

                                            });

                }
            }
        });

        iniciGoogle.setOnClickListener(v -> {
            String webClientId = getString(R.string.default_web_client_id);
            Intent signInIntent = googleSignInClient.getSignInIntent();
            signInLauncher.launch(signInIntent);
        });
        registre.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(v.getContext(), RegistreUsuari.class);
                startRegisterAct.launch(intent);
            }
        });
        startRegisterAct = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if(result.getResultCode() == RESULT_OK) {

                    }
                }
        );
    }
    private void checkBannedUser(final String email, final AuthCredential credential){
            firestore.collection("bannedUsers")
                    .whereEqualTo("email", email)
                    .get()
                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            if(task.isSuccessful()){
                                if(task.getResult()!= null && !task.getResult().isEmpty()){
                                    Toast.makeText(MainActivity.this, "Usari bloquejat", Toast.LENGTH_LONG).show();
                                    googleSignInClient.signOut();
                                }
                                else{
                                    signInWithGoogle(credential);
                                }
                            }else{
                                Toast.makeText(MainActivity.this,
                                        "Error consultando baneados: " +
                                                task.getException().getMessage(),
                                        Toast.LENGTH_LONG).show();
                            }
                        }
                    });
    }

    private void handleSignInResult(Task<GoogleSignInAccount> completedTask) {
        try {
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);
            if (account != null) {
                AuthCredential credential = GoogleAuthProvider.getCredential(account.getIdToken(), null);
                String googleEmail = account.getEmail();
                checkBannedUser(googleEmail,credential);

            }
        } catch (ApiException e) {
            Log.w(TAG, "signInResult:failed code=" + e.getStatusCode(), e);
            Toast.makeText(MainActivity.this, getString(R.string.signInFailed), Toast.LENGTH_LONG).show();
        }
    }

    private void signInWithGoogle(AuthCredential credential) {
        auth.signInWithCredential(credential).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    Log.d(TAG, "Firebase authentication successful");
                    Toast.makeText(MainActivity.this, getString(R.string.signInSuccess), Toast.LENGTH_SHORT).show();

                    if (auth.getCurrentUser() != null) {
                        saveUserInFirestore(auth.getCurrentUser());  // Guardar en Firestore
                    }

                    Intent intent = new Intent(MainActivity.this, OauthAct.class);
                    startActivity(intent);
                } else {
                    Log.e(TAG, "Firebase authentication failed: " + task.getException().getMessage());
                    Toast.makeText(MainActivity.this, getString(R.string.signInFailed), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void saveUserInFirestore(FirebaseUser user) {

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
                        userMap.put("name", user.getDisplayName());
                        userMap.put("email", user.getEmail());
                        userMap.put("photoUrl", user.getPhotoUrl() != null ? user.getPhotoUrl().toString() : null);
                        userMap.put("noGames", false);
                        userMap.put("noFav", false);
                        userMap.put("noFor", false);

                        documentReference.set(userMap).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void unused) {
                                Log.d(TAG, "Usuari guardat en Firestore amb éxit");

                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.w(TAG, "Error al guardar l'usuari a Firestore", e);

                            }
                        });
                    }
                } else {
                    Log.w(TAG, "Error al verificar si l'usuari existeix en Firestore", task.getException());
                }
            }

        });
    }
}
