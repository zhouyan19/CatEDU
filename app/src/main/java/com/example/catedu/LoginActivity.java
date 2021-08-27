package com.example.catedu;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class LoginActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_activity);

        final Activity main=LoginActivity.this;
        Button loginBtn=(Button) this.findViewById(R.id.login);
        EditText nameInput=(EditText) this.findViewById(R.id.name);
        EditText passwordInput=(EditText) this.findViewById(R.id.password);

        LoginListener loginL=new LoginListener(nameInput,passwordInput,this);
        loginBtn.setOnClickListener(loginL);

        TextView register=(TextView) this.findViewById(R.id.register);
        register.setOnClickListener(
                new View.OnClickListener(){
                    @Override
                    public void onClick(View view) {
                        // your code here.
                        Intent intent=new Intent(main,RegisterActivity.class);
                        startActivity(intent);
                    }
                }
        );

        ImageView show_pw=(ImageView) this.findViewById(R.id.show_password);
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
    }
}
