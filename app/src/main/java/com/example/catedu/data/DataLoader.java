/**
 * @filename DataLoader
 * @description  访问不同的接口，获取数据并返回
 * @author ZhouYan
 * */

package com.example.catedu.data;

import android.content.Context;
import android.content.res.AssetManager;
import android.os.Parcelable;
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
import java.net.ProtocolException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Vector;

public class DataLoader {
    private String id = "";
    private boolean logged = false;

    public DataLoader () throws InterruptedException {
        logIn();
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
        conn.setConnectTimeout(8000); // 设置超时
        conn.setReadTimeout(8000);
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
    public Vector<Triple> getLocalCourseData (Context context, String course) throws IOException {
        String file = course + ".json";
        Vector<String> jsons = new Vector<>();
        AssetManager assetManager = context.getAssets(); // assets资源管理器
        int cnt = 0;
        try {
            // 使用IO流读取json文件内容
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(
                    assetManager.open(file), StandardCharsets.UTF_8));
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                cnt++;
                if (cnt <= 0) continue;
                jsons.add(line);
                // 超出上限后停止读
                if (cnt >= 2000) break;
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
    public Vector<Triple> getDataFromJson (Vector<String> jsons) {
        Vector<Triple> vector = new Vector<>();
//        Collections.shuffle(vector);
        Gson gson = new Gson(); // 使用 Gson 工具
        for (int i = 0; i < 2000; ++i) {
            String j = jsons.get(i);
            Triple in = gson.fromJson(j, Triple.class); // 反序列化
            vector.add(in);
        }
//        Collections.shuffle(vector);
        return vector;
    }

    /**
     * 新建一个根据 uri 来获取知识
     */
    public Instance getInstance (String course, String uri) throws InterruptedException {
        // 网络请求不能在主线程中进行，而是要在一个子线程中
        Instance ins = new Instance();
        Thread net_conn_thread = new Thread(() -> {
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
     * 根据 uri 来获取实体简略信息
     */
    public Instance getKnowledgeByUri(String course, String uri) throws IOException, JSONException {
        URL ins_url = new URL("http://open.edukg.cn/opedukg/api/typeOpen/open/getKnowledgeCard");
        HttpURLConnection conn = (HttpURLConnection) ins_url.openConnection(); // 创建HttpURLConnection对象
        conn.setRequestMethod("POST"); // 请求方式为 POST
        conn.setConnectTimeout(8000); // 设置超时
        conn.setReadTimeout(8000);
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
        return new Instance(entity_name, entity_type);
    }

    /**
     * Post 请求根据 uri 获取实体详情
     */
    public InstanceDetail getDetailByUri (String course, String uri) throws IOException, JSONException, InterruptedException {
        URL ins_url = new URL("http://open.edukg.cn/opedukg/api/typeOpen/open/getKnowledgeCard");
        HttpURLConnection conn = (HttpURLConnection) ins_url.openConnection(); // 创建HttpURLConnection对象
        conn.setRequestMethod("POST"); // 请求方式为 POST
        conn.setConnectTimeout(8000); // 设置超时
        conn.setReadTimeout(8000);
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
                res.append(line);
            }
            in.close();
            conn.disconnect();
        }
        if (res.toString().equals("")) {
            Log.e("Entity", "Empty!");
            return new InstanceDetail();
        }
        Log.e("Res(Detail)", res.toString());
        String entity_type;
        String entity_name;
        JSONArray entity_features;
        JSONObject res_json = new JSONObject(res.toString());
        JSONObject data_json = res_json.getJSONObject("data");
        entity_type = data_json.getString("entity_type");
        entity_name = data_json.getString("entity_name");
        entity_features = data_json.getJSONArray("entity_features");
        return new InstanceDetail(entity_type, entity_name, entity_features);
    }

    public Vector<Triple> getInstanceListByString(String course, String keyword) throws IOException, JSONException {
        URL ins_url = new URL("http://open.edukg.cn/opedukg/api/typeOpen/open/instanceList");
        HttpURLConnection conn = (HttpURLConnection) ins_url.openConnection(); // 创建HttpURLConnection对象
        conn.setRequestMethod("GET"); // 请求方式为 GET
        conn.setConnectTimeout(3000); // 设置超时
        conn.setReadTimeout(3000);
        conn.setDoOutput(true);
        conn.setDoInput(true);
//        conn.setUseCaches(false);
//        conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8"); // 设置请求头
        conn.connect();

        // 写入参数
        HashMap<String, String> map = new HashMap<String, String>();
        map.put("course", course);
        map.put("uri", keyword);
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
            while ((line = bf.readLine()) != null) {
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
        return new Vector<Triple>();
    }
    /**
     * Get 请求根据实体名称获取相关试题
     */
    public Vector<Ques> getInstanceQues (String name) throws IOException, JSONException {
        String root = "http://open.edukg.cn/opedukg/api/typeOpen/open/questionListByUriName?uriName=";
        String tail = "&id=" + id;
        URL url = new URL(root + name + tail);
        HttpURLConnection conn = (HttpURLConnection)url.openConnection();
        conn.setRequestMethod("GET");
        //Get请求不需要DoOutPut
        conn.setDoOutput(false);
        conn.setDoInput(true);
        //设置连接超时时间和读取超时时间

        conn.setConnectTimeout(8000);
        conn.setReadTimeout(8000);
      
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
        JSONObject json = new JSONObject(result.toString());
        return getQuesFromJson(json);
    }

    public Vector<Ques> getQuesFromJson (JSONObject json) throws JSONException {
        Vector<Ques> vector = new Vector<>();
        Gson gson = new Gson(); // 使用 Gson 工具
        JSONArray data = json.getJSONArray("data");
        if (data.length() == 0) return vector;
        else {
            for (int i = 0; i < data.length(); ++i) {
                JSONObject item = data.getJSONObject(i);
                Ques ques = gson.fromJson(String.valueOf(item), Ques.class);
                String answer = ques.getqAnswer();
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

    public Vector<Subject> getLinkSubjects (String name, String course) throws IOException, JSONException {
        String root = "http://open.edukg.cn/opedukg/api/typeOpen/open/infoByInstanceName?name=";
        String middle = "&course=" + course;
        String tail = "&id=" + id;
        URL url = new URL(root + name + middle + tail);
        HttpURLConnection conn = (HttpURLConnection)url.openConnection();
        conn.setRequestMethod("GET");
        //Get请求不需要DoOutPut
        conn.setDoOutput(false);
        conn.setDoInput(true);
        //设置连接超时时间和读取超时时间
        conn.setConnectTimeout(8000);
        conn.setReadTimeout(8000);
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
        JSONObject json = new JSONObject(result.toString());
        Log.e("LinkSubjects", result.toString());
        return getSubjectsFromJson(json);
    }

    public Vector<Subject> getSubjectsFromJson (JSONObject json) throws JSONException {
        Vector<Subject> vector = new Vector<>();
        JSONObject data = json.getJSONObject("data");
        JSONArray content = data.getJSONArray("content");
        if (content.length() == 0) return vector;
        else {
            for (int i = 0; i < content.length(); ++i) {
                JSONObject item = content.getJSONObject(i);
                String sub_pre = item.getString("predicate_label");
                if (item.has("object")) { // object
                    String sub_name = item.getString("object_label");
                    String sub_uri = item.getString("object");
                    Subject sub = new Subject(sub_pre, sub_name, sub_uri, true);
                    vector.add(sub);
                } else { // subject
                    String sub_name = item.getString("subject_label");
                    String sub_uri = item.getString("subject");
                    Subject sub = new Subject(sub_pre, sub_name, sub_uri, false);
                    vector.add(sub);
                }
            }
        }
        return vector;
    }

}
