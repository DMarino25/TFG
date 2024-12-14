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
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.GameApp.ClassObjectes.FavoriteGame;
import com.example.GameApp.CoverUtils;
import com.example.GameApp.FavAdapter;
import com.example.GameApp.ImgurApiClient;
import com.example.GameApp.R;
import com.example.GameApp.main.MainActivity;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
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
    private String description = null;

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
        MaterialButton logout = v.findViewById(R.id.logout);
        LinearLayout selectFG = v.findViewById(R.id.selectGame);
        MaterialButton  delete = v.findViewById(R.id.deleteAccount);
        EditText UserName = v.findViewById(R.id.UserName);
        EditText description = v.findViewById(R.id.description);
        TextView fgGame = v.findViewById(R.id.hintFG);
        ImageView ProfilePicture = v.findViewById(R.id.ProfilePicture);

        // New buttons for tick and cross
        ImageView tickButton = v.findViewById(R.id.tickButton);
        ImageView crossButton = v.findViewById(R.id.crossButton);
        ImageView tickButton2 = v.findViewById(R.id.tickButton2);
        ImageView crossButton2 = v.findViewById(R.id.crossButton2);
        ImageView gameFavImg = v.findViewById(R.id.gameFavImg);

        // Initially hide the tick and cross buttons
        tickButton.setVisibility(View.GONE);
        crossButton.setVisibility(View.GONE);
        tickButton2.setVisibility(View.GONE);
        crossButton2.setVisibility(View.GONE);

        firestore.collection("users").document(userId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        currentUsername = documentSnapshot.getString("name");
                        String currentDescription = documentSnapshot.getString("description");
                        String currentGVimage = documentSnapshot.getString("gameFavImg");
                        String currentMPgame = documentSnapshot.getString("gameFav");
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
                        fgGame.setText(currentMPgame);
                        if (currentDescription != null){
                            description.setText(currentDescription);
                        }
                        if (currentGVimage != null){
                            String imageId = CoverUtils.extractImageId(currentGVimage);
                            String imageUrl = CoverUtils.constructImageUrl(imageId, "t_1080p");
                            Glide.with(v.getContext())
                                    .load(imageUrl)
                                    .placeholder(R.mipmap.ic_launcher)
                                    .into(gameFavImg);
                        }
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
        description.setOnFocusChangeListener((view, hasFocus) -> {
            if (hasFocus) {
                description.setEnabled(true);
                tickButton2.setVisibility(View.VISIBLE);
                crossButton2.setVisibility(View.VISIBLE);
            } else {
                tickButton2.setVisibility(View.GONE);
                crossButton2.setVisibility(View.GONE);
            }
        });

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

        tickButton2.setOnClickListener(view -> {
            String newDescription = description.getText().toString().trim();
            if (newDescription.isEmpty()) {
                Toast.makeText(v.getContext(), "No has introduit text", Toast.LENGTH_SHORT).show();
            } else {
                firestore.collection("users").document(userId)
                        .update("description", newDescription)
                        .addOnSuccessListener(aVoid -> {
                            Toast.makeText(v.getContext(), "Descripció actualitzada correctament", Toast.LENGTH_SHORT).show();

                            tickButton2.setVisibility(View.GONE);
                            crossButton2.setVisibility(View.GONE);
                        })
                        .addOnFailureListener(e ->
                                Toast.makeText(v.getContext(), "Error al actualitzar la descripció: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                        );
            }
            description.clearFocus();
        });

        crossButton2.setOnClickListener(view -> {
            description.clearFocus();

            tickButton2.setVisibility(View.GONE);
            crossButton2.setVisibility(View.GONE);
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

        delete.setOnClickListener(v1 -> {
            // Show a confirmation dialog to prevent accidental deletions
            new AlertDialog.Builder(v1.getContext())
                    .setTitle("Confirmació d'eliminació")
                    .setMessage("Estàs segur que vols eliminar el teu compte? Aquesta acció no es pot desfer.")
                    .setPositiveButton("Sí", (dialog, which) -> {
                        // Get the user ID
                        String userIdToDelete = currentUser.getUid();

                        // Delete user data from Firestore
                        firestore.collection("users").document(userIdToDelete).delete()
                                .addOnSuccessListener(aVoid -> {
                                    // Successfully deleted data from Firestore
                                    // Proceed to delete the account from Firebase Authentication
                                    currentUser.delete()
                                            .addOnSuccessListener(aVoid1 -> {
                                                Toast.makeText(v1.getContext(), "El compte s'ha eliminat correctament.", Toast.LENGTH_SHORT).show();

                                                // Log out and redirect to the login page
                                                Intent intent = new Intent(getActivity(), MainActivity.class);
                                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                                startActivity(intent);
                                                getActivity().finish();
                                            })
                                            .addOnFailureListener(e -> {
                                                // Error while deleting the account
                                                Toast.makeText(v1.getContext(), "Error al eliminar el compte: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                            });
                                })
                                .addOnFailureListener(e -> {
                                    // Error while deleting Firestore data
                                    Toast.makeText(v1.getContext(), "Error al eliminar dades del Firestore: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                });
                    })
                    .setNegativeButton("No", null) // Do nothing on "No"
                    .show();
        });

        selectFG.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                View dialogView = LayoutInflater.from(v.getContext()).inflate(R.layout.fragment_frag_fav, null);
                RecyclerView recyclerView = dialogView.findViewById(R.id.favList);
                TextView noFavoritesText = dialogView.findViewById(R.id.no_favorites_text);

                EditText cerca2 = dialogView.findViewById(R.id.cerca2);
                ImageView go2 = dialogView.findViewById(R.id.go2);
                AlertDialog.Builder builder = new AlertDialog.Builder(v.getContext());
                builder.setView(dialogView);
                AlertDialog dialog = builder.create();
                dialog.show();

                ArrayList<FavoriteGame> favoriteGames = new ArrayList<>();
                FavAdapter dialogAdapter = new FavAdapter(v.getContext(), new ArrayList<>(), false, new FavAdapter.OnGameClickListener(){
                    @Override
                    public void onGameClick(FavoriteGame game) {
                        FirebaseFirestore firestore = FirebaseFirestore.getInstance();
                        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

                        if (currentUser != null) {
                            Map<String, Object> updates = new HashMap<>();
                            updates.put("gameFav", game.getTitle());
                            updates.put("gameFavImg", game.getCover_url());
                            firestore.collection("users")
                                    .document(currentUser.getUid())
                                    .update(updates)
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void unused) {
                                            fgGame.setText(game.getTitle());

                                            String imageId = CoverUtils.extractImageId(game.getCover_url());
                                            String imageUrl = CoverUtils.constructImageUrl(imageId, "t_1080p");

                                            Glide.with(v.getContext())
                                                            .load(imageUrl)
                                                            .placeholder(R.mipmap.ic_launcher)
                                                            .into(gameFavImg);

                                            Toast.makeText(v.getContext(), "Joc més jugat seleccionat", Toast.LENGTH_SHORT).show();
                                            dialog.dismiss();
                                        }

                                    })
                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            Toast.makeText(v.getContext(), "Joc més jugat no seleccionat", Toast.LENGTH_SHORT).show();
                                        }
                                    });
                        }
                        else{
                            Toast.makeText(v.getContext(), "Usuari no trobat", Toast.LENGTH_SHORT).show();
                        }
                    }
                });

                recyclerView.setLayoutManager(new LinearLayoutManager(v.getContext()));
                recyclerView.setAdapter(dialogAdapter);

                FirebaseFirestore firestore = FirebaseFirestore.getInstance();
                FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

                if (currentUser != null) {
                    firestore.collection("users").document(currentUser.getUid()).collection("favorits")
                            .get()
                            .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                                @Override
                                public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                                    favoriteGames.clear();
                                    for (DocumentSnapshot document : queryDocumentSnapshots.getDocuments()) {
                                        FavoriteGame game = document.toObject(FavoriteGame.class);
                                        favoriteGames.add(game);
                                    }
                                    if (favoriteGames.isEmpty()) {
                                        recyclerView.setVisibility(View.GONE);
                                        noFavoritesText.setVisibility(View.VISIBLE);
                                    } else {
                                        recyclerView.setVisibility(View.VISIBLE);
                                        noFavoritesText.setVisibility(View.GONE);
                                        dialogAdapter.updateList(favoriteGames);
                                    }
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Toast.makeText(v.getContext(), "Error al carregar los favorits", Toast.LENGTH_SHORT).show();
                                }
                            });
                } else{
                    Toast.makeText(v.getContext(), "Usuari no autenticat", Toast.LENGTH_SHORT).show();
                }

                go2.setOnClickListener(v2 -> {
                    String searchQuery = cerca2.getText().toString().trim();
                    ArrayList<FavoriteGame> filteredList = new ArrayList<>();

                    if (searchQuery.isEmpty()) {
                        // Mostrar todos si no hay búsqueda
                        filteredList.addAll(favoriteGames);
                    } else {
                        for (FavoriteGame game : favoriteGames) {
                            if (game.getTitle() != null && game.getTitle().toLowerCase().contains(searchQuery.toLowerCase())) {
                                filteredList.add(game);
                            }
                        }
                    }
                    dialogAdapter.updateList(filteredList);
                    if (filteredList.isEmpty()) {
                        recyclerView.setVisibility(View.GONE);
                        noFavoritesText.setVisibility(View.VISIBLE);
                    } else {
                        recyclerView.setVisibility(View.VISIBLE);
                        noFavoritesText.setVisibility(View.GONE);
                    }
                });
            }
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

