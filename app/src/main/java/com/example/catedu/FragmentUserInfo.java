package com.example.catedu;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.example.catedu.widget.ItemGroup;
import com.google.gson.internal.LinkedTreeMap;

import java.util.Map;

public class FragmentUserInfo extends Fragment {
    ImageView selfie_to_change;
    ImageButton backButton;
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_userinfo, container, false);
    }
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {

        selfie_to_change= (ImageView) view.findViewById(R.id.selfie_to_change);
        backButton=(ImageButton) view.findViewById(R.id.detail_back);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                backSwitchFragment();
            }
        });
        try {
            refreshView(getView());
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    void refreshView(View view) throws InterruptedException {
        ItemGroup item_nickname=view.findViewById(R.id.item_nickname);
        ItemGroup item_email=view.findViewById(R.id.item_email);
        Logger.e("fui","refresh");
        Thread itemGroupThread=new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    item_nickname.initAttrs();
                    item_email.initAttrs();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
        itemGroupThread.start();
        itemGroupThread.join();

        String url=getResources().getString(R.string.ip)+"/user/info";
        SharedPreferences sharedPref = getContext().getSharedPreferences("user", Context.MODE_PRIVATE);
        String token= sharedPref.getString("token",null);
        Map<String,Object> map=null;
        NetWorkTask netWorkTask=new NetWorkTask(url,"token",token);
        Thread newThread= new Thread(netWorkTask);
        newThread.start();
        newThread.join();
        map=netWorkTask.getMap();
        if(map!=null)
        {
            LinkedTreeMap linkedTreeMap=(LinkedTreeMap) map.get("detail");
            Double selfie_num = (Double) linkedTreeMap.get("selfie");
            String image_name = "avatar_icon_" + String.valueOf(selfie_num.intValue()) ;
            int src = getActivity().getResources().getIdentifier(image_name, "drawable", getActivity().getPackageName());
            selfie_to_change.setImageResource(src);
        }
        LinearLayout ll_portrait=(LinearLayout) view.findViewById(R.id.ll_portrait);
        ll_portrait.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MainActivity.fragments.add(new FragmentAvatar());
                forwardSwitchFragment();
            }
        });

    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged( hidden );
        if (hidden) {// 不在最前端界面显示
        } else {// 重新显示到最前端中
            try {
                refreshView(getView());
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
    protected void forwardSwitchFragment() {
        int from = MainActivity.last_fragment, to = MainActivity.fragments.size() - 1;
        FragmentTransaction transaction =requireActivity().getSupportFragmentManager().beginTransaction();
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
