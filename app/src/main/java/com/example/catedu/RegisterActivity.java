package com.example.catedu;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.gson.JsonObject;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.regex.Pattern;

public class RegisterActivity extends AppCompatActivity {

    EditText username;
    ImageView username_sign;
    EditText password;
    ImageView password_sign;
    EditText confirm_password;
    ImageView confirm_password_sign;
    EditText email;
    ImageView email_sign;
    Button submit_button;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
//        Logger.e("ERROR","register");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.register);

        final boolean[] input_valid=new boolean[4];

        username = (EditText) this.findViewById(R.id.edit_username);
        username_sign = (ImageView) this.findViewById(R.id.username_sign);
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
        password = (EditText) this.findViewById(R.id.edit_password);
        password_sign=(ImageView) this.findViewById(R.id.password_sign);
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

        confirm_password = (EditText) this.findViewById(R.id.edit_confirm_password);
        confirm_password_sign=(ImageView) this.findViewById(R.id.confirm_password_sign);
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

        email=(EditText) this.findViewById(R.id.edit_email);
        email_sign=(ImageView) this.findViewById(R.id.email_sign);
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

        submit_button=(Button) this.findViewById(R.id.submit_button);
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
                    submit();
                }
                else
                {
                    Toast.makeText(RegisterActivity.this,"请确认输入信息符合格式后再提交！",Toast.LENGTH_LONG).show();;
                }

            }
        });


    }

    void submit()
    {
        Logger.d("DEBUG","submitting...");
        String raw_username=username.getText().toString().trim();
        String raw_password=password.getText().toString().trim();
        String raw_email=email.getText().toString().trim();
        JSONObject jsonObject=new JSONObject();
        try{
            jsonObject.put("username",raw_username);
            jsonObject.put("password",raw_password);
            jsonObject.put("email",raw_email);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        String url="http://183.173.179.9:8080/user/register";
        RequestQueue requestQueue = Volley.newRequestQueue(RegisterActivity.this);
        JsonObjectRequest jsonObjectRequest=new JsonObjectRequest(Request.Method.POST, url, jsonObject, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    Logger.d("DEBUG", jsonObject.toString());
                    String msg = response.getString("msg");
                    Toast.makeText(RegisterActivity.this, msg, Toast.LENGTH_LONG).show();
                    if(msg.equals("注册成功")){
//                        JSONObject detail =jsonObject.getJSONObject("detail");
//                        final String username_login=detail.getString("username");
                        Intent intent=new Intent(RegisterActivity.this,LoginActivity.class);
                        startActivity(intent);
                    }

                } catch (JSONException e) {
                    Toast.makeText(RegisterActivity.this,e.getMessage().toString(),Toast.LENGTH_LONG).show();
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(RegisterActivity.this,"网络出错",Toast.LENGTH_LONG).show();
                Logger.d("DEBUG","Register connection failed");
            }
        });
        requestQueue.add(jsonObjectRequest);
//        Toast.makeText(RegisterActivity.this,"提交成功！",Toast.LENGTH_LONG).show();;
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
}
