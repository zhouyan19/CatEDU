/**
 * @filename FragmentQuestion
 * @description  问答和搜索组件 (3)
 * @author ZhouYan
 * */

package com.example.catedu;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

public class FragmentQuestion extends Fragment {

    /**
     * FragmentQuestion 创建时的操作
     */
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_question, container, false);
    }
}