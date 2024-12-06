package com.example.GameApp.FragFolder;

import static java.lang.Float.parseFloat;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.GameApp.R;
import com.example.GameApp.main.MainActivity;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.HashMap;
import java.util.Map;

/**
 * A simple {@link Fragment} subclass.
 */
public class FragAjust extends Fragment {

    private FirebaseAuth auth;
    private GoogleSignInClient googleSignInClient;

    private FirebaseFirestore firestore;
    private FirebaseUser currentUser;

    private String currentUsername = null;

    public FragAjust() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_frag_ajust, container, false);

        auth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        String userId = currentUser.getUid();


        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();
        googleSignInClient = GoogleSignIn.getClient(getActivity(), gso);

        LinearLayout feedback = v.findViewById(R.id.feedback);
        LinearLayout logout = v.findViewById(R.id.logout);
        LinearLayout delete = v.findViewById(R.id.deleteAccount);
        EditText UserName= v.findViewById(R.id.UserName);
        ImageView ProfilePicture = v.findViewById(R.id.ProfilePicture);

        // New buttons for tick and cross
        ImageView tickButton = v.findViewById(R.id.tickButton);
        ImageView crossButton = v.findViewById(R.id.crossButton);

        // Initially hide the tick and cross buttons
        tickButton.setVisibility(View.GONE);
        crossButton.setVisibility(View.GONE);

        firestore.collection("users").document(userId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        currentUsername = documentSnapshot.getString("name");
                        String receiverPicture = documentSnapshot.getString("photoUrl");

                        // Load receiver's picture using Glide
                        if (receiverPicture != null && !receiverPicture.isEmpty()) {
                            Glide.with(v.getContext())
                                    .load(receiverPicture)
                                    .circleCrop()
                                    .placeholder(R.mipmap.ic_launcher)
                                    .into(ProfilePicture);
                        } else {
                            // Default image if no URL
                            ProfilePicture.setImageResource(R.mipmap.ic_launcher);
                        }
                        UserName.setText(currentUsername);
                    } else {
                        Toast.makeText(v.getContext(), "Nom no trobat a la base de dades", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(v.getContext(), "Error al obtenir dades: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                );

        // Set focus change listener on UserName
        UserName.setOnFocusChangeListener((view, hasFocus) -> {
            if (hasFocus) {
                // Enable editing and show tick and cross buttons
                UserName.setEnabled(true);

                tickButton.setVisibility(View.VISIBLE);
                crossButton.setVisibility(View.VISIBLE);
            } else {
                // Optionally, hide the buttons when focus is lost
                tickButton.setVisibility(View.GONE);
                crossButton.setVisibility(View.GONE);
            }
        });

        // Handle the tick button (Save changes)
        tickButton.setOnClickListener(view -> {
            String newUsername = UserName.getText().toString().trim();
            if (newUsername.isEmpty()) {
                Toast.makeText(v.getContext(), "Nom d'usuari buit", Toast.LENGTH_SHORT).show();
            } else if (newUsername.equals(currentUsername)) {
                Toast.makeText(v.getContext(), "És el mateix nom d'usuari", Toast.LENGTH_SHORT).show();
            } else {
                firestore.collection("users").document(userId)
                        .update("name", newUsername)
                        .addOnSuccessListener(aVoid -> {
                            Toast.makeText(v.getContext(), "Username actualitzat correctament", Toast.LENGTH_SHORT).show();
                            currentUsername = newUsername;
                            // Hide tick and cross buttons
                            tickButton.setVisibility(View.GONE);
                            crossButton.setVisibility(View.GONE);
                        })
                        .addOnFailureListener(e ->
                                Toast.makeText(v.getContext(), "Error al actualitzar username: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                        );
            }
            UserName.clearFocus();
        });

        // Handle the cross button (Cancel changes)
        crossButton.setOnClickListener(view -> {
            UserName.setText(currentUsername);
            UserName.clearFocus();
            // Hide tick and cross buttons
            tickButton.setVisibility(View.GONE);
            crossButton.setVisibility(View.GONE);
        });

        feedback.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                View dialogView = LayoutInflater.from(v.getContext()).inflate(R.layout.dialog_feedback,null);
                AlertDialog.Builder builder = new AlertDialog.Builder(v.getContext());
                builder.setView(dialogView);

                EditText comment = dialogView.findViewById(R.id.editTextText);
                EditText report = dialogView.findViewById(R.id.editTextText2);
                Button send = dialogView.findViewById(R.id.sendFeedback);
                Button cancelButton = dialogView.findViewById(R.id.button4);

                AlertDialog dialog = builder.create();
                dialog.show();

                cancelButton.setOnClickListener(view1 -> dialog.dismiss());
                send.setOnClickListener(view1 -> {
                    String userComment = comment.getText().toString().trim();
                    String userReport = report.getText().toString().trim();

                    if (userComment.isEmpty() && userReport.isEmpty()) {
                        Toast.makeText(v.getContext(), "Completa alguna de les preguntes.", Toast.LENGTH_SHORT).show();
                    } else {
                        createFeedback(userId, userComment, userReport);
                        dialog.dismiss();
                    }
                });

            }
        });

        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                auth.signOut();
                 googleSignInClient.signOut().addOnCompleteListener(getActivity(), new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        Intent intent = new Intent(getActivity(), MainActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);

                        getActivity().finish();
                    }
                });
            }
        });

        return v;
    }
    private void createFeedback(String userId, String comment, String report){
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        Map<String, Object> feedbackData = new HashMap<>();
        feedbackData.put("userId", userId);

        if (!comment.isEmpty()) {
            feedbackData.put("comment", comment);
        }
        if (!report.isEmpty()) {
            feedbackData.put("report", report);
        }

        db.collection("feedback")
                .add(feedbackData)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        Toast.makeText(getContext(), "Feedback rebut", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Error" + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

}

