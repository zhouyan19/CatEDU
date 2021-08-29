package com.example.catedu;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.gson.Gson;
import com.google.gson.internal.LinkedTreeMap;

import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class FragmentLogin extends Fragment {

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_login, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {

        Button loginBtn=(Button) view.findViewById(R.id.login);
        EditText nameInput=(EditText) view.findViewById(R.id.name);
        EditText passwordInput=(EditText) view.findViewById(R.id.password);


        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String name=nameInput.getText().toString().trim();
                String password=passwordInput.getText().toString().trim();
                if(name.equals("")||password.equals("")) {
                    Toast.makeText(getActivity(), "用户名密码不能为空", Toast.LENGTH_SHORT).show();
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
                    Document doc = null;
                    try {
                        doc = Jsoup.connect(url).data((Collection<Connection.KeyVal>) jsonObject).ignoreContentType(true).post();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    Element body=doc.body();
                    String str=body.text();
                    Gson gson=new Gson();
                    Map<String, Object> map = new HashMap<String, Object>();
                    map = gson.fromJson(str, map.getClass());
                    System.out.println(map.get("detail"));
                    LinkedTreeMap linkedTreeMap=(LinkedTreeMap) map.get("detail");
                    System.out.println(linkedTreeMap.get("nickname"));
                    RequestQueue requestQueue= Volley.newRequestQueue(getActivity());
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
                                    SharedPreferences sharedPref = getActivity().getSharedPreferences("token", Context.MODE_PRIVATE);
                                    SharedPreferences.Editor editor=sharedPref.edit();
                                    editor.putString("token",jsonObject.getString("token"));
                                    editor.apply();
                                    backSwitchFragment();
                                    Toast.makeText(getActivity(),"登录成功",Toast.LENGTH_LONG).show();
                                }else if(msg.equals("用户名或密码错误")){
                                    Toast.makeText(getActivity(), "用户名密码有误", Toast.LENGTH_SHORT).show();
                                }

                            } catch (JSONException e) {
                                e.printStackTrace();
                            }

                        }
                    }, new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError volleyError) {
                            Toast.makeText(getActivity(), "网络出错", Toast.LENGTH_SHORT).show();
                        }
                    });
                    requestQueue.add(jsonObjectRequest);
            }
        }});
        TextView register = (TextView) view.findViewById(R.id.register);
        register.setOnClickListener(
                new View.OnClickListener(){
                    @Override
                    public void onClick(View view) {
                        // your code here.
                        MainActivity.fragments.add(new FragmentRegister());
                        forwardSwitchFragment();
                    }
                }
        );

        ImageView show_pw=(ImageView) view.findViewById(R.id.show_password);
        show_pw.setOnClickListener(new View.OnClickListener() {
            boolean show=false;

            @Override
            public void onClick(View view) {
                if(this.show==true)
                {
//                  原来是显示，变为不显示
                    show=false;
                    show_pw.setImageResource(R.drawable.hide_password);
                    passwordInput.setTransformationMethod(PasswordTransformationMethod.getInstance());
                }
                else
                {
                    show=true;
                    show_pw.setImageResource(R.drawable.show_password);
                    passwordInput.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                }
//                this.show=!this.show;
            }
        });

        InputMethodManager inputMethodManager =(InputMethodManager)getActivity().getApplicationContext().
                getSystemService(Context.INPUT_METHOD_SERVICE);

        inputMethodManager.hideSoftInputFromWindow(loginBtn.getWindowToken(), 0);
        super.onViewCreated(view,savedInstanceState);
    }

    protected void forwardSwitchFragment() {
        int from = MainActivity.last_fragment, to = MainActivity.fragments.size() - 1;
        FragmentTransaction transaction = requireActivity().getSupportFragmentManager().beginTransaction();
        transaction.hide(MainActivity.fragments.get(from));
        if (!MainActivity.fragments.get(to).isAdded())
            transaction.add(R.id.nav_host_fragment, MainActivity.fragments.get(to));
        transaction.show(MainActivity.fragments.get(to)).commitAllowingStateLoss();
        MainActivity.last_fragment = to; // 更新
    }

    public void backSwitchFragment() {
        int from = MainActivity.last_fragment, to;
        FragmentTransaction transaction = requireActivity().getSupportFragmentManager().beginTransaction();
        transaction.hide(MainActivity.fragments.get(from));
        if (MainActivity.last_fragment == 3) { //次级页面
            to = MainActivity.major_fragment;
        } else { //多级页面
            to = MainActivity.last_fragment - 1;
        }
        if (!MainActivity.fragments.get(to).isAdded())
            transaction.add(R.id.nav_host_fragment, MainActivity.fragments.get(to));
        transaction.show(MainActivity.fragments.get(to)).commitAllowingStateLoss();
        MainActivity.last_fragment = to; //更新
        MainActivity.fragments.removeElementAt(from); //删多余的页面
    }
}
