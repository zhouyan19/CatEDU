/**
 * @filename FragmentMine
 * @description  我的组件 (3)
 * @author ZhouYan
 * */

package com.example.catedu;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

public class FragmentMine extends Fragment {

    /**
     * FragmentMine 创建时的操作
     * @param LayoutInflater inflater
     * @param ViewGroup container
     * @param Bundle savedInstanceState
     */
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_mine, container, false);
        return root;
    }
}
