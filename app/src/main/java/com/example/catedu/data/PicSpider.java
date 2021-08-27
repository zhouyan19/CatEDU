package com.example.catedu.data;

import android.util.Log;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class PicSpider {
    public static String name;

    public PicSpider(String _n) { name = _n; }

    public String getPic () throws IOException {
        String url = "https://pic.sogou.com/pics?query=" + name;
        String ua = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.77 Safari/537.36";
        Document document = Jsoup.connect(url).userAgent(ua).get();
//        Log.e("Document", String.valueOf(document).substring(5000));
        Elements imgs = document.getElementsByTag("img");
        Log.e("imgs", String.valueOf(imgs.size()));
        return "";
//        Element ul_list = document.getElementsByClass("figure-result-list").select("ul").first();
//        Element img = ul_list.child(0).child(0).child(0).children().select("img").first();
//        String res = img.attr("src");
//        Log.e("Pic", res);
//        return res;
    }

//    public String getRequest () throws IOException {
//        URL url = new URL("https://pic.sogou.com/pics?query=" + name);
//        HttpURLConnection conn = (HttpURLConnection)url.openConnection();
//        conn.setRequestMethod("GET");
//        //Get请求不需要DoOutPut
//        conn.setDoOutput(false);
//        conn.setDoInput(true);
//        //设置连接超时时间和读取超时时间
//        conn.setConnectTimeout(5000);
//        conn.setReadTimeout(5000);
//        conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
//        //连接服务器
//        conn.connect();
//        // 取得输入流，并使用Reader读取
//        BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
//        StringBuilder result = new StringBuilder();
//        String line;
//        while ((line = in.readLine()) != null) {
//            result.append(line);
//        }
//        in.close();
//        return result.toString();
//    }
}
