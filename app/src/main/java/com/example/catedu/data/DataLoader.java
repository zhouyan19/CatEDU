/**
 * @filename DataLoader
 * @description  访问不同的接口，获取数据并返回
 * @author ZhouYan
 * */

package com.example.catedu.data;

import android.nfc.cardemulation.OffHostApduService;
import android.util.Log;

import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

public class DataLoader {
    private String id = "";
    private boolean logged = false;

    /**
     * 这个函数要实现什么功能？
     */
    public void getOriginalData () throws InterruptedException {
        // 网络请求不能在主线程中进行，而是要在一个子线程中
        Log.e("DataLoader", "getting data...");
        Thread net_conn_thread = new Thread(() -> {
            try {
                id = logIn(); // 先登录
                Log.e("getOriginalData", id);
            } catch (IOException | JSONException e) {
                Log.e("getOriginalData", "Failed!");
                e.printStackTrace();
            }
        });
        net_conn_thread.start();
        net_conn_thread.join();
    }

    /**
     * 发送 POST 请求以登录， 并获取 id
     */
    public String logIn () throws IOException, JSONException {
        String phone = "18357331593"; // zy的手机号
        String password = "2021zyywtwzt"; // zy的密码
        URL login_url = new URL("http://open.edukg.cn/opedukg/api/typeAuth/user/login");
        HttpURLConnection conn = (HttpURLConnection) login_url.openConnection(); // 创建HttpURLConnection对象
        conn.setRequestMethod("POST"); // 请求方式为 POST
        conn.setConnectTimeout(4000); // 设置超时
        conn.setReadTimeout(4000);
        conn.setDoOutput(true);
        conn.setDoInput(true);
        conn.setUseCaches(false); // Post方式不能缓存,需手动设置为false
        conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8"); // 设置请求头
        conn.connect();

        // 写入参数
        Map map = new HashMap<String, String>();
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
            Log.e("LogIn", "登陆成功！");
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

        if (res.toString().equals("")) return "";
        JSONObject login_json = new JSONObject(res.toString());
        return login_json.getString("id");
    }

    public void getFromUrl (String url) {

    }

}
