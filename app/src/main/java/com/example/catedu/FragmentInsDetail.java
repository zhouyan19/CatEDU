/**
 * @filename FragmentInsDetail
 * @description  实体详情页
 * @author ZhouYan
 * */

package com.example.catedu;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

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
import com.example.catedu.data.DataLoader;
import com.example.catedu.data.InstanceDetail;
import com.github.ybq.android.spinkit.SpinKitView;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Vector;


public class FragmentInsDetail extends Fragment {
    public static String uri; // 实体uri
    public static String course; // 学科名称
    private static DataLoader dataLoader;
    private static InstanceDetail instance;
    private static String picUrl;

    private static Vector<JSONObject> feature_list;

    ImageButton back_home;
    SpinKitView skv;
    FlexibleRichTextView detail_name;
    TextView detail_type;
    RecyclerView detail_feature;
    ImageView entity_pic;

    FragmentInsDetail (String _u, String _c) {
        uri = _u;
        course = _c;
        dataLoader = new DataLoader();
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
        skv = view.findViewById(R.id.spin_kit);

        back_home = view.findViewById(R.id.detail_back_home);
        back_home.setOnClickListener(v -> backSwitchFragment());

        detail_name = view.findViewById(R.id.detail_name);
        detail_type = view.findViewById(R.id.detail_type);
        detail_feature = view.findViewById(R.id.detail_feature);
        detail_feature.addItemDecoration(new DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL));
        detail_feature.setLayoutManager(new LinearLayoutManager(getContext()));

        entity_pic = view.findViewById(R.id.entity_pic);

        getInstanceDetail();

//        try {
//            setPic(instance.getEntity_name());
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
    }

    @SuppressLint("SetTextI18n")
    public void getInstanceDetail () {
        skv.setVisibility(View.VISIBLE);
        new Thread(() -> {
            try {
                new Response().handle(ins -> {
                    instance = ins;
                    Log.e("getInstanceDetail", instance.getEntity_name());
                    String name = "实体名称：" + ((instance.getEntity_name().equals("")) ? "无" : instance.getEntity_name());
                    String type = "实体类别：" + ((instance.getEntity_type().equals("")) ? "无" : instance.getEntity_type());
                    requireActivity().runOnUiThread(() -> {
                        detail_name.setText(name);
                        detail_type.setText(type);
                        JSONArray features = ins.getEntity_features();
                        try {
                            for (int i = 0; i < features.length(); ++i) {
                                JSONObject feature = features.getJSONObject(i);
                                feature_list.add(feature);
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        detail_feature.setAdapter(new FeatureAdapter());
                        requireActivity().runOnUiThread(() -> skv.setVisibility(View.INVISIBLE));
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
            InstanceDetail ins =  dataLoader.getDetailByUri(Utils.English(course), uri);
            callBack.onResponse(ins);
        }
    }
    interface CallBack  {
        void onResponse(InstanceDetail ins) throws IOException;
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
                holder.feature_key.setText(feature.getString("feature_key"));
                holder.feature_value.setText(feature.getString("feature_value"));
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
            public ViewHolder(@NonNull @NotNull View itemView) {
                super(itemView);
                feature_key = itemView.findViewById(R.id.feature_key);
                feature_value = itemView.findViewById(R.id.feature_value);
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
            } catch (IOException | InterruptedException | JSONException e) {
                e.printStackTrace();
            }
            requireActivity().runOnUiThread(() -> skv.setVisibility(View.INVISIBLE));
        }).start();
    }
    public class Response2 {
        public void handle (CallBack2 callBack) throws IOException, JSONException, InterruptedException {
            PicSpider bs = new PicSpider(instance.getEntity_name());
            String res = bs.getPic();
            callBack.onResponse(res);
        }
    }
    interface CallBack2  {
        void onResponse(String res) throws IOException;
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
