package com.example.catedu;

import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class NetWorkTask implements Runnable {
    String url;
    HashMap<String, String> headers;
    JSONObject requestBody;
    String requestParamName;
    String requestParam;
    String method;
    String token;
    int taskno = 0;

    public NetWorkTask(String url, String requestParamName, String requestParam) {
        this.url = url;
        this.method = "POST";
        this.requestParamName = requestParamName;
        this.requestParam = requestParam;
    }

    public NetWorkTask(int taskno, String token,String requestParam) {
        this.taskno = taskno;
        this.token=token;
        this.requestParam = requestParam;
    }

    Map<String, Object> map;

    @Override
    public void run() {
        switch (taskno) {
            case 3:{
                JSONObject jsonObject = new JSONObject();
                try {
                    jsonObject.put("token", token);
                    jsonObject.put("selfie", requestParam);

                } catch (JSONException e) {
                    e.printStackTrace();
                }
                Document doc = null;
                url="http://183.173.179.9:8080/user/modify/selfie";
                try {
                    HashMap<String, String> headers = new HashMap<>();
                    headers.put("Content-Type", "application/json");
                    doc = Jsoup.connect(url).headers(headers).requestBody(jsonObject.toString()).ignoreContentType(true).post();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                if(doc!=null){
//            System.out.println(doc);
                    Element body = doc.body();
                    String str = body.text();
                    Gson gson = new Gson();
                    map=new HashMap<>();
                    map = gson.fromJson(str, map.getClass());
                }
                break;
            }
            case 2: {
                JSONObject jsonObject = new JSONObject();
                try {
                    jsonObject.put("token", token);
                    jsonObject.put("email", requestParam);

                } catch (JSONException e) {
                    e.printStackTrace();
                }
                Document doc = null;
                url="http://183.173.179.9:8080/user/modify/email";
                try {
                    HashMap<String, String> headers = new HashMap<>();
                    headers.put("Content-Type", "application/json");
                    doc = Jsoup.connect(url).headers(headers).requestBody(jsonObject.toString()).ignoreContentType(true).post();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                if(doc!=null){
//            System.out.println(doc);
                    Element body = doc.body();
                    String str = body.text();
                    Gson gson = new Gson();
                    map=new HashMap<>();
                    map = gson.fromJson(str, map.getClass());
                }
                break;
            }
            case 1: {
                JSONObject jsonObject = new JSONObject();
                try {
                    jsonObject.put("token", token);
                    jsonObject.put("nickname", requestParam);

                } catch (JSONException e) {
                    e.printStackTrace();
                }
                Document doc = null;
                url="http://183.173.179.9:8080/user/modify/nickname";
                try {
                    HashMap<String, String> headers = new HashMap<>();
                    headers.put("Content-Type", "application/json");
                    doc = Jsoup.connect(url).headers(headers).requestBody(jsonObject.toString()).ignoreContentType(true).post();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                if(doc!=null){
//            System.out.println(doc);
                    Element body = doc.body();
                    String str = body.text();
                    Gson gson = new Gson();
                    map=new HashMap<>();
                    map = gson.fromJson(str, map.getClass());
                }
                break;
            }
            default:
            case 0: {
                if (method == "POST") {
                    Document doc = null;
                    try {
                        if (requestBody != null) {
                            doc = Jsoup.connect(url).headers(headers).ignoreContentType(true).requestBody(requestBody.toString()).post();
                        } else if (requestParam != null) {
                            doc = Jsoup.connect(url).ignoreContentType(true).data(requestParamName, requestParam).post();
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    if (doc != null) {
                        Element body = doc.body();
                        String str = body.text();
                        Gson gson = new Gson();
                        map = new HashMap<String, Object>();
                        map = gson.fromJson(str, map.getClass());
//                        Logger.e("DEBUG", map.toString());
                    }
                }
            }
        }

    }

    public String getUrl() {
        return url;
    }


    public void setUrl(String url) {
        this.url = url;
    }

    public HashMap<String, String> getHeaders() {
        return headers;
    }

    public void setHeaders(HashMap<String, String> headers) {
        this.headers = headers;
    }

    public JSONObject getRequestBody() {
        return requestBody;
    }

    public void setRequestBody(JSONObject requestBody) {
        this.requestBody = requestBody;
    }

    public String getRequestParam() {
        return requestParam;
    }

    public void setRequestParam(String requestParam) {
        this.requestParam = requestParam;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public Map<String, Object> getMap() {
        return map;
    }

    public void setMap(Map<String, Object> map) {
        this.map = map;
    }
}
