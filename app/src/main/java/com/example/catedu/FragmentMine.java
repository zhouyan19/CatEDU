/**
 * @filename FragmentMine
 * @description  我的组件 (3)
 * @author ZhouYan
 * */

package com.example.catedu;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
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

import org.json.JSONException;
import org.json.JSONObject;

public class FragmentMine extends Fragment {

    /**
     * FragmentMine 创建时的操作
     */
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_mine, container, false);
    }

    public void refreshView(View view,String token) {
        if (token != null) {
            ImageView selfie = (ImageView) view.findViewById(R.id.selfie);
            TextView nickname_view = (TextView) view.findViewById(R.id.login_textView);
            String url = "http://183.173.179.9:8080/user/info";
            JSONObject jsonObject = new JSONObject();
            try {
                jsonObject.put("token", token);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            RequestQueue requestQueue = Volley.newRequestQueue(this.getActivity());
            JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, url, jsonObject, new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject jsonObject) {
                    try {
                        Logger.d("信息", jsonObject.toString());
                        String msg = jsonObject.getString("msg");
                        boolean suc = jsonObject.getBoolean("success");
                        Logger.d("msg", msg);
                        if (suc) {
                            JSONObject detail = jsonObject.getJSONObject("detail");
                            String nickname = detail.getString("nickname");
                            int selfie_num = detail.getInt("selfie");
                            String image_name = "selfie_" + String.valueOf(selfie_num) + ".png";
                            int src = getResources().getIdentifier(image_name, "drawable", getActivity().getPackageName());
                            selfie.setImageResource(src);
                            nickname_view.setText(nickname);
                        } else {
                            Toast.makeText(getActivity(), "获取个人信息失败", Toast.LENGTH_LONG).show();
                        }


                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError volleyError) {

                    Toast.makeText(getActivity(), "网络出错" + volleyError.toString(), Toast.LENGTH_SHORT).show();
                }
            });
            requestQueue.add(jsonObjectRequest);
        }
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {

        SharedPreferences sharedPreferences= getActivity().getSharedPreferences("token", Context.MODE_PRIVATE);
        String token=sharedPreferences.getString("token",null);

        int refIds[] ={R.id.selfie,R.id.login_textView};
        for (int id : refIds) {
            view.findViewById(id).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    // your code here.
                    FragmentLogin fid = new FragmentLogin();
                    MainActivity.fragments.add(fid);
                    forwardSwitchFragment();
                }
            });
        }
        refreshView(view,token);
        Button clear_backup=(Button) view.findViewById(R.id.clear_backup);
        clear_backup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sharedPreferences.edit().clear();
                sharedPreferences.edit().apply();
                Toast.makeText(getActivity(),"缓存清除成功",Toast.LENGTH_LONG).show();
            }
        });
        super.onViewCreated(view, savedInstanceState);
    }
    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged( hidden );
        if (hidden) {// 不在最前端界面显示
        } else {// 重新显示到最前端中
            String token=getActivity().getSharedPreferences("token", Context.MODE_PRIVATE).getString("token",null);
            refreshView(getView(),token);
        }
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
}
