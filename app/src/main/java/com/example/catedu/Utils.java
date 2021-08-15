package com.example.catedu;

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
                break;
        }
        return eng;
    }
}
