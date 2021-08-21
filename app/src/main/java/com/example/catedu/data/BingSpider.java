package com.example.catedu.data;

import android.util.Log;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.net.URL;

public class BingSpider {
    public static String name;

    public BingSpider(String _n) { name = _n; }

    public String getPic () throws IOException {
        String url = "https://image.so.com/i?q=" + name + "&src=srp";
        Document document = Jsoup.parse(new URL(url).openStream(), "UTF-8", url);
        Element img_span = document.getElementsByClass("img").select("span").first();
        Element img = img_span.children().first();
        String res = img.attr("src");
        return res;
    }
}
