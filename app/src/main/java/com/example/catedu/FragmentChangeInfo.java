package com.example.catedu;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import java.util.HashMap;
import java.util.Map;

public class FragmentChangeInfo extends Fragment {

    String item;
    TextView title_text;
    EditText modify_edt;
    TextView hint;
    Button submit_modify;
    private ImageButton backButton;

    public FragmentChangeInfo(String _item){
        this.item=_item;
    }

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_change_info, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        title_text= view.findViewById(R.id.title_text);
        modify_edt=view.findViewById(R.id.modify_edt);
        submit_modify=view.findViewById(R.id.submit_modify);
        hint=view.findViewById(R.id.modify_hint);

        backButton=(ImageButton) view.findViewById(R.id.detail_back);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                backSwitchFragment();
            }
        });


        title_text.setText("修改"+item);
        hint.setText("好"+item+"可以让人更快记住你");
        SharedPreferences sharedPreferences= getActivity().getSharedPreferences("user", Context.MODE_PRIVATE);
        String token=sharedPreferences.getString("token",null);

            submit_modify.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    String requestParam=modify_edt.getText().toString();
                    Map<String,Object> map=null;
                    NetWorkTask netWorkTask;
                    if(item.equals("昵称")) {
                        netWorkTask = new NetWorkTask(1, token, requestParam);
                    }
                    else{
                        netWorkTask = new NetWorkTask(2, token, requestParam);
                    }
                    Thread newThread= new Thread(netWorkTask);
                    newThread.start();
                    try {
                        newThread.join();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    map=netWorkTask.getMap();
                    if (map!=null)
                    {
                        boolean suc = (boolean) map.get("success");
                        String msg=(String) map.get("msg");
                        if(suc){
                            backSwitchFragment();
                            Toast.makeText(getActivity(),msg,Toast.LENGTH_LONG).show();
                        }
                        else {
                            Toast.makeText(getActivity(),msg,Toast.LENGTH_LONG).show();
                        }
                    }
                    else{
                        Toast.makeText(getActivity(),"修改失败，请重试",Toast.LENGTH_LONG).show();
                    }
                }
            });

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

    protected void backSwitchFragment() {
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
