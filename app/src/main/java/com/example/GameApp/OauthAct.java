package com.example.GameApp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
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
import com.example.GameApp.FragFolder.FragAjust;
import com.example.GameApp.FragFolder.FragFav;
import com.example.GameApp.FragFolder.FragForum;
import com.example.GameApp.FragFolder.FragHome;
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
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;


import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class OauthAct extends AppCompatActivity {

    private GoogleSignInClient mGoogleSignInClient;
    private List<Cover> coverList = new ArrayList<>();

    private BottomNavigationView bottomNavigationView;
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
        db = FirebaseFirestore.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();

        handleHome();

        fragments = new Fragment[4];

        fragments[0] = new FragHome();
        fragments[1] = new FragFav();
        fragments[2] = new FragForum();
        fragments[3] = new FragAjust();
        setContentView(R.layout.activity_oauth);

        bottomNavigationView = findViewById(R.id.menuNav);

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

                        crearConversa(usuari, conversationsAdapter);
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
                            Toast.makeText(OauthAct.this, "Introdueix un nom per cercar.", Toast.LENGTH_SHORT).show();
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
                        if(noGames){
                            openFragment(new BannedFragment());
                        }
                        else{
                            openFragment(new FragHome());
                        }
                    }
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
                                Toast.makeText(OauthAct.this, "No s'han trobat usuaris.", Toast.LENGTH_SHORT).show();
                            }
                            // Actualizar el adaptador con la lista (vacía o con resultados)
                            userAdapter.updateList(llistaUsuaris);
                        } else {
                            Log.d(TAG, "Error obtenint usuaris: ", task.getException());
                            // En caso de error, también limpiamos la lista y mostramos un mensaje
                            userAdapter.updateList(llistaUsuaris);
                            Toast.makeText(OauthAct.this, "Error en cercar usuaris.", Toast.LENGTH_SHORT).show();
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
    private void crearConversa(User usuariReceptor, final ConversationAdapter conversationsAdapter) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        List<String> participants = new ArrayList<>();
        participants.add(currentUserId);
        participants.add(usuariReceptor.getUid());

        // Ordenar la lista para tener un orden consistente
        Collections.sort(participants);

        // Crear un identificador único para la conversación basado en los IDs de los participantes
        String conversationId = participants.get(0) + "_" + participants.get(1);

        // Comprobar si la conversación ya existe
        db.collection("messages").document(conversationId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        // La conversación ya existe
                        Toast.makeText(this, "La conversa ja existeix.", Toast.LENGTH_SHORT).show();
                    } else {
                        // Crear nueva conversación
                        Conversation novaConversa = new Conversation();
                        novaConversa.setContent("");
                        novaConversa.setIsRead(false);
                        novaConversa.setUserIdSender(currentUserId);
                        novaConversa.setUserIdReceiver(usuariReceptor.getUid());
                        novaConversa.setReceiverName(usuariReceptor.getName());
                        novaConversa.setParticipants(participants);

                        db.collection("messages").document(conversationId)
                                .set(novaConversa)
                                .addOnSuccessListener(aVoid -> {
                                    Log.d(TAG, "Conversa afegida: " + conversationId);
                                    carregarConverses(conversationsAdapter);
                                })
                                .addOnFailureListener(e -> {
                                    Log.w(TAG, "Error afegint conversa", e);
                                });
                    }
                })
                .addOnFailureListener(e -> {
                    Log.w(TAG, "Error comprobando existencia de la conversación", e);
                });
    }






}
