package com.example.catedu;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.regex.Pattern;

public class RegisterActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
//        Logger.e("ERROR","register");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.register);

        final boolean[] input_valid=new boolean[4];
        EditText username = (EditText) this.findViewById(R.id.edit_username);
        ImageView username_sign = (ImageView) this.findViewById(R.id.username_sign);
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
        EditText password = (EditText) this.findViewById(R.id.edit_password);
        ImageView password_sign=(ImageView) this.findViewById(R.id.password_sign);
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

        EditText confirm_password = (EditText) this.findViewById(R.id.edit_confirm_password);
        ImageView confirm_password_sign=(ImageView) this.findViewById(R.id.confirm_password_sign);
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

        EditText email=(EditText) this.findViewById(R.id.edit_email);
        ImageView email_sign=(ImageView) this.findViewById(R.id.email_sign);
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

        Button submit_button=(Button) this.findViewById(R.id.submit_button);
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
        Toast.makeText(RegisterActivity.this,"提交成功！",Toast.LENGTH_LONG).show();;
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
