/**
 * @filename Feature
 * @description 实体的 feature
 * @author ZhouYan
 * */

package com.example.catedu.data;

public class Feature {
    String feature_key;
    String feature_value;

    public Feature() {
        feature_key = "";
        feature_value = "";
    }

    public Feature(String _k, String _v) {
        this.feature_key = _k;
        this.feature_value = _v;
    }

    public String getFeature_key() {
        return feature_key;
    }

    public void setFeature_key(String feature_key) {
        this.feature_key = feature_key;
    }

    public String getFeature_value() {
        return feature_value;
    }

    public void setFeature_value(String feature_value) {
        this.feature_value = feature_value;
    }
}
