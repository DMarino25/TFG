package com.example.GameApp.FragFolder;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.GameApp.R;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link FragForum#newInstance} factory method to
 * create an instance of this fragment.
 */
public class FragForum extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER

    public FragForum() {
        // Required empty public constructor
    }


    // TODO: Rename and change types and number of parameters

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_frag_forum, container, false);
        return v;
    }
}