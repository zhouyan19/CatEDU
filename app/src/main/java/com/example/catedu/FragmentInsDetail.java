/**
 * @filename FragmentInsDetail
 * @description  实体详情页
 * @author ZhouYan
 * */

package com.example.catedu;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DecodeFormat;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.target.Target;
import com.daquexian.flexiblerichtextview.FlexibleRichTextView;
import com.example.catedu.data.PicSpider;
import com.example.catedu.data.InstanceDetail;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Vector;


public class FragmentInsDetail extends Fragment {
    public static String uri; // 实体uri
    public static String name; // 实体名称
    public static String course; // 学科名称
    private static InstanceDetail instance;
    private static String picUrl;
    private boolean independence; // true表示不依附FragmentInstance

    private static Vector<JSONObject> feature_list;

    FlexibleRichTextView detail_name;
    RecyclerView detail_feature;
    ImageView entity_pic;

    ImageButton back_home;

    FragmentInsDetail (String _u, String _n, String _c, boolean _i) {
        Log.e("FragmentInsDetail", "New!");
        uri = _u;
        name = _n;
        course = _c;
        independence = _i;
        instance = new InstanceDetail();
        feature_list = new Vector<>();
        picUrl = "";
    }

    /**
     * FragmentInsDetail 创建时的操作
     */
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // 绑定 layout
        View view;
        if (!independence) view = inflater.inflate(R.layout.fragment_ins_detail, container, false);
        else view = inflater.inflate(R.layout.fragment_head_detail, container, false);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        if (independence) {
            back_home = view.findViewById(R.id.detail_back_home);
//            Logger.e("FD","onclick1");
            back_home.setOnClickListener(v -> {
//                Logger.e("FD","onclick2");
                try {
                    backSwitchFragment();
                } catch (Throwable throwable) {
                    throwable.printStackTrace();
                }
            });
        }

        detail_name = view.findViewById(R.id.detail_name);
        detail_feature = view.findViewById(R.id.detail_feature);
        detail_feature.addItemDecoration(new DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL));
        detail_feature.setLayoutManager(new LinearLayoutManager(getContext()));

        entity_pic = view.findViewById(R.id.entity_pic);

        getInstanceDetail();

        setPic();
    }

    @SuppressLint("SetTextI18n")
    public void getInstanceDetail () {
        new Thread(() -> {
            try {
                new Response().handle(ins -> {
                    instance = ins;
                    MainActivity main = (MainActivity) getActivity();
                    assert main != null;
                    main.addSeen(uri, ins);
                    Log.e("InsDetail", "Add Seen");

                    addHistory();

                    String name = "实体名称：" + ((instance.getEntity_name().equals("")) ? "无" : instance.getEntity_name());
                    requireActivity().runOnUiThread(() -> {
                        detail_name.setText(name);
                        JSONArray features = ins.getEntity_features();
                        try {
                            for (int i = 0; i < features.length(); ++i) {
                                JSONObject feature = features.getJSONObject(i);
                                String value = null;
                                if (feature.has("object")) {
                                    value = feature.getString("object");
                                } else if (feature.has("subject")) {
                                    value = feature.getString("subject");
                                } else {
                                    value = "";
                                }
                                if (value.contains("http://www.w3")
                                        || value.contains("http://webprotege")
                                        || value.contains("http://kb.cs.tsinghua.edu.cn/apibztask")
                                        || value.contains("http://edukg.")
                                        || value.contains("http://edukb.")) {
                                    continue;
                                }
                                feature_list.add(feature);
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        detail_feature.setAdapter(new FeatureAdapter());
                        if (!independence) {
                            requireActivity().runOnUiThread(() -> FragmentInstance.skv.setVisibility(View.INVISIBLE));
                            FragmentInstance.mHandler.sendMessage(new Message());
                        }
                        Log.e("getInstanceDetail", "FeatureAdapter");
                    });
                });
            } catch (JSONException | IOException | InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
    }
    public class Response {
        public void handle (CallBack callBack) throws IOException, JSONException, InterruptedException {
            Log.e("FragmentInsDetail(course)", course);
            InstanceDetail ins =  MainActivity.dataLoader.getDetailByName(course, name);
            callBack.onResponse(ins);
        }
    }
    interface CallBack  {
        void onResponse(InstanceDetail ins) throws IOException, JSONException;
    }

    /**
     * RecyclerView 的 Adapter
     */
    class FeatureAdapter extends RecyclerView.Adapter<FragmentInsDetail.FeatureAdapter.ViewHolder> {

        @NonNull
        @NotNull
        @Override
        public FragmentInsDetail.FeatureAdapter.ViewHolder onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.feature_item, parent, false);
            return new FragmentInsDetail.FeatureAdapter.ViewHolder(v);
        }

        @SuppressLint("UseCompatLoadingForDrawables")
        @Override
        public void onBindViewHolder(@NonNull @NotNull FragmentInsDetail.FeatureAdapter.ViewHolder holder, int position) {
            JSONObject feature = feature_list.get(position);
            try {
                holder.feature_key.setText(feature.getString("predicateLabel"));
                String value = null;
                if (feature.has("object")) {
                    value = feature.getString("object");
                } else if (feature.has("subject")) {
                    value = feature.getString("subject");
                } else {
                    value = "";
                }
                if (value.contains("http://kb.cs.tsinghua.edu.cn/apihtml/getjpg") || value.contains("http://kb.cs.tsinghua.edu.cn/apihtml/getpng")) {
                    Glide.with(requireContext())
                            .load(value)
                            .centerCrop()
                            .dontTransform()
                            .dontAnimate()
                            .format(DecodeFormat.PREFER_ARGB_8888)
                            .diskCacheStrategy(DiskCacheStrategy.ALL)
                            .override(Target.SIZE_ORIGINAL, Target.SIZE_ORIGINAL)
                            .into(holder.feature_value_image);
                } else {
                    holder.feature_value.setText(value);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        @Override
        public int getItemCount() {
            return feature_list.size();
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            TextView feature_key;
            FlexibleRichTextView feature_value;
            ImageView feature_value_image;
            public ViewHolder(@NonNull @NotNull View itemView) {
                super(itemView);
                feature_key = itemView.findViewById(R.id.feature_key);
                feature_value = itemView.findViewById(R.id.feature_value);
                feature_value_image = itemView.findViewById(R.id.feature_value_img);
            }
        }
    }

    public void setPic() {
        new Thread(() -> {
            try {
                new Response2().handle(res -> {
                    picUrl = res;
                    requireActivity().runOnUiThread(() -> {
                        Glide.with(getContext())
                                .load(picUrl)
                                .centerCrop()
                                .dontTransform()
                                .dontAnimate()
                                .format(DecodeFormat.PREFER_ARGB_8888)
                                .diskCacheStrategy(DiskCacheStrategy.ALL)
                                .override(Target.SIZE_ORIGINAL, Target.SIZE_ORIGINAL)
                                .into(entity_pic);
                    });
                });
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
    }
    public class Response2 {
        public void handle (CallBack2 callBack) throws IOException {
            PicSpider bs = new PicSpider(name);
            String res = bs.getPic();
            callBack.onResponse(res);
        }
    }
    interface CallBack2  {
        void onResponse(String res) throws IOException;
    }

    public String getText () throws JSONException {
        String res = "";
        res += ("实体名称：" + name + "\n");
        res += ("所属学科：" + Utils.Chinese(course) + "\n");
        for (int i = 0; i < feature_list.size(); ++i) {
            JSONObject feature = feature_list.get(i);
            String key = feature.getString("predicateLabel");
            String value = "";
            if (feature.has("object")) {
                value = feature.getString("object");
            } else if (feature.has("subject")) {
                value = feature.getString("subject");
            }
            res += (key + "：" + value + "\n");
        }
        res = res.substring(0, res.length() - 1);
        return res;
    }

    public String getPicUrl () {
        return picUrl;
    }

    public String getInsString () {
        return instance.toString();
    }

    public void addHistory () {
        SharedPreferences sharedPreferences = requireActivity().getSharedPreferences("user", Context.MODE_PRIVATE);
        String token = sharedPreferences.getString("token",null);
        if (token == null) return;
        com.alibaba.fastjson.JSONObject body = new com.alibaba.fastjson.JSONObject();
        body.put("token", token);
        body.put("insUri", uri);
        body.put("detail", getInsString());
        body.put("time", new Date().getTime());
        String request_url = "http://82.156.215.178:8080/user/addHistory";
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
                com.alibaba.fastjson.JSONObject res = com.alibaba.fastjson.JSONObject.parseObject(str);
                Log.e("addHistory", res.toString());
                if (res.containsKey("success") || res.getString("success").equals("true")) {
                } else {
                    requireActivity().runOnUiThread(() -> Toast.makeText(getActivity(), "网络请求错误", Toast.LENGTH_SHORT).show());
                }
            }
        }).start();
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

