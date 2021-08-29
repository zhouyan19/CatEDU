package com.example.catedu;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.Spinner;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.androidkun.xtablayout.XTabLayout;
import com.github.ybq.android.spinkit.SpinKitView;

import java.util.Map;
import java.util.Vector;

public class FragmentInstance extends Fragment {
    public static String uri; // 实体uri
    public static String name; // 实体名称
    public static String course; // 学科名称
    public static Vector<Fragment> fragments;
    public static int last_fragment = 0;

    FragmentInsDetail fragment_ins_detail;
    FragmentInsQues fragment_ins_ques;
    FragmentInsRelated fragment_ins_related;

    ImageButton back_home;
    public static SpinKitView skv;
    XTabLayout detail_tabLayout;

    public FragmentInstance (String _u, String _n, String _c) {
        Log.e("FragmentInstance", "New!");
        uri = _u;
        name = _n;
        course = _c;
    }

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // 绑定 layout
        return inflater.inflate(R.layout.fragment_instance, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        skv = view.findViewById(R.id.spin_kit);
        skv.setVisibility(View.VISIBLE);

        detail_tabLayout = view.findViewById(R.id.detail_tab_layout);

        back_home = view.findViewById(R.id.detail_back_home);
        back_home.setOnClickListener(v -> {
            try {
                backSwitchFragment();
            } catch (Throwable throwable) {
                throwable.printStackTrace();
            }
        });

        fragment_ins_detail = new FragmentInsDetail(uri, name, course);
        fragment_ins_ques = new FragmentInsQues(name);
        fragment_ins_related = new FragmentInsRelated();

        fragments = new Vector<>();

        fragments.add(fragment_ins_detail);
        detail_tabLayout.addTab(detail_tabLayout.newTab().setText("详情"));

        fragments.add(fragment_ins_ques);
        detail_tabLayout.addTab(detail_tabLayout.newTab().setText("试题"));

        fragments.add(fragment_ins_related);
        detail_tabLayout.addTab(detail_tabLayout.newTab().setText("相关"));

        detail_tabLayout.setOnTabSelectedListener(new XTabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(XTabLayout.Tab tab) {
                int id = tab.getPosition();
                switchFragment(last_fragment, id);
                Log.e("Tab", (String) tab.getText());
            }

            @Override
            public void onTabUnselected(XTabLayout.Tab tab) {}

            @Override
            public void onTabReselected(XTabLayout.Tab tab) {}
        });

        getChildFragmentManager()
                .beginTransaction()
                .replace(R.id.detail_fragment_container, fragment_ins_detail)
                .show(fragment_ins_detail).commit();
    }

    private void switchFragment (int last, int index) {
        FragmentTransaction transaction = getChildFragmentManager().beginTransaction();
        transaction.hide(fragments.get(last));
        if (!fragments.get(index).isAdded())
            transaction.add(R.id.detail_fragment_container, fragments.get(index));
        transaction.show(fragments.get(index)).commitAllowingStateLoss();
        last_fragment = index;
    }

    protected void backSwitchFragment() throws Throwable {
        int from = MainActivity.last_fragment, to;
        FragmentTransaction transaction = requireActivity().getSupportFragmentManager().beginTransaction();
        transaction.hide(MainActivity.fragments.get(from));
        if (MainActivity.last_fragment == 3) { //次级页面
            to = MainActivity.major_fragment;
        } else { //多级页面
            to = MainActivity.last_fragment - 1;
        }
        if (!MainActivity.fragments.get(to).isAdded())
            transaction.add(R.id.nav_host_fragment, MainActivity.fragments.get(to));
        transaction.show(MainActivity.fragments.get(to)).commitAllowingStateLoss();
        MainActivity.last_fragment = to; //更新
        MainActivity.fragments.removeElementAt(from); //删多余的页面
        finalize();
    }
}
