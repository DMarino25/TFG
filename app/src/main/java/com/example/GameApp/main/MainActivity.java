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
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private ActivityResultLauncher<Intent> startRegisterAct;
    private GoogleSignInClient googleSignInClient;
    private FirebaseAuth auth;
    private EditText usuari, contrasenya;
    private FirebaseFirestore firestore;  // Añadido Firestore

    private final ActivityResultLauncher<Intent> signInLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {

                if (result.getResultCode() == RESULT_OK) {
                    // Resultado exitoso
                    Intent data = result.getData();
                    Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
                    handleSignInResult(task);
                } else {
                    // Registro de fallo
                    Log.w(TAG, "signInResult: failed code=" + result.getResultCode());

                    // Imprime el Intent que se devolvió
                    Toast.makeText(MainActivity.this, "Sign in failed", Toast.LENGTH_LONG).show();
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
                .requestIdToken(getString(R.string.default_web_client_id))  // Asegúrate de que este ID sea correcto
                .requestEmail()
                .build();
        googleSignInClient = GoogleSignIn.getClient(this, options);

        LinearLayout iniciGoogle = findViewById(R.id.IniciGoogle);
        LinearLayout iniciNormal = findViewById(R.id.IniciSessio);
        TextView registre = findViewById(R.id.Registre);
        usuari = findViewById(R.id.nameInici);
        contrasenya = findViewById(R.id.contrasenyaInici);
        iniciNormal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String strUserName = usuari.getText().toString();  // Aquí el nombre de usuario
                String strPassword = contrasenya.getText().toString();

                if (strUserName.isEmpty() || strPassword.isEmpty()) {
                    Toast.makeText(MainActivity.this, "Completa tots els camps", Toast.LENGTH_SHORT).show();
                } else {
                    // Primero busca el correo asociado al nombre de usuario en Firestore
                    firestore.collection("users")
                            .whereEqualTo("name", strUserName)
                            .get()
                            .addOnCompleteListener(task -> {
                                if (task.isSuccessful() && !task.getResult().isEmpty()) {
                                    // Obtiene el primer documento (debería ser único)
                                    String email = task.getResult().getDocuments().get(0).getString("email");

                                    // Ahora usa el correo electrónico para iniciar sesión con Firebase Authentication
                                    auth.signInWithEmailAndPassword(email, strPassword)
                                            .addOnCompleteListener(MainActivity.this, new OnCompleteListener<AuthResult>() {
                                                @Override
                                                public void onComplete(@NonNull Task<AuthResult> task) {
                                                    if (task.isSuccessful()) {
                                                        // Inicio de sesión exitoso
                                                        FirebaseUser user = auth.getCurrentUser();
                                                        if (user != null) {
                                                            Toast.makeText(MainActivity.this, "Inicio de sesión exitoso", Toast.LENGTH_SHORT).show();
                                                            Intent intent = new Intent(MainActivity.this, OauthAct.class);
                                                            startActivity(intent);
                                                        }
                                                    } else {
                                                        // Si el inicio de sesión falla, muestra un mensaje
                                                        Toast.makeText(MainActivity.this, "Error en el inicio de sesión: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                                                    }
                                                }
                                            });
                                } else {
                                    Toast.makeText(MainActivity.this, "Usuario no encontrado", Toast.LENGTH_SHORT).show();
                                }
                            });
                }
            }
        });

        iniciGoogle.setOnClickListener(v -> {
            String webClientId = getString(R.string.default_web_client_id);
            Log.d(TAG, "default_web_client_id: " + webClientId);
            Intent signInIntent = googleSignInClient.getSignInIntent();
            // Imprimir el contenido del Intent como URI
            Log.d(TAG, "SignIn Intent URI: " + signInIntent.toUri(Intent.URI_INTENT_SCHEME));

            Bundle extras = signInIntent.getExtras();
            if (extras != null) {
                for (String key : extras.keySet()) {
                    Object value = extras.get(key);
                    Log.d(TAG, String.format("Extras Key: %s, Value: %s", key, value));
                }
            }
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
                    if (result.getResultCode() == RESULT_OK) {

                    }
                }
        );
        Button skipAuth = findViewById(R.id.button_skip);
    }

    private void handleSignInResult(Task<GoogleSignInAccount> completedTask) {
        try {
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);
            if (account != null) {
                AuthCredential credential = GoogleAuthProvider.getCredential(account.getIdToken(), null);
                signInWithGoogle(credential);
            }
        } catch (ApiException e) {
            Log.w(TAG, "signInResult:failed code=" + e.getStatusCode(), e);
            Toast.makeText(MainActivity.this, "Google Sign-In Failed", Toast.LENGTH_LONG).show();
        }
    }

    private void signInWithGoogle(AuthCredential credential) {
        auth.signInWithCredential(credential).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    Log.d(TAG, "Firebase authentication successful");
                    Toast.makeText(MainActivity.this, "Signed In Successfully", Toast.LENGTH_SHORT).show();

                    // Obtener el usuario autenticado
                    if (auth.getCurrentUser() != null) {
                        saveUserInFirestore(auth.getCurrentUser());  // Guardar en Firestore
                    }

                    Intent intent = new Intent(MainActivity.this, OauthAct.class);
                    startActivity(intent);
                } else {
                    Log.e(TAG, "Firebase authentication failed: " + task.getException().getMessage());
                    Toast.makeText(MainActivity.this, "Firebase Authentication Failed", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void saveUserInFirestore(FirebaseUser user) {
        // Crear un objeto Map para almacenar la información del usuario
        Map<String, Object> userMap = new HashMap<>();
        userMap.put("uid", user.getUid());
        userMap.put("name", user.getDisplayName());
        userMap.put("email", user.getEmail());
        userMap.put("photoUrl", user.getPhotoUrl() != null ? user.getPhotoUrl().toString() : null);

        // Guardar los datos en Firestore en la colección "users" con el UID como ID del documento
        firestore.collection("users").document(user.getUid())
                .set(userMap)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Usuario guardado en Firestore con éxito");
                })
                .addOnFailureListener(e -> {
                    Log.w(TAG, "Error al guardar el usuario en Firestore", e);
                });
    }
}
