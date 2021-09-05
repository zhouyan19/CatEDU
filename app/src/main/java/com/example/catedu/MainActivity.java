/**
 * @filename MainActivity
 * @description  app主活动
 * @author ZhouYan
 * */

package com.example.catedu;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentTransaction;

import com.alibaba.fastjson.JSONObject;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DecodeFormat;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.target.Target;
import com.example.catedu.data.DataLoader;
import com.example.catedu.data.InstanceDetail;
import com.example.catedu.data.PicSpider;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.sina.weibo.sdk.api.ImageObject;
import com.sina.weibo.sdk.api.TextObject;
import com.sina.weibo.sdk.api.WeiboMultiMessage;
import com.sina.weibo.sdk.auth.AuthInfo;
import com.sina.weibo.sdk.auth.Oauth2AccessToken;
import com.sina.weibo.sdk.auth.WbAuthListener;
import com.sina.weibo.sdk.common.UiError;
import com.sina.weibo.sdk.openapi.IWBAPI;
import com.sina.weibo.sdk.openapi.WBAPIFactory;
import com.sina.weibo.sdk.share.WbShareCallback;

import org.scilab.forge.jlatexmath.core.AjLatexMath;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Objects;
import java.util.Vector;

import io.github.kbiakov.codeview.classifier.CodeProcessor;

public class MainActivity extends AppCompatActivity {
    public static Vector<Fragment> fragments;
    public static int last_fragment = 0;
    public static int major_fragment = 0;

    public static DataLoader dataLoader;

    public static JSONObject seenList; // 每个学科已看过的实体

    @SuppressLint("StaticFieldLeak")
    public static FragmentHome fragment_home;

    public static FragmentQuestion fragment_search;

    @SuppressLint("StaticFieldLeak")
    public static FragmentMine fragment_mine;

    IWBAPI mWBAPI;
    private static final String APP_KY = "3099333889";
    private static final String REDIRECT_URL = "https://api.weibo.com/oauth2/default.html";
    private static final String SCOPE = "";

    public static WeiboSDK weibo;

    /**
     * MainActivity 创建时的操作
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Objects.requireNonNull(getSupportActionBar()).hide();
        AjLatexMath.init(this);
        CodeProcessor.init(this);
        weibo = new WeiboSDK();
        weibo.initSdk();
        try {
            dataLoader = new DataLoader();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        setContentView(R.layout.activity_main);
        seenList = null;
        seenList = readCache();
        initView();
    }


    /**
     * 为 BottomNavigationView 绑定 Fragment
     */
    private void initView () {
        fragment_home = new FragmentHome();
        fragment_search = new FragmentQuestion();
        fragment_mine = new FragmentMine();

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

//    @Override
//    public void onBackPressed() {
//        super.onBackPressed();
//        backSwitchFragment();
//    }

    protected void backSwitchFragment() {
        int from = MainActivity.last_fragment, to;
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
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
    }
    public void forwardSwitchFragment() {
        int from = MainActivity.last_fragment, to = MainActivity.fragments.size() - 1;
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.hide(MainActivity.fragments.get(from));
        if (!MainActivity.fragments.get(to).isAdded())
            transaction.add(R.id.nav_host_fragment, MainActivity.fragments.get(to));
        transaction.show(MainActivity.fragments.get(to)).commitAllowingStateLoss();
        MainActivity.last_fragment = to; // 更新
    }

    @Override
    protected void onStop() {
        SharedPreferences sharedPref = getSharedPreferences("user", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.clear();
        editor.apply();
        super.onStop();
    }

    public static FragmentActivity getFragmentActivityFromView(View view) {
        if (null != view) {
            Context context = view.getContext();
            while (context instanceof ContextWrapper) {
                if (context instanceof Activity) {
                    return (FragmentActivity) context;
                }
                context = ((ContextWrapper) context).getBaseContext();
            }
        }
        return null;
    }

    /**
     * 加入一个看过的
     * @param ins 实体详情
     */
    public void addSeen(String uri, InstanceDetail ins) {
        if (seenList == null) seenList = new JSONObject();
        seenList.put(uri, ins.toString());
        Log.e("addSeen", uri);
        fragment_home.update();
        refreshCache();
    }

    protected void refreshCache() {
        String name = "DetailCache.dat";
        FileOutputStream outputStream;
        try {
            outputStream = openFileOutput(name, Context.MODE_PRIVATE);
            outputStream.write(seenList.toString().getBytes());
            outputStream.close();
        } catch (Exception e) {
            Log.e("RefreshCache", e.toString());
        }
    }

    public void clearCache () {
        String name = "DetailCache.dat";
        FileOutputStream outputStream;
        try {
            outputStream = openFileOutput(name, Context.MODE_PRIVATE);
            outputStream.write("".getBytes());
            outputStream.close();
        } catch (Exception e) {
            Log.e("ClearCache", e.toString());
        }
    }

    public JSONObject readCache() {
        String name = "DetailCache.dat";
        FileInputStream inStream = null;
        try {
            inStream = openFileInput(name);
        } catch (FileNotFoundException e) { //若不存在文件则创建一个
            FileOutputStream out;
            try {
                out = openFileOutput(name, Context.MODE_APPEND);
                out.write("".getBytes());
                out.close();
            } catch (Exception ignored) {
            }
        }
        try {
            inStream = openFileInput(name);
        } catch (FileNotFoundException e) {
            Log.e("ReadCache", e.toString());
        }
        byte[] b = new byte[(int) new File(getFilesDir().toString() + "/" + name).length()]; //恰好文件大小的字节数组
        try {
            Log.e("readBytes" + name + ":", String.valueOf(inStream.read(b)));
        } catch (IOException e) {
            Log.e("readStorageExc", e.toString());
        }
        try {
            inStream.close();
        } catch (IOException e) {
            Log.e("closeExc", e.toString());
        }
        String res = new String(b);
        if (res.equals("")) {
            seenList = null;
            return null;
        }
        seenList = JSONObject.parseObject(res);
        return seenList;
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable
            Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (mWBAPI != null) {
            mWBAPI.authorizeCallback(requestCode, resultCode, data);
        }
    }


    public class WeiboSDK {
        public void initSdk() {
            AuthInfo authInfo = new AuthInfo(MainActivity.this, APP_KY, REDIRECT_URL, SCOPE);
            mWBAPI = WBAPIFactory.createWBAPI(MainActivity.this);
            mWBAPI.registerApp(MainActivity.this, authInfo);
        }
    }

    public void doWeiboShare(String text, String picUrl) {
        WeiboMultiMessage message = new WeiboMultiMessage();
        TextObject textObject = new TextObject();
        // 分享⽂字
        textObject.text = text;
        message.textObject = textObject;

        if (!picUrl.equals("")) {
            ImageObject imageObject = new ImageObject();
            new Thread(() -> {
                try {
                    new Response().handle(picUrl, bitmap -> {
                        imageObject.setImageData(bitmap);
                        message.imageObject = imageObject;
                        mWBAPI.shareMessage(message, true);
                    });
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }).start();
        } else {
            mWBAPI.shareMessage(message, true);
        }
    }

    public class Response {
        public void handle (String pu, CallBack callBack) throws IOException {
            Bitmap bitmap = BitmapFactory.decodeStream(new URL(pu).openStream());
            callBack.onResponse(bitmap);
        }
    }
    interface CallBack  {
        void onResponse(Bitmap bitmap) throws IOException;
    }

}