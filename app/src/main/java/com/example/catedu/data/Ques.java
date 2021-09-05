package com.example.catedu.data;

import android.util.Log;

public class Ques {
    String qAnswer;
    String id;
    String qBody;

    public Ques(String qAnswer, String id, String qBody) {
        this.qAnswer = qAnswer;
        this.id = id;
        this.qBody = qBody;
    }

    public String getqAnswer() { return qAnswer; }

    public void setqAnswer(String qAnswer) { this.qAnswer = qAnswer; }

    public String getId() { return id; }

    public void setId(String id) { this.id = id; }

    public String getqBody() { return qBody; }

    public void setqBody(String qBody) { this.qBody = qBody; }

    @Override
    public String toString() {
        String text = "答案：" + qAnswer + "\n";
        text += ("题目：" + qBody);
        return text;
    }

    public void print () {
        Log.e("Ques", toString());
    }
}
