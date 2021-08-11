package com.example.catedu;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.annotation.SuppressLint;
import android.os.Bundle;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {
    private Fragment[] fragments;
    private int last_fragment = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
    }

    private void initView () {
        FragmentHome fragment_home = new FragmentHome();
        FragmentQuestion fragment_search = new FragmentQuestion();
        FragmentMine fragment_mine = new FragmentMine();
        fragments = new Fragment[]{fragment_home, fragment_search, fragment_mine};

        getSupportFragmentManager().beginTransaction().replace(R.id.nav_host_fragment, fragment_home).show(fragment_home).commit();
        BottomNavigationView nav_view = findViewById(R.id.nav_view);
        nav_view.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
    }

    @SuppressLint("NonConstantResourceId")
    private final BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = item -> {
        switch (item.getItemId()) {
            case R.id.navigation_home:
                if (last_fragment != 0) {
                    switchFragment(last_fragment, 0);
                    last_fragment = 0;
                }
                return true;
            case R.id.navigation_question:
                if (last_fragment != 1) {
                    switchFragment(last_fragment, 1);
                    last_fragment = 1;
                }
                return true;
            case R.id.navigation_mine:
                if (last_fragment != 2) {
                    switchFragment(last_fragment, 2);
                    last_fragment = 2;
                }
                return true;
            default:
                break;
        }
        return false;
    };

    private void switchFragment (int last, int index) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.hide(fragments[last]);
        if (!fragments[index].isAdded())
            transaction.add(R.id.nav_host_fragment, fragments[index]);
        transaction.show(fragments[index]).commitAllowingStateLoss();
    }

    @Override
    protected void onStart() {
        super.onStart();
    }
}