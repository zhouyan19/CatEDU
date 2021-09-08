package com.example.catedu;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;


/**
 * A simple {@link Fragment} subclass.
 * Use the {@link FragmentWordCloud#newInstance} factory method to
 * create an instance of this fragment.
 */
public class FragmentWordCloud extends Fragment {
    ImageView imageView;


    public FragmentWordCloud() {
        // Required empty public constructor
    }


    // TODO: Rename and change types and number of parameters
    public static FragmentWordCloud newInstance(String param1, String param2) {
        FragmentWordCloud fragment = new FragmentWordCloud();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_word_cloud, container, false);
    }

    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        imageView = view.findViewById(R.id.rv_subjects);

    }

}