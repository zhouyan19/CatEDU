package com.example.catedu;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DecodeFormat;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.target.Target;

import org.json.JSONException;

import java.io.IOException;
import java.util.ArrayList;


/**
 * A simple {@link Fragment} subclass.
 * Use the {@link FragmentWordCloud#newInstance} factory method to
 * create an instance of this fragment.
 */
public class FragmentWordCloud extends Fragment {
    ImageButton back_home;
    ImageView imageView;
    RadioGroup colorGroup;
    CheckBox darkBox;
    private int colorMode = 0;
    private boolean isDark = false;


    public FragmentWordCloud() {
        // Required empty public constructor
    }


    public static FragmentWordCloud newInstance(String param1, String param2) {
        FragmentWordCloud fragment = new FragmentWordCloud();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_word_cloud, container, false);
    }

    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        back_home = view.findViewById(R.id.detail_back_home);
        back_home.setOnClickListener(v -> {
            try {
                backSwitchFragment();
            } catch (Throwable throwable) {
                throwable.printStackTrace();
            }
        });

        imageView = view.findViewById(R.id.cloud_view);
        colorGroup = view.findViewById(R.id.color_group);
        darkBox = view.findViewById(R.id.dark_box);
        colorGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int id) {
                colorMode = Id2ColorMode(id);
                showCloud();
            }
        });
        darkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                isDark = isChecked;
                showCloud();
            }
        });
        showCloud();
    }


    public void showCloud() {
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("user", Context.MODE_PRIVATE);
        String token = sharedPreferences.getString("token", null);
        if (token != null) {
            new Thread(() -> {
                String cloudURL = getCloudURL(token);
                requireActivity().runOnUiThread(() -> {
                    Glide.with(requireContext())
                            .load("http://82.156.215.178:8080" + "/img" + cloudURL)
                            .centerCrop()
                            .dontTransform()
                            .dontAnimate()
                            .format(DecodeFormat.PREFER_ARGB_8888)
                            .diskCacheStrategy(DiskCacheStrategy.ALL)
                            .override(Target.SIZE_ORIGINAL, Target.SIZE_ORIGINAL)
                            .into(imageView);
                });
            }).start();
        } else {
            imageView.setImageResource(R.drawable.not_login_tip);
        }
    }

    private String getCloudURL(String token) {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("token", token);
            jsonObject.put("colorStyle", colorMode);
            jsonObject.put("dark", isDark);
            NetWorkTask netWorkTask = new NetWorkTask(7, token, jsonObject.toString());
            Thread newThread = new Thread(netWorkTask);
            newThread.start();
            try {
                newThread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            String res = netWorkTask.getRes();
            JSONObject resJson = JSONObject.parseObject(res);
            Log.e("cloud return", resJson.toString());
//            JSONArray jsonArray = (JSONArray) resJson.get("detail");
            return resJson.getString("detail");
    }

    private int Id2ColorMode(int id) {
        switch (id) {
            case R.id.orange:
                return 0;
            case R.id.blue:
                return 1;
            case R.id.green:
                return 2;
            case R.id.purple:
                return 3;
            default:
                return -1;
        }
    }

    protected void backSwitchFragment() throws Throwable {
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
        finalize();
    }
}