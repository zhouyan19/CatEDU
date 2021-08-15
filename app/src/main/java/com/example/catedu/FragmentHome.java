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
    final int NUM_PER_PAGE = 10;
    private DataLoader dataLoader;
    private int course_id = 0; // 选中学科编号，默认为语文0
    private final String []courses_all = {"语文", "英语", "数学", "物理", "化学", "生物", "政治", "地理", "历史"};
    private boolean []courses_now = {true, true, true, false, false, false, false, false, false};
    public ArrayList []triLists = new ArrayList[9]; // 学科所有数据
    public ArrayList []triNow = new ArrayList[9]; // 学科当前显示数据
    public int []cntList = {0, 0, 0, 0, 0, 0, 0, 0, 0};

    private String course_name() {
        return courses_all[course_id];
    }

    /**
     * 构造函数，初始化 DataLoader 和各个列表
     */
    public FragmentHome() {
        dataLoader = new DataLoader();
        for (int i = 0; i < 9; ++i) {
            triLists[i] = new ArrayList<Triple>();
            triNow[i] = new ArrayList<Triple>();
        }
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

        for (int i = 0; i < 9; ++i) {
            int finalI = i;
            Thread local_data_thread = new Thread(() -> {
                try {
                    getListByCourse(finalI);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
            local_data_thread.start();
            try {
                local_data_thread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        renewData();

        RefreshLayout srl = view.findViewById(R.id.smart_refresh);
        RecyclerView rv_list = view.findViewById(R.id.rv_list);
        rv_list.addItemDecoration(new DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL));
        rv_list.setLayoutManager(new LinearLayoutManager(getContext()));
        rv_list.setAdapter(new MyAdapter());
        srl.setOnRefreshListener(refreshLayout -> {
            Log.e("Refresh", course_name());
            cntList[course_id] = 0;
            renewData();
            refreshLayout.finishRefresh(2000);
        });
        srl.setOnLoadMoreListener(refreshLayout -> {
            Log.e("LoadMore", course_name());
            renewData();
            refreshLayout.finishLoadMore(2000);
        });

        XTabLayout tl = view.findViewById(R.id.tab_layout);
        tl.addOnTabSelectedListener(new XTabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(XTabLayout.Tab tab) {
                int idc = tab.getPosition();
                Log.e("TabSelected", courses_all[idc]);
                course_id = idc;
                renewData();
                rv_list.setAdapter(new MyAdapter());
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
        addButton.setOnClickListener(v -> builder.show());

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
     * @param id 学科序号
     */
    public void getListByCourse(int id) throws IOException {
        Context context = getContext();
        assert context != null;
        triLists[id] = dataLoader.getLocalCourseData(context, Utils.English(courses_all[id]));
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
            Triple tri = (Triple) triNow[course_id].get(position);
            holder.ins_item.setOnClickListener(v -> showDetail(tri));
            try {
                Instance ins = dataLoader.getInstance(Utils.English(course_name()), tri.getS());
                String number = String.valueOf(position + 1);
                holder.ins_number.setText(number);
                holder.ins_name.setText(ins.getName());
                holder.ins_type.setText(ins.getType());
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        @Override
        public int getItemCount() {
            return triNow[course_id].size();
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            LinearLayout ins_item;
            TextView ins_number;
            TextView ins_name;
            TextView ins_type;
            public ViewHolder(@NonNull @NotNull View itemView) {
                super(itemView);
                ins_item = itemView.findViewById(R.id.ins_item);
                ins_number = itemView.findViewById(R.id.ins_number);
                ins_name = itemView.findViewById(R.id.ins_name);
                ins_type = itemView.findViewById(R.id.ins_type);
            }
        }
    }

    /**
     * 增加 NUM_PER_PAGE 条当前学科的知识
     */
    public void renewData() {
        int num = cntList[course_id];
        for (int i = num; i < num + NUM_PER_PAGE; ++i) {
            triNow[course_id].add(triLists[course_id].get(i));
        }
    }

    /**
     * 查看实体详情
     * @param ins 实体
     */
    public void showDetail (Triple ins) {
    }

}
