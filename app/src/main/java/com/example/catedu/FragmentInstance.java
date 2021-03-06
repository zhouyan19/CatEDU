package com.example.catedu;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.alibaba.fastjson.JSONObject;
import com.androidkun.xtablayout.XTabLayout;
import com.github.ybq.android.spinkit.SpinKitView;

import org.json.JSONException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.IOException;
import java.util.HashMap;
import java.util.Objects;
import java.util.Vector;

public class FragmentInstance extends Fragment {
    public static String uri; // 实体uri
    public static String name; // 实体名称
    public static String course; // 学科名称
    public static Vector<Fragment> fragments;
    public static int last_fragment;
    public static int loaded;
    public static boolean liked;

    FragmentInsDetail fragment_ins_detail;
    FragmentInsQues fragment_ins_ques;
    FragmentInsRelated fragment_ins_related;

    ImageButton back_home;
    public static SpinKitView skv;
    XTabLayout detail_tabLayout;

    ImageButton more_op;
    CustomPopWindow pop_window;

    AlertDialog alert;
    AlertDialog.Builder builder;

    ImageButton shareBtn;
    ImageButton likeBtn;
    TextView cancelTv;

    public FragmentInstance (String _u, String _n, String _c) {
        Log.e("FragmentInstance", "New!");
        uri = _u;
        name = _n;
        course = _c;
        loaded = 0;
        last_fragment = 0;
        liked = false;
    }

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // 绑定 layout
        return inflater.inflate(R.layout.fragment_instance, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        skv = view.findViewById(R.id.spin_kit);
        skv.setVisibility(View.VISIBLE);

        detail_tabLayout = view.findViewById(R.id.detail_tab_layout);

        back_home = view.findViewById(R.id.detail_back_home);
        back_home.setOnClickListener(v -> {
            try {
                backSwitchFragment();
            } catch (Throwable throwable) {
                throwable.printStackTrace();
            }
        });

        more_op = view.findViewById(R.id.more_op);
        more_op.setOnClickListener(v -> {
            pop_window = new CustomPopWindow();
            pop_window.showAtLocation(v,
                    Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL, 0, 0);
            pop_window.setOnDismissListener(() -> pop_window.backgroundAlpha(1f));
        });

        fragment_ins_detail = new FragmentInsDetail(uri, name, course, false);
        fragment_ins_ques = new FragmentInsQues(name);
        fragment_ins_related = new FragmentInsRelated(name, course);
        fragments = new Vector<>();

        fragments.add(fragment_ins_detail);
        detail_tabLayout.addTab(detail_tabLayout.newTab().setText("详情"));

        fragments.add(fragment_ins_ques);
        detail_tabLayout.addTab(detail_tabLayout.newTab().setText("试题"));

        fragments.add(fragment_ins_related);
        detail_tabLayout.addTab(detail_tabLayout.newTab().setText("相关"));

        detail_tabLayout.setOnTabSelectedListener(new XTabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(XTabLayout.Tab tab) {
                int id = tab.getPosition();
                switchFragment(last_fragment, id);
                Log.e("Tab", (String) tab.getText());
            }

            @Override
            public void onTabUnselected(XTabLayout.Tab tab) {}

            @Override
            public void onTabReselected(XTabLayout.Tab tab) {}
        });

        checkLike();

        getChildFragmentManager()
                .beginTransaction()
                .replace(R.id.detail_fragment_container, fragment_ins_detail)
                .show(fragment_ins_detail).commit();
    }

    private void switchFragment (int last, int index) {
        FragmentTransaction transaction = getChildFragmentManager().beginTransaction();
        transaction.hide(fragments.get(last));
        if (!fragments.get(index).isAdded())
            transaction.add(R.id.detail_fragment_container, fragments.get(index));
        transaction.show(fragments.get(index)).commitAllowingStateLoss();
        last_fragment = index;
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

    @SuppressLint("HandlerLeak")
    public static Handler mHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage (Message msg) {
            super.handleMessage(msg);
            loaded++;
            if (loaded >= 3) {
                Log.e("Loaded", String.valueOf(loaded));
                skv.setVisibility(View.GONE);
            }
        }
    };

    public class CustomPopWindow extends PopupWindow {
        private final View view;

        @SuppressLint("InflateParams")
        public CustomPopWindow() {
            super();
            LayoutInflater inflater = (LayoutInflater) requireContext().getSystemService(getContext().LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.widget_popupwindow, null);
            initView();
            initPopWindow();
        }

        private void initView() {
            shareBtn = view.findViewById(R.id.button_share);
            likeBtn = view.findViewById(R.id.button_like);
            cancelTv = view.findViewById(R.id.share_cancel);

            shareBtn.setOnClickListener(v -> {
                try {
                    doShareDetail();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            });

            if (liked) likeBtn.setImageResource(R.mipmap.like_yes);
            else likeBtn.setImageResource(R.mipmap.like_no);

            likeBtn.setOnClickListener(v -> {
                SharedPreferences sharedPreferences = requireActivity().getSharedPreferences("user", Context.MODE_PRIVATE);
                String token = sharedPreferences.getString("token",null);
                if (token == null) { // 未登录
                    Toast.makeText(getActivity(), "请先登录！", Toast.LENGTH_SHORT).show();
                } else {
                    if (liked) { // 取消收藏
                        removeLike(token);
                    } else { // 添加收藏
                        addToLike(token);
                    }
                }
            });

            cancelTv.setOnClickListener(v -> dismiss());

        }

        private void initPopWindow() {
            this.setContentView(view);
            // 设置弹出窗体的宽
            this.setWidth(ViewGroup.LayoutParams.MATCH_PARENT);
            // 设置弹出窗体的高
            this.setHeight(ViewGroup.LayoutParams.WRAP_CONTENT);
            // 设置弹出窗体可点击()
            this.setFocusable(true);
            this.setOutsideTouchable(true);
            //设置SelectPicPopupWindow弹出窗体动画效果
            this.setAnimationStyle(R.style.mypopwindow_anim_style);
            ColorDrawable dw = new ColorDrawable(0x00FFFFFF);
            //设置弹出窗体的背景
            this.setBackgroundDrawable(dw);
            backgroundAlpha(0.5f); //0.0-1.0
        }

        public void backgroundAlpha(float bgAlpha) {
            WindowManager.LayoutParams lp = requireActivity().getWindow().getAttributes();
            lp.alpha = bgAlpha;
            requireActivity().getWindow().addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
            requireActivity().getWindow().setAttributes(lp);
        }
    }

    public void doShareDetail () throws JSONException {
        String text = fragment_ins_detail.getText();
        String picUrl = fragment_ins_detail.getPicUrl();
        MainActivity main = (MainActivity) getActivity();
        assert main != null;
        LinearLayout share_pop = (LinearLayout) getLayoutInflater().inflate(R.layout.share_popupwindow, null);
        share_pop.setBackgroundResource(R.drawable.pop_border);
        TextView share_summary = share_pop.findViewById(R.id.share_summary);
        TextView cancel = share_pop.findViewById(R.id.share_cancel);
        TextView confirm = share_pop.findViewById(R.id.share_confirm);

        String summary;
        if (text.length() > 100) summary = text.substring(0, 50) + "......";
        else summary = text;
        share_summary.setText(summary);
        alert = null;
        builder = new AlertDialog.Builder(getContext());;
        builder.setView(share_pop);
        alert = builder.create();

        cancel.setOnClickListener(v -> {
            alert.dismiss();
        });
        confirm.setOnClickListener(v -> {
            main.doWeiboShare(text, picUrl);
            alert.dismiss();
            pop_window.dismiss();
        });

        alert.show();

    }

    public void addToLike (String token) {
        JSONObject body = new JSONObject();
        body.put("token", token);
        body.put("insUri", uri);
        body.put("detail", fragment_ins_detail.getInsString());
        String request_url = "http://82.156.215.178:8080/user/addStars";
        HashMap<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");
        new Thread (() -> {
            Document doc = null;
            try {
                doc = Jsoup.connect(request_url).headers(headers).requestBody(body.toString()).ignoreContentType(true).post();
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (doc != null) {
                Element content = doc.body();
                String str = content.text();
                JSONObject res = JSONObject.parseObject(str);
                Log.e("addToLike", res.toString());
                if (res.containsKey("success") || res.getString("success").equals("true")) {
                    liked = true;
                    likeBtn.setImageResource(R.mipmap.like_yes);
                    requireActivity().runOnUiThread(() -> Toast.makeText(getActivity(), "添加收藏成功", Toast.LENGTH_SHORT).show());
                } else {
                    requireActivity().runOnUiThread(() -> Toast.makeText(getActivity(), "网络请求错误", Toast.LENGTH_SHORT).show());
                }
            }
        }).start();
    }

    public void removeLike (String token) {
        JSONObject body = new JSONObject();
        body.put("token", token);
        body.put("insUri", uri);
        body.put("detail", fragment_ins_detail.getInsString());
        String request_url = "http://82.156.215.178:8080/user/rmStars";
        HashMap<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");
        new Thread (() -> {
            Document doc = null;
            try {
                doc = Jsoup.connect(request_url).headers(headers).requestBody(body.toString()).ignoreContentType(true).post();
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (doc != null) {
                Element content = doc.body();
                String str = content.text();
                JSONObject res = JSONObject.parseObject(str);
                Log.e("removeLike", res.toString());
                if (res.containsKey("success") || res.getString("success").equals("true")) {
                    liked = false;
                    likeBtn.setImageResource(R.mipmap.like_no);
                    requireActivity().runOnUiThread(() -> Toast.makeText(getActivity(), "取消收藏成功", Toast.LENGTH_SHORT).show());
                } else {
                    requireActivity().runOnUiThread(() -> Toast.makeText(getActivity(), "网络请求错误", Toast.LENGTH_SHORT).show());
                }
            }
        }).start();
    }

    public void checkLike () {
        SharedPreferences sharedPreferences = requireActivity().getSharedPreferences("user", Context.MODE_PRIVATE);
        String token = sharedPreferences.getString("token",null);
        if (token != null) {
            JSONObject body = new JSONObject();
            body.put("token", token);
            body.put("insUri", uri);
            String request_url = "http://82.156.215.178:8080/user/searchStars";
            HashMap<String, String> headers = new HashMap<>();
            headers.put("Content-Type", "application/json");
            new Thread (() -> {
                Document doc = null;
                try {
                    doc = Jsoup.connect(request_url).headers(headers).requestBody(body.toString()).ignoreContentType(true).post();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                if (doc != null) {
                    Element content = doc.body();
                    String str = content.text();
                    JSONObject res = JSONObject.parseObject(str);
                    Log.e("checkLike", res.toString());
                    if (res.containsKey("success")) {
                        if (res.getString("success").equals("true")) {
                            liked = true;
                        } else {
                            liked = false;
                        }
                    }
                }
            }).start();
        }
    }

}
