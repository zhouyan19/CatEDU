/**
 * @filename MainActivity
 * @description  app主活动
 * @author ZhouYan
 * */

package com.example.catedu;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;
import android.view.Window;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import org.scilab.forge.jlatexmath.core.AjLatexMath;

import java.util.Objects;
import java.util.Vector;

import io.github.kbiakov.codeview.classifier.CodeProcessor;

public class MainActivity extends AppCompatActivity {
    public static Vector<Fragment> fragments;
    public static int last_fragment = 0;
    public static int major_fragment = 0;

    /**
     * MainActivity 创建时的操作
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Objects.requireNonNull(getSupportActionBar()).hide();
        AjLatexMath.init(this);
        CodeProcessor.init(this);
        setContentView(R.layout.activity_main);
        initView();
    }


    /**
     * 为 BottomNavigationView 绑定 Fragment
     */
    private void initView () {
        FragmentHome fragment_home = new FragmentHome();
        FragmentQuestion fragment_search = new FragmentQuestion();
        FragmentMine fragment_mine = new FragmentMine();

        fragments = new Vector<>();
        fragments.add(fragment_home);
        fragments.add(fragment_search);
        fragments.add(fragment_mine);

        getSupportFragmentManager().beginTransaction().replace(R.id.nav_host_fragment, fragment_home).show(fragment_home).commit();
        BottomNavigationView nav_view = findViewById(R.id.nav_view);
        nav_view.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
    }


    /**
     * 为 BottomNavigationView 设置选择切换
     */
    @SuppressLint("NonConstantResourceId")
    private final BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener = item -> {
        switch (item.getItemId()) {
            case R.id.navigation_home:
                if (last_fragment != 0) {
                    switchFragment(last_fragment, 0);
                }
                return true;
            case R.id.navigation_question:
                if (last_fragment != 1) {
                    switchFragment(last_fragment, 1);
                }
                return true;
            case R.id.navigation_mine:
                if (last_fragment != 2) {
                    switchFragment(last_fragment, 2);
                }
                return true;
            default:
                break;
        }
        return false;
    };

    /**
     * 切换组件
     * @param last 上一组件的序号
     * @param index 要切换的组件的序号
     */
    private void switchFragment (int last, int index) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.hide(fragments.get(last));
        if (!fragments.get(index).isAdded())
            transaction.add(R.id.nav_host_fragment, fragments.get(index));
        transaction.show(fragments.get(index)).commitAllowingStateLoss();
        for (int i = fragments.size() - 1; i >= 3; i--) {  //删多余的页面
            fragments.removeElementAt(i);
        }
        last_fragment = index;
        major_fragment = index;
    }

}