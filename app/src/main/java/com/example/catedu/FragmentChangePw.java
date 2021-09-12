package com.example.catedu;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.alibaba.fastjson.JSONObject;

//import org.json.JSONException;
//import org.json.JSONObject;


public class FragmentChangePw extends Fragment {
    Button submit;
    EditText old_pw_edt;
    EditText new_pw_edt;
    EditText confirm_new_pw_edt;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_change_pw, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        ImageButton pw_back = view.findViewById(R.id.pw_back);
        pw_back.setOnClickListener(v -> {
            backSwitchFragment();
        });

        submit = view.findViewById(R.id.submit_pw);
        old_pw_edt=(EditText) view.findViewById(R.id.old_pw_edt);
        new_pw_edt=(EditText) view.findViewById(R.id.new_pw_edt);
        confirm_new_pw_edt=(EditText) view.findViewById(R.id.confirm_pw_edt);
        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String old_pw = old_pw_edt.getText().toString();
                String new_pw = new_pw_edt.getText().toString();
                String confirm_pw = confirm_new_pw_edt.getText().toString();
                if (!new_pw.equals(confirm_pw)) {
                    Toast.makeText(getActivity(), "两次输入密码不一致", Toast.LENGTH_SHORT).show();
                } else {
                    SharedPreferences sharedPreferences = getActivity().getSharedPreferences("user", Context.MODE_PRIVATE);
                    String token = sharedPreferences.getString("token", null);
                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put("oldPassword", old_pw);
                    jsonObject.put("newPassword", new_pw);
                    jsonObject.put("token",token);


                    NetWorkTask netWorkTask = new NetWorkTask(8, token, jsonObject.toString());
                    Thread newThread = new Thread(netWorkTask);
                    newThread.start();
                    try {
                        newThread.join();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    String res = netWorkTask.getRes();
                    JSONObject resJson = JSONObject.parseObject(res);
                    Logger.e("FCP62",resJson.toString());
//                    JSONArray jsonArray = (JSONArray) resJson.get("detail");
                    boolean suc=resJson.getBoolean("success");
                    String msg=resJson.getString("msg");
                    if(msg!=null)
                        Toast.makeText(getActivity(), msg, Toast.LENGTH_SHORT).show();
                    if(suc)
                        backSwitchFragment();

                }
            }
        });
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
