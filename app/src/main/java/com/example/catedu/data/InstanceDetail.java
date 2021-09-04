/**
 * @filename InstanceDetail
 * @description 实体类（详细）
 * @author ZhouYan
 * */

package com.example.catedu.data;

import android.util.Log;

import com.google.gson.Gson;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;

public class InstanceDetail {
    String uri;
    String course;
    String entity_name;
    JSONArray entity_features;

    public InstanceDetail() {
        uri = "";
        course = "";
        entity_name = "";
        entity_features = new JSONArray();
    }

    InstanceDetail (String _u, String _c, String _n, JSONArray _f) {
        uri = _u;
        course = _c;
        entity_name = _n;
        entity_features = _f;
    }

    public String getUri() { return uri; }

    public String getCourse() { return course; }

    public String getEntity_name() {
        return entity_name;
    }

    public JSONArray getEntity_features() {
        return entity_features;
    }

    public void setUri(String uri) { this.uri = uri; }

    public void setCourse(String course) { this.course = course; }

    public void setEntity_name(String entity_name) {
        this.entity_name = entity_name;
    }

    public void setEntity_features(JSONArray entity_features) { this.entity_features = entity_features; }

    @NotNull
    @Override
    public String toString() {
        Gson gson = new Gson();
        String res = gson.toJson(this);
        return res;
    }
}
