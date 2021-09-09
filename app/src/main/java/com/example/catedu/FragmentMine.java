/**
 * @filename FragmentMine
 * @description 我的组件 (3)
 * @author ZhouYan
 */

package com.example.catedu;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import com.example.catedu.data.Ques;
import com.example.catedu.widget.RoundImageView;
import com.google.gson.Gson;
import com.google.gson.internal.LinkedTreeMap;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

public class FragmentMine extends Fragment {

    /**
     * FragmentMine 创建时的操作
     */
    String token = null;
    boolean logined;
    RoundImageView selfie;
    TextView nickname_view;
    SharedPreferences sharedPreferences;
    LinearLayout info;
    LinearLayout history;
    LinearLayout local_cache;
    LinearLayout stars;
    LinearLayout recommendation;
    private boolean logged;
    private String id;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        return inflater.inflate(R.layout.fragment_mine, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {

        logined = false;
        logged=false;
        selfie = (RoundImageView) view.findViewById(R.id.selfie);
        nickname_view = (TextView) view.findViewById(R.id.login_textView);
        info=(LinearLayout) view.findViewById(R.id.info);
        history=(LinearLayout) view.findViewById(R.id.history);
        local_cache=(LinearLayout) view.findViewById(R.id.local_cache);
        stars=(LinearLayout) view.findViewById(R.id.stars);


        try {
            refreshView(view);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        Button clear_backup = (Button) view.findViewById(R.id.clear_backup);
        clear_backup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                MainActivity.fragments.add(new FragmentAvatar());
//                forwardSwitchFragment();
                ((MainActivity) getActivity()).clearCache();
                Toast.makeText(getActivity(), "缓存清除成功", Toast.LENGTH_LONG).show();
            }
        });

        history.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MainActivity.fragments.add(new FragmentUserHistory());
                forwardSwitchFragment();
            }
        });
        local_cache.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MainActivity.fragments.add(new FragmentHistory());
                forwardSwitchFragment();
            }
        });
        stars.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MainActivity.fragments.add(new FragmentStars());
                forwardSwitchFragment();
            }
        });

        recommendation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Vector<Ques> vecQues=new Vector<>();
                SharedPreferences sharedPreferences = getActivity().getSharedPreferences("user", Context.MODE_PRIVATE);
                String token = sharedPreferences.getString("token", null);
                if (token != null) {
                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put("token", token);
                    NetWorkTask netWorkTask = new NetWorkTask(5, token, jsonObject.toString());
                    Thread newThread = new Thread(netWorkTask);
                    newThread.start();
                    try {
                        newThread.join();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    String res = netWorkTask.getRes();
                    JSONObject resJson = JSONObject.parseObject(res);
                    JSONArray jsonArray = (JSONArray) resJson.get("detail");
                    for (int i = 0; i < jsonArray.size(); i++) {
                        JSONObject jsonObject1=(JSONObject) jsonArray.get(i);
                        JSONObject detail = JSONObject.parseObject(jsonObject1.get("detail").toString());
                        String name = detail.getString("entity_name");
                        try {
                            Vector<Ques> tmpVec=getInstanceQues(name);
                            vecQues.addAll(tmpVec);

                        } catch (IOException e) {
                            e.printStackTrace();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                    Collections.shuffle(vecQues);
                    MainActivity.fragments.add(new FragmentRecommend((Vector<Ques>) vecQues.subList(0,10)));
                    forwardSwitchFragment();


                } else {
                    Toast.makeText(getActivity(), "未登录", Toast.LENGTH_SHORT).show();
                }
            }
        });
        super.onViewCreated(view, savedInstanceState);
    }

    public Vector<Ques> getInstanceQues (String name) throws IOException, JSONException, InterruptedException {
        if (!logged) logIn();
        String root = "http://open.edukg.cn/opedukg/api/typeOpen/open/questionListByUriName?uriName=";
        String tail = "&id=" + id;
        URL url = new URL(root + name + tail);
        HttpURLConnection conn = (HttpURLConnection)url.openConnection();
        conn.setRequestMethod("GET");
        //Get请求不需要DoOutPut
        conn.setDoOutput(false);
        conn.setDoInput(true);
        //设置连接超时时间和读取超时时间

        conn.setConnectTimeout(12000);
        conn.setReadTimeout(12000);

        conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
        //连接服务器
        conn.connect();
        // 取得输入流，并使用Reader读取
        BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
        StringBuilder result = new StringBuilder();
        String line;
        while ((line = in.readLine()) != null) {
            result.append(line);
        }
        in.close();
        JSONObject json = new JSONObject();
        return getQuesFromJson(json);
    }

    public Vector<Ques> getQuesFromJson (JSONObject json) throws JSONException {
        Vector<Ques> vector = new Vector<>();
        Gson gson = new Gson(); // 使用 Gson 工具
        JSONArray data = json.getJSONArray("data");
        if (data.size() == 0) return vector;
        else {
            for (int i = 0; i < data.size(); ++i) {
                JSONObject item = data.getJSONObject(i);
                Ques ques = gson.fromJson(String.valueOf(item), Ques.class);
                String answer = ques.getqAnswer();

                // 排除脏选项
                if (answer.equals("A") ||
                        answer.equals("B") ||
                        answer.equals("C") ||
                        answer.equals("D")) {
                    ques.print();
                    vector.add(ques);
                }
            }
        }
        return vector;
    }

    public void logIn () throws InterruptedException {
        Thread net_conn_thread = new Thread(() -> {
            try {
                logInOnSubThread();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        });
        net_conn_thread.start();
        net_conn_thread.join();
    }

    /**
     * 发送 POST 请求以登录， 并获取 id
     */
    public void logInOnSubThread () throws IOException, JSONException {
        String phone = "18357331593"; // zy的手机号
        String password = "2021zyywtwzt"; // zy的密码
        URL login_url = new URL("http://open.edukg.cn/opedukg/api/typeAuth/user/login");
        HttpURLConnection conn = (HttpURLConnection) login_url.openConnection(); // 创建HttpURLConnection对象
        conn.setRequestMethod("POST"); // 请求方式为 POST
        conn.setConnectTimeout(12000); // 设置超时
        conn.setReadTimeout(12000);
        conn.setDoOutput(true);
        conn.setDoInput(true);
        conn.setUseCaches(false); // Post方式不能缓存,需手动设置为false
        conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8"); // 设置请求头
        conn.connect();

        // 写入参数
        HashMap map = new HashMap<String, String>();
        map.put("password", password);
        map.put("phone", phone);
        String params = new Gson().toJson(map);

        // 获取输出流，写入参数
        OutputStream out = conn.getOutputStream();
        out.write(params.getBytes());
        out.flush();
        out.close();

        // 读取响应
        StringBuilder res = new StringBuilder();
        int code = conn.getResponseCode();
        Log.e("Response", String.valueOf(code));
        if (code == 200) {
            Logger.e("LogIn", "登陆成功！");
            logged = true;
            InputStreamReader in = new InputStreamReader(conn.getInputStream());
            BufferedReader bf = new BufferedReader(in);
            String line;
            // 一行一行读取
            while ((line = bf.readLine()) != null){
                res.append(line);
            }
            in.close();
            conn.disconnect();
        }

        if (res.toString().equals("")) {
            Logger.e("LogIn", "Fail");
            return;
        }
        JSONObject login_json = JSONObject.parseObject(res.toString());
        id = login_json.getString("id");
    }
    public void refreshView(View view) throws InterruptedException {

        sharedPreferences = getActivity().getSharedPreferences("user", Context.MODE_PRIVATE);
        this.token = sharedPreferences.getString("token", null);
        Runnable networkTask = new Runnable() {
            @Override
            public void run() {
                if (token != null) {
                    String url = getActivity().getString(R.string.ip) + "/user/info";
                    HashMap<String, String> headers = new HashMap<>();
                    headers.put("Content-Type", "application/json");
                    Document doc = null;
                    try {
                        doc = Jsoup.connect(url).ignoreContentType(true).data("token", token).post();
                        if (doc != null) {
                            Element body = doc.body();
                            String str = body.text();
                            Gson gson = new Gson();
                            Map<String, Object> map = new HashMap<String, Object>();
                            map = gson.fromJson(str, map.getClass());
                            boolean suc = (boolean) map.get("success");
                            LinkedTreeMap linkedTreeMap = (LinkedTreeMap) map.get("detail");
                            if (suc) {
                                logined = true;
                                String nickname = (String) linkedTreeMap.get("nickname");
                                Double selfie_num = (Double) linkedTreeMap.get("selfie");
                                String image_name = "avatar_icon_" + String.valueOf(selfie_num.intValue());
                                int src = getActivity().getResources().getIdentifier(image_name, "drawable", getActivity().getPackageName());
                                getActivity().runOnUiThread(() -> {
                                    selfie.setImageResource(src);
                                    nickname_view.setText(nickname);
                                });
                            }
                        } else {
                            Looper.prepare();
                            Toast.makeText(getActivity(), "网络出错", Toast.LENGTH_SHORT).show();
                            Looper.loop();
                        }

                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                else{
                    logined=false;
                    getActivity().runOnUiThread(() -> {
                        selfie.setImageResource(R.drawable.cat_edu);
                        nickname_view.setText("我");
                    });
                }
            }
        };
        Thread newThread = new Thread(networkTask);
        newThread.start();
        newThread.join();

        if (!logined) {
            info.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    // your code here.
                    MainActivity.fragments.add(new FragmentLogin());
                    forwardSwitchFragment();
                }
            });
        } else {
            info.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    MainActivity.fragments.add(new FragmentUserInfo());
                    forwardSwitchFragment();
                }
            });
        }
    }


    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
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
