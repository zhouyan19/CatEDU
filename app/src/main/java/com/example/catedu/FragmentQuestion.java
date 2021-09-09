/**
 * @filename FragmentQuestion
 * @description  问答和搜索组件 (3)
 * @author ZhouYan
 * */

package com.example.catedu;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

public class FragmentQuestion extends Fragment {
    CardView cardRetrieval, cardLink, cardQa, cardCamera;
    /**
     * FragmentQuestion 创建时的操作
     */
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_question, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        cardRetrieval = view.findViewById(R.id.card_retrieval);
        cardLink = view.findViewById(R.id.card_link);
        cardQa = view.findViewById(R.id.card_qa);
        cardCamera = view.findViewById(R.id.card_camera);

        cardRetrieval.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i("i","Retrieval btn clicked!");
                MainActivity.fragments.add(new FragmentQuesRetrieval());
                forwardSwitchFragment();
            }
        });
        cardLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i("i","Link btn clicked!");
                MainActivity.fragments.add(new FragmentQuesLink());
                forwardSwitchFragment();
            }
        });
        cardQa.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i("i","Qa btn clicked!");
                MainActivity.fragments.add(new FragmentQuesQa());
                forwardSwitchFragment();
            }
        });

        cardCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i("i","Camera btn clicked!");
                MainActivity.fragments.add(new FragmentCamera());
                forwardSwitchFragment();
            }
        });

    }

    protected void forwardSwitchFragment() {
        int from = MainActivity.last_fragment, to = MainActivity.fragments.size() - 1;
        FragmentTransaction transaction = requireActivity().getSupportFragmentManager().beginTransaction();
        transaction.hide(MainActivity.fragments.get(from));
        if (!MainActivity.fragments.get(to).isAdded())
            transaction.add(R.id.nav_host_fragment, MainActivity.fragments.get(to));
        transaction.show(MainActivity.fragments.get(to)).commitAllowingStateLoss();
        MainActivity.last_fragment = to; // 更新
    }

}

