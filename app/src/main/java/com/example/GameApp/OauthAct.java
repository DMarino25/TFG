package com.example.GameApp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.GameApp.ClassObjectes.Conversation;
import com.example.GameApp.ClassObjectes.Cover;
import com.example.GameApp.ClassObjectes.User;
import com.example.GameApp.FragFolder.BanFavFragment;
import com.example.GameApp.FragFolder.BanForFragment;
import com.example.GameApp.FragFolder.BannedFragment;
import com.example.GameApp.FragFolder.ForumDetailsActivity;
import com.example.GameApp.FragFolder.FragAjust;
import com.example.GameApp.FragFolder.FragFav;
import com.example.GameApp.FragFolder.FragForum;
import com.example.GameApp.FragFolder.FragHome;
import com.example.GameApp.main.MainActivity;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.navigation.NavigationBarView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;


import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class OauthAct extends AppCompatActivity {

    private GoogleSignInClient mGoogleSignInClient;
    private List<Cover> coverList = new ArrayList<>();

    private BottomNavigationView bottomNavigationView;


    private ListenerRegistration banListener;
    private ImageButton missatge;
    private Fragment fragments[];
    RecyclerView recyclerView;
    private CoverAdapter coverAdapter;
    private static final String TAG = "OauthAct";
    private FirebaseAuth firebaseAuth;

    private FirebaseFirestore db;

    private Boolean noGames = false;
    private Boolean noFav = false;
    private Boolean noFor = false;


    private TextView gameName;

    private EditText cercaText;


    RecyclerView lusers;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Fragmentos
        setContentView(R.layout.activity_oauth);

        db = FirebaseFirestore.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();

        bottomNavigationView = findViewById(R.id.menuNav);
        bottomNavigationView.setEnabled(false);
        handleHome();

        fragments = new Fragment[4];

        fragments[0] = new FragHome();
        fragments[1] = new FragFav();
        fragments[2] = new FragForum();
        fragments[3] = new FragAjust();


        missatge = findViewById(R.id.missatges);
        missatge.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                View dialogView = LayoutInflater.from(v.getContext()).inflate(R.layout.dialog_message, null);

                EditText searchUserEditText = dialogView.findViewById(R.id.searchUserEditText);
                RecyclerView usersRecyclerView = dialogView.findViewById(R.id.usersRecyclerView);
                RecyclerView conversationsRecyclerView = dialogView.findViewById(R.id.conversasRecyclerView);
                usersRecyclerView.setNestedScrollingEnabled(false);
                conversationsRecyclerView.setNestedScrollingEnabled(false);
                ImageButton cercaUsers = dialogView.findViewById(R.id.searchButton);

                LinearLayout contenidorResultats = dialogView.findViewById(R.id.contenedorResultados);
                contenidorResultats.setVisibility(View.GONE);

                // Configurar RecyclerView de conversaciones existentes
                conversationsRecyclerView.setLayoutManager(new LinearLayoutManager(v.getContext()));
                ArrayList<Conversation> llistaConverses = new ArrayList<>();
                ConversationAdapter conversationsAdapter = new ConversationAdapter(v.getContext(), llistaConverses);
                conversationsRecyclerView.setAdapter(conversationsAdapter);

                carregarConverses(conversationsAdapter);

                usersRecyclerView.setLayoutManager(new LinearLayoutManager(v.getContext()));
                ArrayList<User> llistaUsuaris = new ArrayList<>();
                UserAdapter userAdapter = new UserAdapter(v.getContext(), llistaUsuaris, new UserAdapter.OnUserClickListener() {
                    @Override
                    public void onConversaClick(User usuari) {
                        createConversa(usuari, conversationsAdapter);
                    }
                });
                usersRecyclerView.setAdapter(userAdapter);

                cercaUsers.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        String textCerca = searchUserEditText.getText().toString().trim();
                        if (!textCerca.isEmpty()) {
                            contenidorResultats.setVisibility(View.VISIBLE);
                            cercaUsuaris(textCerca, userAdapter);
                        } else {
                            contenidorResultats.setVisibility(View.GONE);
                            llistaUsuaris.clear();
                            userAdapter.notifyDataSetChanged();
                            Toast.makeText(OauthAct.this, getString(R.string.OauthActSearch), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
                searchUserEditText.setOnEditorActionListener((View,actionId,event) ->{
                    if(actionId == EditorInfo.IME_ACTION_SEARCH || actionId == EditorInfo.IME_ACTION_DONE){
                        cercaUsers.performClick();
                        return true;
                    }
                    return false;
                });
                BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(v.getRootView().getContext());
                bottomSheetDialog.setContentView(dialogView);
                bottomSheetDialog.show();


            }
        });


        bottomNavigationView.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                int id = menuItem.getItemId();
                if (id == R.id.home) {
                    handleHome();
                } else if (id == R.id.favorits) {
                    handleFav();
                   // openFragment(fragments[1]);
                } else if (id == R.id.forums) {
                    handleFor();
                    //openFragment(fragments[2]);
                } else if (id == R.id.profile) {
                    openFragment(fragments[3]);
                }
                return true;
            }
        });


    }
    @Override
    protected void onStart() {
        super.onStart();

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            String email = currentUser.getEmail();

            banListener = FirebaseFirestore.getInstance()
                    .collection("bannedUsers")
                    .whereEqualTo("email", email)
                    .addSnapshotListener((querySnapshot, e) -> {
                        if (e != null) {
                            return;
                        }
                        if (querySnapshot != null && !querySnapshot.isEmpty()) {
                            FirebaseAuth.getInstance().signOut();
                            Toast.makeText(OauthAct.this,"Usuari bloquejat",Toast.LENGTH_LONG).show();

                            Intent intent = new Intent(OauthAct.this, MainActivity.class);
                            startActivity(intent);
                            finish();
                        }
                    });
        }
    }
    @Override
    protected void onStop() {
        super.onStop();
        if (banListener != null) {
            banListener.remove();
            banListener = null;
        }
    }
    private void handleHome(){
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        if (currentUser != null){
            String userId= firebaseAuth.getUid();

            DocumentReference documentReference = db.collection("users").document(userId);
            documentReference.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                @Override
                public void onSuccess(DocumentSnapshot documentSnapshot) {
                    if (documentSnapshot.exists()){
                         noGames = documentSnapshot.getBoolean("noGames");
                         noFav = documentSnapshot.getBoolean("noFav");
                         noFor = documentSnapshot.getBoolean("noFor");

                        Log.d(TAG, "Valor de noGames obtenido: " + noGames);
                        if (bottomNavigationView.getSelectedItemId() == R.id.home) {
                            if(noGames){
                                openFragment(new BannedFragment());
                            }
                            else{
                                openFragment(new FragHome());
                            }
                        }
                    }
                    bottomNavigationView.setEnabled(true);
                }
            });
        }
    }

    private void handleFav(){
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        if (currentUser != null){
            String userId= firebaseAuth.getUid();

            DocumentReference documentReference = db.collection("users").document(userId);
            documentReference.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                @Override
                public void onSuccess(DocumentSnapshot documentSnapshot) {
                    if (documentSnapshot.exists()){

                        noFav = documentSnapshot.getBoolean("noFav");

                        Log.d(TAG, "Valor de noGames obtenido: " + noGames);
                        if(noFav){
                            openFragment(new BanFavFragment());
                        }
                        else{
                            openFragment(new FragFav());
                        }
                    }
                }
            });
        }

    }
    private void handleFor(){
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        if (currentUser != null){
            String userId= firebaseAuth.getUid();

            DocumentReference documentReference = db.collection("users").document(userId);
            documentReference.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                @Override
                public void onSuccess(DocumentSnapshot documentSnapshot) {
                    if (documentSnapshot.exists()){
                        noFor = documentSnapshot.getBoolean("noFor");
                        Log.d(TAG, "Valor de noGames obtenido: " + noGames);
                        if(noFor){
                            openFragment(new BanForFragment());
                        }
                        else{
                            openFragment(new FragForum());
                        }
                    }
                }
            });
        }

    }
    private void openFragment(Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.fragmentContainerView2, fragment);
        fragmentTransaction.commit();
    }

    private void cercaUsuaris(String textCerca, final UserAdapter userAdapter) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Normalizar el texto de búsqueda
        String textCercaNormalitzat = Normalizer.normalize(textCerca, Normalizer.Form.NFD)
                .replaceAll("[\\p{InCombiningDiacriticalMarks}]", "")
                .toLowerCase();

        db.collection("users")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        ArrayList<User> llistaUsuaris = new ArrayList<>();
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                User usuari = document.toObject(User.class);

                                String nomUsuari = usuari.getName();
                                if (nomUsuari != null && !nomUsuari.isEmpty()) {
                                    String nomUsuariNormalitzat = Normalizer.normalize(nomUsuari, Normalizer.Form.NFD)
                                            .replaceAll("[\\p{InCombiningDiacriticalMarks}]", "")
                                            .toLowerCase();

                                    if (nomUsuariNormalitzat.contains(textCercaNormalitzat)) {
                                        llistaUsuaris.add(usuari);
                                    }
                                } else {
                                    Log.d(TAG, "Usuari sense nom o nom null: " + usuari.getUid());
                                }
                            }
                            if (llistaUsuaris.isEmpty()) {
                                // No se encontraron usuarios que coincidan con la búsqueda
                                Toast.makeText(OauthAct.this, getString(R.string.OauthActEmpty), Toast.LENGTH_SHORT).show();
                            }
                            // Actualizar el adaptador con la lista (vacía o con resultados)
                            userAdapter.updateList(llistaUsuaris);
                        } else {
                            Log.d(TAG, "Error obtenint usuaris: ", task.getException());
                            // En caso de error, también limpiamos la lista y mostramos un mensaje
                            userAdapter.updateList(llistaUsuaris);
                            Toast.makeText(OauthAct.this, getString(R.string.OauthActError), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }
    private void carregarConverses(final ConversationAdapter conversationsAdapter) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        db.collection("messages")
                .whereArrayContains("participants", currentUserId)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            ArrayList<Conversation> llistaConverses = new ArrayList<>();
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Conversation conversa = document.toObject(Conversation.class);
                                conversa.setConversationId(document.getId());
                                llistaConverses.add(conversa);
                            }
                            conversationsAdapter.updateList(llistaConverses);
                        } else {
                            Log.d(TAG, "Error obtenint converses: ", task.getException());
                        }
                    }
                });
    }
    private void createConversa(User usuariReceptor, final ConversationAdapter conversationsAdapter) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        String currentUserId = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();
        List<String> participants = new ArrayList<>();
        participants.add(currentUserId);
        participants.add(usuariReceptor.getUid());
        Collections.sort(participants);

        String conversationId = participants.get(0) + "_" + participants.get(1);
        // Check if chat exists
        db.collection("messages").document(conversationId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Toast.makeText(this, getString(R.string.OauthActExists), Toast.LENGTH_SHORT).show();
                    } else {
                        // Create new chat
                        Map<String, Object> newChat = new HashMap<>();
                        newChat.put("participants", participants);
                        db.collection("messages").document(conversationId)
                                .set(newChat)
                                .addOnSuccessListener(aVoid -> {
                                    Log.d(TAG, "New chat added: " + conversationId);
                                    carregarConverses(conversationsAdapter);
                                    Intent intent = new Intent(this, chatActivity.class);
                                    intent.putExtra("conversationId", conversationId);
                                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                    startActivity(intent);
                                })
                                .addOnFailureListener(e -> {
                                    Log.w(TAG, "Error adding the new chat", e);
                                });
                    }
                })
                .addOnFailureListener(e -> {
                    Log.w(TAG, "Error checking if chat exists", e);
                });
    }



}
