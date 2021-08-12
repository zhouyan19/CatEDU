/**
 * @filename FragmentHome
 * @description  主页组件 (1)
 * @author ZhouYan
 * */

package com.example.catedu;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

public class FragmentHome extends Fragment {

    /**
     * FragmentHome 创建时的操作
     * @param LayoutInflater inflater
     * @param ViewGroup container
     * @param Bundle savedInstanceState
     */
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // 绑定 layout
        View root = inflater.inflate(R.layout.fragment_home, container, false);
        return root;
    }
}
