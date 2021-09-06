package com.example.catedu;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;

import java.io.ByteArrayOutputStream;

public class Utils {
    static String English(String chi) {
        String eng = "";
        switch (chi) {
            case "语文":
                eng = "chinese";
                break;
            case "英语":
                eng = "english";
                break;
            case "数学":
                eng = "math";
                break;
            case "物理":
                eng = "physics";
                break;
            case "化学":
                eng = "chemistry";
                break;
            case "生物":
                eng = "biology";
                break;
            case "历史":
                eng = "history";
                break;
            case "地理":
                eng = "geo";
                break;
            case "政治":
                eng = "politics";
                break;
            default:
                eng = chi;
                break;
        }
        return eng;
    }

    static String Chinese(String eng) {
        String chi = "";
        switch (eng) {
            case "chinese":
                chi = "语文";
                break;
            case "english":
                chi = "英语";
                break;
            case "math":
                chi = "数学";
                break;
            case "physics":
                chi = "物理";
                break;
            case "chemistry":
                chi = "化学";
                break;
            case "biology":
                chi = "生物";
                break;
            case "history":
                chi = "历史";
                break;
            case "geo":
                chi = "地理";
                break;
            case "politics":
                chi = "政治";
                break;
            default:
                chi = eng;
                break;
        }
        return chi;
    }

    public static Bitmap stringToBitmap(String string) {
        // 将字符串转换成Bitmap类型
        Bitmap bitmap = null;
        try {
            byte[] bitmapArray;
            bitmapArray = Base64.decode(string, Base64.DEFAULT);
            bitmap = BitmapFactory.decodeByteArray(bitmapArray, 0,
                    bitmapArray.length);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return bitmap;
    }

    public static String bitmapToString(Bitmap bitmap){
        //将Bitmap转换成字符串
        String string = null;
        ByteArrayOutputStream bStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG,100,bStream);
        byte []bytes = bStream.toByteArray();
        string = Base64.encodeToString(bytes,Base64.DEFAULT);
        return string;
    }
}
