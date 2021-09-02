/**
 * @filename Subject
 * @description Subject类，在相关实体中用到
 * @author ZhouYan
 * */


package com.example.catedu.data;

public class Subject {
    String predicate; // predicate_label
    String name; // subject_label or object_label
    String uri; // subject or object
    boolean type; // true:object, false:subject

    public Subject(String predicate, String name, String uri, boolean type) {
        this.predicate = predicate;
        this.name = name;
        this.uri = uri;
        this.type = type;
    }

    public String getPredicate() {
        return predicate;
    }

    public void setPredicate(String predicate) {
        this.predicate = predicate;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    public boolean isType() { return type; }

    public void setType(boolean type) { this.type = type; }
}
