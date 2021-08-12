/**
 * @filename FragmentHome
 * @description  主页组件 (1)
 * @author ZhouYan
 * */

package com.example.catedu;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.catedu.data.DataLoader;

import javax.sql.DataSource;

public class FragmentHome extends Fragment {

    private DataLoader dataLoader;

    /**
     * 构造函数，初始化 DataLoader
     */
    public FragmentHome() {
        dataLoader = new DataLoader();
    }

    /**
     * FragmentHome 创建时的操作
     */
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // 绑定 layout
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    /**
     * FragmentHome 启动时运行
     */
    @Override
    public void onStart() {
        super.onStart();
        try {
            dataLoader.getOriginalData();
        } catch (InterruptedException e) {
            Log.e("FragmentHome", "Get Data Failed");
            e.printStackTrace();
        }
    }
}
