package com.example.catedu;

import android.app.Activity;
import android.content.Intent;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

public class LoginListener implements View.OnClickListener {
    EditText nameInput;
    EditText passwordInput;
    Activity main;
    public LoginListener(EditText nameInput, EditText password, Activity main)
    {
        super();
        this.nameInput=nameInput;
        this.main=main;
        this.passwordInput=password;
    }

    @Override
    public void onClick(View v)
    {
        String name=nameInput.getText().toString().trim();
        String password=passwordInput.getText().toString().trim();
        if(name.equals("")||password.equals("")) {
            Toast.makeText(main.getApplicationContext(), "用户名密码不能为空", Toast.LENGTH_SHORT).show();
        }
        else {
            JSONObject jsonObject=new JSONObject();
            try {
                jsonObject.put("username",name);
                jsonObject.put("password",password);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            String url="http://183.173.179.9:8080/user/login";
            RequestQueue requestQueue= Volley.newRequestQueue(main.getApplicationContext());
            JsonObjectRequest jsonObjectRequest=new JsonObjectRequest(Request.Method.POST, url,jsonObject, new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject jsonObject) {
                    try {
                        Logger.d("信息", jsonObject.toString());
                        String msg = jsonObject.getString("msg");
                        Logger.d("msg", msg);
                        if(msg.equals("登录成功")){

                            JSONObject detail = jsonObject.getJSONObject("detail");
                            String username = detail.getString("username");
                            Intent intent=new Intent(main,MainActivity.class);
                            main.startActivity(intent);
                            Toast.makeText(main,"登录成功",Toast.LENGTH_LONG).show();
                        }else if(msg.equals("用户名或密码错误")){
                            Toast.makeText(main, "用户名密码有误", Toast.LENGTH_SHORT).show();
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError volleyError) {
                    Toast.makeText(main, "网络出错", Toast.LENGTH_SHORT).show();
                }
            });
            requestQueue.add(jsonObjectRequest);
        }
    }

}
