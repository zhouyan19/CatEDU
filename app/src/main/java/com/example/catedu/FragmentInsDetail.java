/**
 * @filename FragmentInsDetail
 * @description  实体详情页
 * @author ZhouYan
 * */

package com.example.catedu;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
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

import java.io.IOException;
import java.time.Instant;
import java.util.Vector;


public class FragmentInsDetail extends Fragment {
    public static String uri; // 实体uri
    public static String name; // 实体名称
    public static String course; // 学科名称
    private static InstanceDetail instance;
    private static String picUrl;

    private static Vector<JSONObject> feature_list;

    FlexibleRichTextView detail_name;
    RecyclerView detail_feature;
    ImageView entity_pic;

    FragmentInsDetail (String _u, String _n, String _c) {
        Log.e("FragmentInsDetail", "New!");
        uri = _u;
        name = _n;
        course = _c;
        instance = new InstanceDetail();
        feature_list = new Vector<>();
        picUrl = "";
    }

    /**
     * FragmentInsDetail 创建时的操作
     */
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // 绑定 layout
        return inflater.inflate(R.layout.fragment_ins_detail, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {

        detail_name = view.findViewById(R.id.detail_name);
        detail_feature = view.findViewById(R.id.detail_feature);
        detail_feature.addItemDecoration(new DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL));
        detail_feature.setLayoutManager(new LinearLayoutManager(getContext()));

        entity_pic = view.findViewById(R.id.entity_pic);

        getInstanceDetail();

        try {
            setPic(name);
        } catch (IOException e) {
            e.printStackTrace();
        }
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
                    Log.e("getInstanceDetail", instance.getEntity_name());
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
                        requireActivity().runOnUiThread(() -> FragmentInstance.skv.setVisibility(View.INVISIBLE));
                        FragmentInstance.mHandler.sendMessage(new Message());
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

    public void setPic(String name) throws IOException {
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

}

