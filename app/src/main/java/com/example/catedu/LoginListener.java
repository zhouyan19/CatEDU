package com.example.catedu;

import android.app.Activity;
import android.content.Intent;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

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
        String name=nameInput.getText().toString();
        String password=passwordInput.getText().toString();
        if(name.equals("admin")&&password.equals("123456"))
        {
            Logger.e("login","success!");
            Toast.makeText(main,"登录成功",Toast.LENGTH_LONG).show();
            Intent intent=new Intent();
            intent.setClass(main,ProgressBarActivity.class);
            main.startActivity(intent);
        }
        else
        {
            Toast.makeText(main,"信息错误",Toast.LENGTH_LONG).show();
        }
    }

}
