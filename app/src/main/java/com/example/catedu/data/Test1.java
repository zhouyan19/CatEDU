package com.example.catedu.data;

import android.content.Context;
import android.content.res.AssetManager;
import android.util.Log;


import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Vector;

public class Test1 {

    public static void getInstanceListByString(String course, String keyword) throws IOException, JSONException {
        URL ins_url = new URL("http://open.edukg.cn/opedukg/api/typeOpen/open/instanceList");
        HttpURLConnection conn = (HttpURLConnection) ins_url.openConnection(); // 创建HttpURLConnection对象
        conn.setRequestMethod("GET"); // 请求方式为 GET
        conn.setConnectTimeout(3000); // 设置超时
        conn.setReadTimeout(3000);
        conn.setDoOutput(true);
        conn.setDoInput(true);
//        conn.setUseCaches(false);
        conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8"); // 设置请求头
        conn.connect();

        // 写入参数
        HashMap<String, String> map = new HashMap<String, String>();
        map.put("course", course);
        map.put("uri", keyword);
        String id = "6ce51df5-5758-4768-b997-30a8429512b0";
        map.put("id", id);
        String params = new Gson().toJson(map);

        // 获取输出流，写入参数
        OutputStream out = conn.getOutputStream();
        out.write(params.getBytes());
        out.flush();
        out.close();

        // 读取响应
        StringBuilder res = new StringBuilder();
        int code = conn.getResponseCode();
        if (code == 200) {
            InputStreamReader in = new InputStreamReader(conn.getInputStream());
            BufferedReader bf = new BufferedReader(in);
            String line;
            // 一行一行读取
            while ((line = bf.readLine()) != null){
                Log.i("res_line", line);
                res.append(line);
            }
            in.close();
            conn.disconnect();
        }
//        if (res.toString().equals("")) {
//            Log.e("Entity", "Empty!");
//            return new Instance();
//        }
//        Log.e("Res", res.toString());
//        String entity_type = "无类别";
//        String entity_name = "无名称";
//        JSONObject res_json = new JSONObject(res.toString());
//        JSONObject data_json = res_json.getJSONObject("data");
//        entity_type = data_json.getString("entity_type");
//        entity_name = data_json.getString("entity_name");
    }

    public static void main(String[] args) throws IOException, JSONException {
        getInstanceListByString("geo", "地球");
    }

}