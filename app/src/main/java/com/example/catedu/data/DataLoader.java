/**
 * @filename DataLoader
 * @description  访问不同的接口，获取数据并返回
 * @author ZhouYan
 * */

package com.example.catedu.data;

import android.content.Context;
import android.content.res.AssetManager;
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
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;

public class DataLoader {
    private final int NUM_PER_PAGE = 10;
    private String id = "";
    private boolean logged = false;

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
        conn.setConnectTimeout(4000); // 设置超时
        conn.setReadTimeout(4000);
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

        if (res.toString().equals("")) {
            Log.e("LogIn", "Fail");
            return;
        }
        JSONObject login_json = new JSONObject(res.toString());
        id = login_json.getString("id");
    }

    /**
     * 从本地 csv 文件加载实体数据
     * @param course 课程英文名
     */
    public ArrayList<Triple> getLocalCourseData (Context context, String course, int idx) throws IOException {
        String file = course + ".json";
        ArrayList<String> jsons = new ArrayList<>();
        AssetManager assetManager = context.getAssets(); // assets资源管理器
        int cnt = 0;
        try {
            // 使用IO流读取json文件内容
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(
                    assetManager.open(file), StandardCharsets.UTF_8));
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                // 跳过已经读入的
                if (cnt < idx) {
                    cnt++;
                    continue;
                }
                Log.e("Reading", line);
                jsons.add(line);
                cnt++;
                // 超出NUM_PER_PAGE后停止读
                if (cnt >= idx + NUM_PER_PAGE) break;
            }
            bufferedReader.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        Log.e("getLocalCourseData", String.valueOf(cnt));
        return getDataFromJson(jsons);
    }

    /**
     * 把读到的 json 字符串转为 Instance 对象
     * @param jsons json字符串数组
     */
    public ArrayList<Triple> getDataFromJson(ArrayList<String> jsons) {
        ArrayList<Triple> arrayList = new ArrayList<>();
        Gson gson = new Gson(); // 使用 Gson 工具
        for (String j : jsons) {
            Triple in = gson.fromJson(j, Triple.class); // 反序列化
            arrayList.add(in);
        }
        return arrayList;
    }

    /**
     * 新建一个根据 uri 来获取知识
     */
    public Instance getInstance (String course, String uri) throws InterruptedException {
        // 网络请求不能在主线程中进行，而是要在一个子线程中
        Log.e("GetInstance", course + "/" + uri);
        Instance ins = new Instance();
        Thread net_conn_thread = new Thread(() -> {
            if (!logged) {
                try {
                    logInOnSubThread();
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            try {
                Instance tmp = getKnowledgeByUri(course, uri);
                ins.setName(tmp.getName());
                ins.setType(tmp.getType());
            } catch (IOException | JSONException e) {
                e.printStackTrace();
            }
        });
        net_conn_thread.start();
        net_conn_thread.join();
        return ins;
    }

    /**
     * 根据 uri 来获取知识
     */
    public Instance getKnowledgeByUri(String course, String uri) throws IOException, JSONException {
        URL ins_url = new URL("http://open.edukg.cn/opedukg/api/typeOpen/open/getKnowledgeCard");
        HttpURLConnection conn = (HttpURLConnection) ins_url.openConnection(); // 创建HttpURLConnection对象
        conn.setRequestMethod("POST"); // 请求方式为 POST
        conn.setConnectTimeout(4000); // 设置超时
        conn.setReadTimeout(4000);
        conn.setDoOutput(true);
        conn.setDoInput(true);
        conn.setUseCaches(false); // Post方式不能缓存,需手动设置为false
        conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8"); // 设置请求头
        conn.connect();

        // 写入参数
        HashMap map = new HashMap<String, String>();
        map.put("course", course);
        map.put("uri", uri);
        map.put("id", id);
        String params = new Gson().toJson(map);
        Log.e("Params", params);

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
            Log.e("Entity", "获取成功！");
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
            Log.e("Entity", "Empty!");
            return new Instance();
        }
        Log.e("Res", res.toString());
        String entity_type = "无类别";
        String entity_name = "无名称";
        JSONObject res_json = new JSONObject(res.toString());
        JSONObject data_json = res_json.getJSONObject("data");
        entity_type = data_json.getString("entity_type");
        entity_name = data_json.getString("entity_name");
        Log.e("Instance/Name", entity_name);
        Log.e("Instance/Type", entity_type);
        return new Instance(entity_name, entity_type);
    }

}
