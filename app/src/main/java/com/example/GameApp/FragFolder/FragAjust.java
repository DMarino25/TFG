package com.example.GameApp.FragFolder;

import static java.lang.Float.parseFloat;

import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.GameApp.ImgurApiClient;
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
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import androidx.activity.result.contract.ActivityResultContracts;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public class FragAjust extends Fragment {
    private ActivityResultLauncher<Intent> pickImageLauncher;
    private Uri selectedImageUri;
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

        // Initialize the ActivityResultLauncher
        pickImageLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult result) {
                        if (result.getResultCode() == getActivity().RESULT_OK && result.getData() != null) {
                            selectedImageUri = result.getData().getData();

                            // Display the selected image in the ImageView
                            ImageView ProfilePicture = v.findViewById(R.id.ProfilePicture);
                            Glide.with(FragAjust.this)
                                    .load(selectedImageUri)
                                    .circleCrop()
                                    .into(ProfilePicture);

                            // Upload the image to Imgur
                            uploadImageToImgur();
                        }
                    }
                });

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
        EditText UserName = v.findViewById(R.id.UserName);
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

        // Handle profile picture selection
        ProfilePicture.setOnClickListener(view -> {
            Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            intent.setType("image/*");
            pickImageLauncher.launch(intent); // Launch the intent using the new launcher
        });

        feedback.setOnClickListener(v1 -> {
            View dialogView = LayoutInflater.from(v1.getContext()).inflate(R.layout.dialog_feedback, null);
            AlertDialog.Builder builder = new AlertDialog.Builder(v1.getContext());
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
                    Toast.makeText(v1.getContext(), "Completa alguna de les preguntes.", Toast.LENGTH_SHORT).show();
                } else {
                    createFeedback(userId, userComment, userReport);
                    dialog.dismiss();
                }
            });

        });

        logout.setOnClickListener(v1 -> {

            auth.signOut();
            googleSignInClient.signOut().addOnCompleteListener(getActivity(), task -> {
                Intent intent = new Intent(getActivity(), MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);

                getActivity().finish();
            });
        });

        return v;
    }

    private void uploadImageToImgur() {
        if (selectedImageUri != null) {
            try {
                InputStream imageStream = getContext().getContentResolver().openInputStream(selectedImageUri);
                ImgurApiClient.uploadImageToImgur(imageStream, new ImgurApiClient.UploadCallback() {
                    @Override
                    public void onUploadSuccess(String imgurUrl) {
                        Log.d("Imgur", "Imgur URL: " + imgurUrl);
                        uploadImageToFirebase(imgurUrl);
                    }

                    @Override
                    public void onUploadFailure(String errorMessage) {
                        Toast.makeText(getContext(), "Failed to upload image: " + errorMessage, Toast.LENGTH_SHORT).show();
                    }
                });
            } catch (IOException e) {
                Toast.makeText(getContext(), "Failed to upload image to Imgur: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void uploadImageToFirebase(String URL) {
        if (URL != null) {
            // Extract the Imgur image URL (you should parse it from the response, as needed)

            // Save it in Firestore or Firebase Realtime Database
            currentUser = FirebaseAuth.getInstance().getCurrentUser();
            String userId = currentUser.getUid();

            firestore.collection("users").document(userId)
                    .update("photoUrl", URL)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(getContext(), "Profile picture updated successfully", Toast.LENGTH_SHORT).show();
                    })
                    .addOnFailureListener(e ->
                            Toast.makeText(getContext(), "Error updating profile picture: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                    );
        }
    }
    
    private void createFeedback(String userId, String comment, String report) {
        Map<String, Object> feedback = new HashMap<>();
        feedback.put("userId", userId);
        if (!comment.isEmpty()) {
            feedback.put("comment", comment);
        }
        if (!report.isEmpty()) {
            feedback.put("report", report);
        }

        firestore.collection("feedbacks").add(feedback)
                .addOnSuccessListener(documentReference ->
                        Toast.makeText(getActivity(), "Feedback enviat", Toast.LENGTH_SHORT).show()
                )
                .addOnFailureListener(e ->
                        Toast.makeText(getActivity(), "Error al enviar el feedback: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                );
    }
}

