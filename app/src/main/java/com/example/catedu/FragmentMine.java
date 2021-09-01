/**
 * @filename FragmentMine
 * @description  我的组件 (3)
 * @author ZhouYan
 * */

package com.example.catedu;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.catedu.widget.RoundImageView;
import com.google.gson.Gson;
import com.google.gson.internal.LinkedTreeMap;

import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class FragmentMine extends Fragment {

    /**
     * FragmentMine 创建时的操作
     */
    String token=null;
    boolean logined;
    RoundImageView selfie;
    TextView nickname_view;
    SharedPreferences sharedPreferences;
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        return inflater.inflate(R.layout.fragment_mine, container, false);
    }

    public void refreshView(View view) throws InterruptedException {
        sharedPreferences= getActivity().getSharedPreferences("user", Context.MODE_PRIVATE);
        this.token=sharedPreferences.getString("token",null);
        Runnable networkTask = new Runnable() {
            @Override
            public void run() {
                if (token != null) {
                    String url = "http://183.173.179.9:8080/user/info";
                    HashMap<String, String> headers = new HashMap<>();
                    headers.put("Content-Type", "application/json");
                    Document doc = null;
                    try {
                        doc = Jsoup.connect(url).ignoreContentType(true).data("token",token).post();
                        if(doc!=null){
                            Element body = doc.body();
                            String str = body.text();
                            Gson gson = new Gson();
                            Map<String, Object> map = new HashMap<String, Object>();
                            map = gson.fromJson(str, map.getClass());
                            boolean suc = (boolean) map.get("success");
                            LinkedTreeMap linkedTreeMap=(LinkedTreeMap) map.get("detail");
                            if (suc) {
                                logined=true;
                                String nickname=(String) linkedTreeMap.get("nickname");
                                Double selfie_num = (Double) linkedTreeMap.get("selfie");
                                String image_name = "avatar_icon_" + String.valueOf(selfie_num.intValue()) ;
                                int src = getActivity().getResources().getIdentifier(image_name, "drawable", getActivity().getPackageName());
                                getActivity().runOnUiThread(()->{
                                    selfie.setImageResource(src);
                                    nickname_view.setText(nickname);
                                });
                            }
                        }
                        else {
                            Looper.prepare();
                            Toast.makeText(getActivity(), "网络出错", Toast.LENGTH_SHORT).show();
                            Looper.loop();
                        }

                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        };
        Thread newThread= new Thread(networkTask);
        newThread.start();
        newThread.join();
        int refIds[] ={R.id.selfie,R.id.login_textView};
        for (int id : refIds) {
            if(!logined){
                view.findViewById(id).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        // your code here.
                        MainActivity.fragments.add(new FragmentLogin());
                        forwardSwitchFragment();
                    }
                });
            }
            else{
                view.findViewById(id).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        MainActivity.fragments.add(new FragmentUserInfo());
                        forwardSwitchFragment();
                    }
                });
            }}
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {

        logined=false;
        selfie = (RoundImageView) view.findViewById(R.id.selfie);
        nickname_view = (TextView) view.findViewById(R.id.login_textView);


        try {
            refreshView(view);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        Button clear_backup=(Button) view.findViewById(R.id.clear_backup);
        clear_backup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                MainActivity.fragments.add(new FragmentAvatar());
//                forwardSwitchFragment();
                sharedPreferences.edit().clear();
                sharedPreferences.edit().apply();
                Toast.makeText(getActivity(),"缓存清除成功",Toast.LENGTH_LONG).show();
            }
        });
        super.onViewCreated(view, savedInstanceState);
    }
    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged( hidden );
        if (hidden) {// 不在最前端界面显示
        } else {// 重新显示到最前端中
            try {
                refreshView(getView());
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
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
