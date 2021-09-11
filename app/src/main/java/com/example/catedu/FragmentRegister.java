package com.example.catedu;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

public class FragmentRegister extends Fragment {

    EditText username;
    ImageView username_sign;
    EditText password;
    ImageView password_sign;
    EditText confirm_password;
    ImageView confirm_password_sign;
    EditText email;
    ImageView email_sign;
    Button submit_button;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.register, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        final boolean[] input_valid=new boolean[4];

        ImageButton register_back = view.findViewById(R.id.register_back);
        register_back.setOnClickListener(v -> {
            backSwitchFragment();
        });

        username = (EditText) view.findViewById(R.id.edit_username);
        username_sign = (ImageView) view.findViewById(R.id.username_sign);
        TextWatcher username_watcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void afterTextChanged(Editable editable) {

                String pattern = "\\w{5,}";
                boolean isMatch = Pattern.matches(pattern, editable);
                input_valid[0] =isMatch;
                setImage(username_sign,isMatch);
            }
        };
        username.addTextChangedListener(username_watcher);
//
        password = (EditText) view.findViewById(R.id.edit_password);
        password_sign=(ImageView) view.findViewById(R.id.password_sign);
        password.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                String pattern = "[a-z0-9A-Z]{8,}";
                boolean isMatch = Pattern.matches(pattern, editable);
                input_valid[1]=isMatch;
                setImage(password_sign,isMatch);
            }
        });

        confirm_password = (EditText) view.findViewById(R.id.edit_confirm_password);
        confirm_password_sign=(ImageView)view.findViewById(R.id.confirm_password_sign);
        confirm_password.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                boolean isMatch=editable.toString().equals(password.getText().toString());
                input_valid[2]=isMatch;
                setImage(confirm_password_sign,isMatch);
            }
        });

        email=(EditText) view.findViewById(R.id.edit_email);
        email_sign=(ImageView) view.findViewById(R.id.email_sign);
        email.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                String pattern="^\\s*\\w+(?:\\.{0,1}[\\w-]+)*@[a-zA-Z0-9]+(?:[-.][a-zA-Z0-9]+)*\\.[a-zA-Z]+\\s*$";
                boolean isMatch=Pattern.matches(pattern,editable);
                input_valid[3]=isMatch;
                setImage(email_sign,isMatch);
            }
        });

        submit_button=(Button) view.findViewById(R.id.submit_button);
        submit_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                boolean submit_valid=true;
                for(boolean i:input_valid)
                {
                    if(!i) submit_valid=false;
                }
                if(submit_valid)
                {
                    try {
                        submit();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                else
                {
                    Toast.makeText(getActivity(),"请确认输入信息符合格式后再提交！",Toast.LENGTH_LONG).show();;
                }

            }
        });
    }


    void submit() throws InterruptedException {
        Logger.d("DEBUG","submitting...");
        String raw_username=username.getText().toString().trim();
        String raw_password=password.getText().toString().trim();
        String raw_email=email.getText().toString().trim();
        JSONObject jsonObject=new JSONObject();
        try{
            jsonObject.put("username",raw_username);
            jsonObject.put("password",raw_password);
            jsonObject.put("email",raw_email);
            jsonObject.put("selfie",1);
            jsonObject.put("nickname","我");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        String url=getActivity().getString(R.string.ip)+"/user/register";
        NetWorkTask netWorkTask=new NetWorkTask(4,null,jsonObject.toString());
        Map<String, Object> map = new HashMap<String, Object>();
        Thread newThread= new Thread(netWorkTask);
        newThread.start();
        newThread.join();
        map=netWorkTask.getMap();
        if(map!=null){
            boolean suc = (boolean) map.get("success");
            String msg = (String) map.get("msg");
            Toast.makeText(getActivity(), msg, Toast.LENGTH_SHORT).show();
            if (suc) {
                backSwitchFragment();
            }
        }
        else{
           Toast.makeText(getActivity(),"网络出错",Toast.LENGTH_LONG).show();
        }
    }
    void setImage(ImageView image,boolean isMatch)
    {
        if (isMatch) {
            image.setImageResource(R.drawable.true_mark);
            image.setVisibility(View.VISIBLE);
        } else {
            image.setImageResource(R.drawable.false_mark);
            image.setVisibility(View.VISIBLE);
        }
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
