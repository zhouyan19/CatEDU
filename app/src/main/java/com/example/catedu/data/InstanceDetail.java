/**
 * @filename InstanceDetail
 * @description 实体类（详细）
 * @author ZhouYan
 * */

package com.example.catedu.data;

import org.json.JSONArray;

public class InstanceDetail {
    String entity_type;
    String entity_name;
    JSONArray entity_features;

    public InstanceDetail() {
        entity_type = "";
        entity_name = "";
        entity_features = new JSONArray();
    }

    InstanceDetail (String _t, String _n, JSONArray _f) {
        entity_type = _t;
        entity_name = _n;
        entity_features = _f;
    }

    public String getEntity_type() {
        return entity_type;
    }

    public String getEntity_name() {
        return entity_name;
    }

    public JSONArray getEntity_features() {
        return entity_features;
    }

    public void setEntity_type(String entity_type) {
        this.entity_type = entity_type;
    }

    public void setEntity_name(String entity_name) {
        this.entity_name = entity_name;
    }

    public void setEntity_features(JSONArray entity_features) {
        this.entity_features = entity_features;
    }
}
