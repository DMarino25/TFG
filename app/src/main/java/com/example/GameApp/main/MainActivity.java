package com.example.GameApp.main;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.example.GameApp.OauthAct;

import com.example.GameApp.R;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInStatusCodes;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private GoogleSignInClient mGoogleSignInClient;

    private Button enter, oauth;

    private ActivityResultLauncher<Intent> signInLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        oauth = findViewById(R.id.button2);



        // Configura el Google Sign-In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken("840661236439-u1vskjpgspjrdqisaunnb2gkd7pa8eij.apps.googleusercontent.com") // Asegúrate de que esta ID coincida con tu ID de cliente en la consola de Google
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        // Configura el ActivityResultLauncher
        signInLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK) {
                        Intent data = result.getData();
                        Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
                        handleSignInResult(task);
                    } else {
                        Log.w(TAG, "signInResult:failed code=" + result.getResultCode());
                        Toast.makeText(MainActivity.this, "Sign in failed", Toast.LENGTH_LONG).show();
                    }
                });

        oauth.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                signIn();
            }
        });
    }

    private void signIn() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        signInLauncher.launch(signInIntent);
    }

    private void handleSignInResult(Task<GoogleSignInAccount> completedTask) {
        try {
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);
            Log.d(TAG, "signInResult:success, account: " + account.getDisplayName());
            Intent intent = new Intent(MainActivity.this, OauthAct.class);
            startActivity(intent);
        } catch (ApiException e) {
            Log.w(TAG, "signInResult:failed code=" + e.getStatusCode());
            Log.e(TAG, "Exception: ", e);
            Toast.makeText(MainActivity.this, "Sign in failed: " + getErrorMessage(e.getStatusCode()), Toast.LENGTH_LONG).show();
        }
    }

    private String getErrorMessage(int statusCode) {
        switch (statusCode) {
            case GoogleSignInStatusCodes.SIGN_IN_CANCELLED:
                return "Sign in was cancelled.";
            case GoogleSignInStatusCodes.SIGN_IN_FAILED:
                return "Sign in failed.";
            case GoogleSignInStatusCodes.NETWORK_ERROR:
                return "Network error.";
            // Añade más casos según sea necesario
            default:
                return "Unknown error.";
        }
    }
}
