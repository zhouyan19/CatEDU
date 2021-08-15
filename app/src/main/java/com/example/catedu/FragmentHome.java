/**
 * @filename FragmentHome
 * @description  主页组件 (1)
 * @author ZhouYan
 * */

package com.example.catedu;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Entity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.androidkun.xtablayout.XTabLayout;
import com.example.catedu.data.DataLoader;
import com.example.catedu.data.Instance;
import com.example.catedu.data.Triple;
import com.scwang.smart.refresh.layout.api.RefreshLayout;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;

import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Map.Entry;

public class FragmentHome extends Fragment {
    private DataLoader dataLoader;
    private ArrayList<Triple> triList; // 存取学科三元组的数组
    private String course_name = "语文"; // 当前学科名，默认为语文
    private final String []courses_all = {"语文", "英语", "数学", "物理", "化学", "生物", "政治", "地理", "历史"};
    private boolean []courses_now = {true, true, true, false, false, false, false, false, false};
    private int idx = 0; // 三元组计数

    /**
     * 构造函数，初始化 DataLoader 和各个列表
     */
    public FragmentHome() {
        dataLoader = new DataLoader();
        triList = new ArrayList<>();
    }

    /**
     * FragmentHome 创建时的操作
     */
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // 绑定 layout
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    /**
     * CreateView 之后初始化数据
     * 绑定 SmartRefreshLayout, RecyclerView, TabLayout
     */
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        RefreshLayout srl = view.findViewById(R.id.smart_refresh);
        RecyclerView rv_list = view.findViewById(R.id.rv_list);
        rv_list.addItemDecoration(new DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL));
        rv_list.setLayoutManager(new LinearLayoutManager(getContext()));
        rv_list.setAdapter(new MyAdapter());
        srl.setOnRefreshListener(refreshLayout -> {
            Log.e("Refresh", course_name);
            try {
                idx = 0;
                getListByCourse(course_name);
            } catch (IOException e) {
                e.printStackTrace();
            }
            refreshLayout.finishRefresh(5000);
        });
        srl.setOnLoadMoreListener(refreshLayout -> {
            Log.e("LoadMore", course_name);
            try {
                getListByCourse(course_name);
            } catch (IOException e) {
                e.printStackTrace();
            }
            refreshLayout.finishLoadMore(5000);
        });

        XTabLayout tl = view.findViewById(R.id.tab_layout);
        tl.addOnTabSelectedListener(new XTabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(XTabLayout.Tab tab) {
                String course = (String) tab.getText();
                try {
                    Log.e("TabSelected", course);
                    idx = 0;
                    getListByCourse(course);
                    rv_list.setAdapter(new MyAdapter());
                    course_name = course;
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onTabUnselected(XTabLayout.Tab tab) {}

            @Override
            public void onTabReselected(XTabLayout.Tab tab) {}
        });
        for (int i = 0; i < 9; ++i) {
            if (courses_now[i]) {
                String c = courses_all[i];
                tl.addTab(tl.newTab().setText(c));
            }
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("选择科目");
        builder.setIcon(R.mipmap.cat_icon);
        builder.setMultiChoiceItems(courses_all, courses_now, (dialog, which, isChecked) -> courses_now[which] = isChecked);
        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                builder.setMultiChoiceItems(courses_all, courses_now, (_dialog, _which, _isChecked) -> courses_now[_which] = _isChecked);
                tl.removeAllTabs();
                for (int i = 0; i < 9; ++i) {
                    if (courses_now[i]) {
                        String c = courses_all[i];
                        tl.addTab(tl.newTab().setText(c));
                    }
                }
            }
        });
        builder.setNegativeButton("取消", null);

        ImageButton addButton = view.findViewById(R.id.add_button);
        addButton.setOnClickListener(v -> {
            builder.show();
        });

        super.onViewCreated(view, savedInstanceState);
    }

    /**
     * 加载完本地数据后，再进行登录
     */
    @Override
    public void onStart() {
        super.onStart();
        try {
            dataLoader.logIn();
            Log.e("FragmentHome", "Log In Success!");
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * 点击不同学科的按钮后获取数据
     * @param course 学科中文名
     */
    public void getListByCourse(String course) throws IOException {
        Context context = getContext();
        triList.addAll(dataLoader.getLocalCourseData(context, Utils.English(course), idx));
        idx = triList.size();
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
            holder.ins_item.setOnClickListener(v -> showDetail(triList.get(position)));
            Triple tri = triList.get(position);
            try {
                Instance ins = dataLoader.getInstance(Utils.English(course_name), tri.getS());
                holder.ins_title.setText(ins.getName());
            } catch (InterruptedException e) {
                holder.ins_title.setText("无名称");
                e.printStackTrace();
            }

        }

        @Override
        public int getItemCount() {
            return triList.size();
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
    public void showDetail (Triple ins) {

    }

}
