/**
 * @filename StartActivity
 * @description  app启动界面
 * @author ZhouYan
 * */

package com.example.catedu;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.WindowManager;

import java.util.Objects;


public class StartActivity extends AppCompatActivity {

    /**
     * StartActivity 创建时的操作
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN); // 隐藏状态栏
        Objects.requireNonNull(getSupportActionBar()).hide(); // 隐藏标题栏
        setContentView(R.layout.activity_start);

        // 开一个子线程来启动 MainActivity
        // 以此做到边显示启动界面，边在 MainActivity 中发送网络请求获取数据
        // 创建子线程
        Thread myThread = new Thread(() -> {
            try{
                Thread.sleep(1000);
                Intent it = new Intent(getApplicationContext(), MainActivity.class); // 启动MainActivity
                startActivity(it);
                finish(); // 关闭当前活动
            } catch (Exception e){
                e.printStackTrace();
            }
        });
        myThread.start();//启动线程
    }
}