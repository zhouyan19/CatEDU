/**
 * @filename Instance
 * @description  实体类的定义
 * @author ZhouYan
 * */

package com.example.catedu.data;

import org.jetbrains.annotations.NotNull;

public class Instance {
    private String label;
    private String category;
    private String uri;

    // getters
    public String getLabel () { return label; }
    public String getCategory () { return category; }
    public String getUri () { return uri; }

    // setters
    public void setLabel (String l) { label = l; }
    public void setCategory (String c) {category = c; }
    public void setUri (String u) { uri = u; }

    @NotNull
    @Override
    public String toString() {
        return "Instance{" +
                "label='" + label + '\'' +
                ", category='" + category + '\'' +
                ", uri='" + uri + '\'' +
                '}';
    }
}
