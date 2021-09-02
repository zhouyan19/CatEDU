package com.example.catedu.data;

public class InstanceWithUri {
    private String name;
    private String type;
    private String uri;

    public InstanceWithUri(String _n, String _t, String _u) {
        name = _n;
        type = _t;
        uri = _u;
    }

    public InstanceWithUri() {
        name = "无名称";
        type = "无类别";
        uri = "null uri";
   }

    // getters
    public String getName () { return name; }
    public String getType () { return type; }
    public String getUri () { return uri; }

    // setters
    public void setName (String _n) { name = _n; }
    public void setType (String _t) { type = _t; }
    public void setUri (String _t) { type = _t; }
}
