package com.example.GameApp.FragFolder;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import com.example.GameApp.ClassObjectes.Forum;
import com.example.GameApp.ForumAdapter;
import com.example.GameApp.R;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link FragForum#newInstance} factory method to
 * create an instance of this fragment.
 */
public class FragForum extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private RecyclerView recyclerView;
    private ForumAdapter forumAdapter;
    private List<Forum> forumList;
    private FirebaseFirestore db;

    public FragForum() {
        // Required empty public constructor
    }


    // TODO: Rename and change types and number of parameters

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_frag_forum, container, false);

        recyclerView = view.findViewById(R.id.recyclerViewForums);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        forumList = new ArrayList<>();
        forumAdapter = new ForumAdapter(forumList, forum -> {
            // Navigate on forum click
            Intent intent = new Intent(getActivity(), ForumDetailsActivity.class);

            intent.putExtra("forumId", forum.getId());
            // Pasar los detalles del foro a la nueva actividad
            intent.putExtra("forumTitle", forum.getTitle());
            intent.putExtra("forumDescription", forum.getDescription());
            intent.putExtra("userName", forum.getUserName());
            intent.putExtra("userProfilePhoto", forum.getUserProfilePhoto());
            intent.putExtra("lastModifiedDate", forum.getFormattedDate());

            startActivity(intent);
        });

        recyclerView.setAdapter(forumAdapter);

        db = FirebaseFirestore.getInstance();
        loadForumsFromFirestore();

        return view;
    }

    private void loadForumsFromFirestore() {
        // SELECT * FROM forums;
        db.collection("forums")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Log.d("FragForum", "Data fetch successful");

                        // Clear list so we don't have duplicates
                        forumList.clear();

                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Forum forum = document.toObject(Forum.class);
                            Log.d("FragForum", "Forum fetched: " + forum.toString());

                            // Get userId from the forum so we can search for more information in users Collection
                            String userId = forum.getUserId();

                            // Set formattedDate into forum
                            Timestamp lastModifiedDate = forum.getLastModifiedDate();
                            if (lastModifiedDate != null) {
                                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                                String formattedDate = sdf.format(lastModifiedDate.toDate());
                                Log.d("FragForum", "Formatted Date: " + formattedDate);
                                forum.setFormattedDate(formattedDate);
                            }

                            // Query "users" collection to obtain more data from the userId
                            db.collection("users")
                                    .whereEqualTo("userId", userId)
                                    .get()
                                    .addOnCompleteListener(userTask -> {
                                        if (userTask.isSuccessful()) {
                                            if (!userTask.getResult().isEmpty()) {
                                                DocumentSnapshot userDocument = userTask.getResult().getDocuments().get(0);

                                                // Get userName and userProfilePhoto
                                                String userName = userDocument.getString("userName");
                                                String userProfilePhoto = userDocument.getString("userProfilePhoto");

                                                // Setters to our forum
                                                forum.setUserName(userName);
                                                forum.setUserProfilePhoto(userProfilePhoto);
                                                forum.setId(document.getId());

                                                forumList.add(forum);

                                                Log.d("FragForum", "User data added: " + userName);
                                                Log.d("FragForum", "User Profile Photo added: " + userProfilePhoto);

                                                forumAdapter.notifyDataSetChanged();
                                            } else {
                                                Log.d("FragForum", "No user document found for userId: " + userId);
                                            }
                                        } else {
                                            Log.e("FragForum", "Error fetching user document", userTask.getException());
                                        }
                                    });
                        }
                    } else {
                        Log.e("FragForum", "Error fetching forums", task.getException());
                    }
                });
    }
}