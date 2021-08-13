/**
 * @filename FragmentHome
 * @description  主页组件 (1)
 * @author ZhouYan
 * */

package com.example.catedu;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.catedu.data.DataLoader;
import com.example.catedu.data.Instance;
import com.scwang.smart.refresh.layout.api.RefreshLayout;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;

import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.IntSummaryStatistics;

import javax.sql.DataSource;

public class FragmentHome extends Fragment {
    private DataLoader dataLoader;
    private ArrayList<Instance> insList; // 存取 chinese 实体的数组
    private int idx = 0; // chinese 实体计数

    /**
     * 构造函数，初始化 DataLoader 和各个列表
     */
    public FragmentHome() {
        dataLoader = new DataLoader();
        insList = new ArrayList<>();
    }

    /**
     * FragmentHome 创建时的操作
     */
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // 绑定 layout
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    /**
     * 初始化数据
     * 绑定 SmartRefreshLayout 和 RecyclerView
     */
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        try {
            getListByCourse("语文");
        } catch (IOException e) {
            e.printStackTrace();
        }
        Log.e("onViewCreated", "Got!");
        assert insList!=null;
        Log.e("onViewCreated", String.valueOf(idx));

        RefreshLayout srl = view.findViewById(R.id.smart_refresh);
        RecyclerView rv_list = view.findViewById(R.id.rv_list);
        rv_list.addItemDecoration(new DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL));
        rv_list.setLayoutManager(new LinearLayoutManager(getContext()));
        rv_list.setAdapter(new MyAdapter());

        super.onViewCreated(view, savedInstanceState);
    }

    /**
     * 点击不同学科的按钮后获取数据
     * @param course 学科中文名
     */
    public void getListByCourse(String course) throws IOException {
        Context context = getContext();
        switch (course) {
            case "语文":
                insList = dataLoader.getLocalCourseData(context, "chinese", idx);
                break;
            case "英语":
                insList = dataLoader.getLocalCourseData(context, "english", idx);
                break;
            case "数学":
                insList = dataLoader.getLocalCourseData(context, "math", idx);
                break;
            case "物理":
                insList = dataLoader.getLocalCourseData(context, "physics", idx);
                break;
            case "化学":
                insList = dataLoader.getLocalCourseData(context, "chemistry", idx);
                break;
            case "生物":
                insList = dataLoader.getLocalCourseData(context, "biology", idx);
                break;
            case "历史":
                insList = dataLoader.getLocalCourseData(context, "history", idx);
                break;
            case "地理":
                insList = dataLoader.getLocalCourseData(context, "geo", idx);
                break;
            case "政治":
                insList = dataLoader.getLocalCourseData(context, "politics", idx);
                break;
            default:
                break;
        }
        idx += insList.size();
    }

    /**
     * RecyclerView 的 Adapter
     */
    class MyAdapter extends RecyclerView.Adapter<MyAdapter.ViewHolder> {

        @NonNull
        @NotNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.ins_item, parent, false);
            return new ViewHolder(v);
        }

        @SuppressLint("UseCompatLoadingForDrawables")
        @Override
        public void onBindViewHolder(@NonNull @NotNull ViewHolder holder, int position) {
            holder.ins_item.setOnClickListener(v -> showDetail(insList.get(position)));
            String s = (String) insList.get(position).getS();
            holder.ins_title.setText(s);
        }

        @Override
        public int getItemCount() {
            return insList.size();
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            LinearLayout ins_item;
            TextView ins_title;

            public ViewHolder(@NonNull @NotNull View itemView) {
                super(itemView);
                ins_item = itemView.findViewById(R.id.ins_item);
                ins_title = itemView.findViewById(R.id.ins_title);
            }
        }
    }

    /**
     * 查看实体详情
     * @param ins 实体
     */
    public void showDetail (Instance ins) {

    }
}
