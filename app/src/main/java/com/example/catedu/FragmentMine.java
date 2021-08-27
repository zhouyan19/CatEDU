/**
 * @filename FragmentMine
 * @description  我的组件 (3)
 * @author ZhouYan
 * */

package com.example.catedu;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class FragmentMine extends Fragment {

    /**
     * FragmentMine 创建时的操作
     */
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_mine, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {

        int refIds[] ={R.id.selfie,R.id.login_textView};
        for (int id : refIds) {
            view.findViewById(id).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    // your code here.
                    Intent intent=new Intent(getActivity(), LoginActivity.class);
                    startActivity(intent);
                }
            });
        }
        super.onViewCreated(view, savedInstanceState);
    }
}
