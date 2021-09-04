/**
 * @filename Instance
 * @description 实体类（缩略）
 * @author ZhouYan
 * */

package com.example.catedu.data;

public class Instance {
    private String name;
    private String type;

    public Instance(String _n, String _t) {
        name = _n;
        type = _t;
    }

    public Instance() {
        name = "";
        type = "";
    }

    // getters
    public String getName () { return name; }
    public String getType () { return type; }

    // setters
    public void setName (String _n) { name = _n; }
    public void setType (String _t) { type = _t; }
}
