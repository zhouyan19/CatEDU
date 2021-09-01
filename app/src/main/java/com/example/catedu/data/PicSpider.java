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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PicSpider {
    public static String name;

    public PicSpider(String _n) { name = _n; }

    public String getPic () throws IOException {
        String url = "https://pic.sogou.com/pics?query=" + name;
        Log.e("URL", url);
        String ua = "Mozilla/5.0 (Macintosh; Intel Mac OS X 10.6; rv:2.0.1) Gecko/20100101 Firefox/4.0.1";
        Document document = Jsoup.connect(url).userAgent(ua).get();
        String docs = document.toString();
        docs = unicodeToString(docs);
        String pattern = "locImageLink\":\"(.+?)\",";
        Pattern r = Pattern.compile(pattern);
        Matcher m = r.matcher(docs);
        Log.e("getPic", "Compile and Match");
        String res = "";
        if (m.find()) {
            res = m.group(0);
            res = res.substring(15);
            res = res.substring(0, res.length() - 2);
            Log.e("Match", res);
        }
        return res;
    }

    public static String unicodeToString(String str) {
        Pattern pattern = Pattern.compile("(\\\\u(\\p{XDigit}{4}))");
        Matcher matcher = pattern.matcher(str);
        char ch;
        while (matcher.find()) {
            //group 6728
            String group = matcher.group(2);
            //ch:'木' 26408
            ch = (char) Integer.parseInt(group, 16);
            //group1 \u6728
            String group1 = matcher.group(1);
            str = str.replace(group1, ch + "");
        }
        return str;
    }

}
