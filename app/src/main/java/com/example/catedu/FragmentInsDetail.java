/**
 * @filename FragmentInsDetail
 * @description  实体详情页
 * @author ZhouYan
 * */

package com.example.catedu;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.catedu.data.DataLoader;
import com.example.catedu.data.Instance;
import com.example.catedu.data.InstanceDetail;
import com.example.catedu.data.Triple;
import com.github.ybq.android.spinkit.SpinKitView;

import org.json.JSONException;

import java.io.IOException;
import java.util.Date;
import java.util.Objects;
import java.util.Vector;


public class FragmentInsDetail extends Fragment {
    public static String uri; // 实体uri
    public static String course; // 学科名称
    private static DataLoader dataLoader;
    private static InstanceDetail instance;

    ImageButton back_home;
    SpinKitView skv;
    TextView detail_text;

    FragmentInsDetail (String _u, String _c) {
        uri = _u;
        course = _c;
        dataLoader = new DataLoader();
        instance = new InstanceDetail();
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
        back_home.setOnClickListener(v -> getParentFragmentManager().popBackStack());

        detail_text = (TextView) view.findViewById(R.id.detail_text);

        getInstanceDetail();
    }

    public void getInstanceDetail () {
        skv.setVisibility(View.VISIBLE);
        new Thread(() -> {
            try {
                new Response().handle(ins -> {
                    instance = ins;
                    Log.e("getInstanceDetail", instance.getEntity_name());
                    requireActivity().runOnUiThread(() -> {
                        try {
                            detail_text.setText(instance.getEntity_features().getJSONObject(1).getString("feature_value"));
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    });
                });
            } catch (JSONException | IOException | InterruptedException e) {
                e.printStackTrace();
            }
            requireActivity().runOnUiThread(() -> skv.setVisibility(View.INVISIBLE));
        }).start();
    }

    public static class Response {
        public void handle (CallBack callBack) throws IOException, JSONException, InterruptedException {
            InstanceDetail ins =  dataLoader.getDetailByUri(Utils.English(course), uri);
            callBack.onResponse(ins);
        };
    }

    interface CallBack  {
        void onResponse(InstanceDetail ins);
    }
}
