/**
 * @filename FragmentHome
 * @description  主页组件 (1)
 * @author ZhouYan
 * */

package com.example.catedu;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TableRow;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.androidkun.xtablayout.XTabLayout;
import com.daquexian.flexiblerichtextview.FlexibleRichTextView;
import com.example.catedu.data.DataLoader;
import com.example.catedu.data.Instance;
import com.example.catedu.data.Triple;
import com.github.ybq.android.spinkit.SpinKitView;
import com.ldoublem.loadingviewlib.view.LVEatBeans;
import com.scwang.smart.refresh.layout.api.RefreshLayout;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;

import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Objects;
import java.util.TreeMap;
import java.util.Vector;

public class FragmentHome extends Fragment {
    final int NUM_PER_PAGE = 8;
    private int course_id = 0; // 选中学科编号，默认为语文0
    private final String []courses_all = {"语文", "英语", "数学", "物理", "化学", "生物", "政治", "地理", "历史"};
    private boolean []courses_now = {true, true, true, false, false, false, false, false, false};
    public Vector []triLists = new Vector[9]; // 学科所有数据
    public Vector []triNow = new Vector[9]; // 学科当前显示数据
    public int []cntList = {0, 0, 0, 0, 0, 0, 0, 0, 0}; // 每个学科当前数据数量
    public Vector []insLists = new Vector[9]; // 每个学科当前获取的实体
//    public Vector []seenLists = new Vector[9]; // 每个学科已看过的实体
    public int ins_cnt = 0;

    SpinKitView skv;
    RefreshLayout srl;
    RecyclerView rv_list;
    XTabLayout tl;
    AlertDialog.Builder builder;
    ImageButton addButton;

    /**
     * @return 当前学科的名字
     */
    private String course_name () {
        return courses_all[course_id];
    }

    private int course_count () {
        int cnt = 0;
        for (int i = 0; i < 9; ++i) { if (courses_now[i]) cnt++; }
        return cnt;
    }

    private int indexOfCourse (String course) {
        switch (course) {
            case "语文": return 0;
            case "英语": return 1;
            case "数学": return 2;
            case "物理": return 3;
            case "化学": return 4;
            case "生物": return 5;
            case "政治": return 6;
            case "地理": return 7;
            case "历史": return 8;
            default: return -1;
        }
    }

    /**
     * 构造函数，初始化 DataLoader 和各个列表
     */
    public FragmentHome() {
        for (int i = 0; i < 9; ++i) {
            triLists[i] = new Vector<Triple>();
            triNow[i] = new Vector<Triple>();
            insLists[i] = new Vector<Instance>();
//            seenLists[i] = new Vector<Boolean>();
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
     * 重要！此函数为魔法，勿动
     */
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        skv = view.findViewById(R.id.spin_kit);
        skv.setVisibility(View.VISIBLE);

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

        srl = view.findViewById(R.id.smart_refresh);
        rv_list = view.findViewById(R.id.rv_list);
        rv_list.addItemDecoration(new DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL));
        rv_list.setLayoutManager(new LinearLayoutManager(getContext()));
        rv_list.setVisibility(View.INVISIBLE);

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

        tl = view.findViewById(R.id.tab_layout);
        tl.addOnTabSelectedListener(new XTabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(XTabLayout.Tab tab) {
                Log.e("OnTabSelected", (String) tab.getText());
                String course = (String) tab.getText();
                course_id = indexOfCourse(course);
                rv_list.setAdapter(new MyAdapter());
                rv_list.setVisibility(View.VISIBLE);
                Log.e("FragmentHome", "Adapter Set");
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
                Log.e("AddTab", c);
            }
        }

        renewAllData();
        for (int i = 0; i < 9; ++i) {
            if (courses_now[i]) initIns(i);
        }
        int tmp_cnt = course_count();
        while (true) {
            if (ins_cnt == (NUM_PER_PAGE * tmp_cnt)) break;
        }
        skv.setVisibility(View.INVISIBLE);

        rv_list.setAdapter(new MyAdapter());
        rv_list.setVisibility(View.VISIBLE);
        Log.e("FragmentHome", "Adapter Set");

        builder = new AlertDialog.Builder(getContext());
        builder.setTitle("选择科目");
        builder.setIcon(R.mipmap.cat_icon);
        builder.setMultiChoiceItems(courses_all, courses_now, (dialog, which, isChecked) -> courses_now[which] = isChecked);
        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                builder.setMultiChoiceItems(courses_all, courses_now, (_dialog, _which, _isChecked) -> courses_now[_which] = _isChecked);
                tl.removeAllTabs();
                int tmp_id = -1;
                for (int i = 0; i < 9; ++i) {
                    if (courses_now[i]) {
                        if (tmp_id == -1) tmp_id = i; // 找到目前的第一个学科
                    }
                }
                if (tmp_id == -1) {
                    courses_now = new boolean[]{true, true, true, false, false, false, false, false, false};
                    course_id = 0;
                } else {
                    course_id = tmp_id;
                }

                Log.e("Builder/id", String.valueOf(course_id));

                renewAllData();
                for (int i = 0; i < 9; ++i) {
                    if (courses_now[i]) initIns(i);
                }
                int tmp_cnt = course_count();
                Log.e("tmp_cnt", String.valueOf(tmp_cnt));
                while (true) {
                    Log.e("ins_cnt", String.valueOf(ins_cnt));
                    if (ins_cnt == (NUM_PER_PAGE * tmp_cnt)) break;
                }
                Log.e("ins_cnt", String.valueOf(ins_cnt));
                rv_list.setAdapter(new MyAdapter());
                rv_list.setVisibility(View.VISIBLE);
                Log.e("FragmentHome", "Adapter Set");

                for (int i = 0; i < 9; ++i) {
                    if (courses_now[i]) {
                        String c = courses_all[i];
                        tl.addTab(tl.newTab().setText(c));
                    }
                }
            }
        });
        builder.setNegativeButton("取消", null);

        addButton = view.findViewById(R.id.add_button);
        addButton.setOnClickListener(v -> builder.show());

        super.onViewCreated(view, savedInstanceState);
    }


    /**
     * 点击不同学科的按钮后获取数据
     * @param id 学科序号
     */
    public void getListByCourse(int id) throws IOException {
        Context context = getContext();
        assert context != null;
        triLists[id] = MainActivity.dataLoader.getLocalCourseData(context, Utils.English(courses_all[id]));
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

        @SuppressLint({"UseCompatLoadingForDrawables", "ResourceAsColor"})
        @Override
        public void onBindViewHolder(@NonNull @NotNull ViewHolder holder, int position) {
            holder.ins_item.setOnClickListener(v -> showDetail(position));
            holder.ins_number.setOnClickListener(v -> showDetail(position));
            holder.ins_name.setOnClickListener(v -> showDetail(position));
            Instance ins = (Instance) insLists[course_id].get(position);
            String number = String.valueOf(position + 1);
            holder.ins_number.setText(number);
            holder.ins_name.setText(ins.getName());
//            Boolean seen = (Boolean) seenLists[course_id].get(position);
            String u = ((Triple) triLists[course_id].get(position)).getS();
            if (MainActivity.seenList.containsKey(u)) {
                Log.e("Gray", ins.getName());
                holder.ins_number.setTextColor(Color.LTGRAY);
                holder.ins_name.setTextColor(Color.LTGRAY);
            }
        }

        @Override
        public int getItemCount() {
            return insLists[course_id].size();
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            LinearLayout ins_item;
            TextView ins_number;
            TextView ins_name;
            public ViewHolder(@NonNull @NotNull View itemView) {
                super(itemView);
                ins_item = itemView.findViewById(R.id.ins_item);
                ins_number = itemView.findViewById(R.id.ins_number);
                ins_name = itemView.findViewById(R.id.ins_name);
            }
        }
    }

    /**
     * 更新 NUM_PER_PAGE 条当前学科的知识
     */
    public void renewData() {
        int num = cntList[course_id];
        for (int i = num; i < num + NUM_PER_PAGE; ++i) {
            Triple tri = (Triple) triLists[course_id].get(i);
            triNow[course_id].add(tri);
            Instance ins = null;
            try {
                ins = MainActivity.dataLoader.getInstance(Utils.English(course_name()), tri.getS());
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            insLists[course_id].add(ins);
//            seenLists[course_id].add(new Boolean(false));
        }
        cntList[course_id] += NUM_PER_PAGE;
    }

    /**
     * 清空情空并初始化 NUM_PER_PAGE 条所有学科的知识
     */
    public void renewAllData() {
        ins_cnt = 0;
        for (int i = 0; i < 9; ++i) {
            triNow[i].clear();
            insLists[i].clear();
//            seenLists[i].clear();
            cntList[i] = 0;
            for (int j = 0; j < NUM_PER_PAGE; ++j) {
                Triple tri = (Triple) triLists[i].get(j);
                triNow[i].add(tri);
            }
            cntList[i] = NUM_PER_PAGE;
        }
    }

    /**
     * 对该学科的实体访问uri来初始化
     */
    public void initIns (int _id) {
        int num = insLists[_id].size();
        new Thread(() -> {
            Vector tris = new Vector();
            for (int j = num; j < cntList[_id]; ++j) {
                Triple tri = (Triple) triLists[_id].get(j);
                tris.add(tri);
            }
            try {
                new Response().handle(tris, inss -> {
                    for (Instance ins : inss) {
                        insLists[_id].add(ins);
//                        seenLists[_id].add(new Boolean(false));
                        ins_cnt++;
                    }
                    Log.e("initIns", "InsLists set");
                });
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
    }

    public class Response {
        public void handle(Vector<Triple> tris, CallBack callBack) throws InterruptedException {
            Vector<Instance> inss = new Vector<>();
            for (int i = 0; i < tris.size(); ++i) inss.add(new Instance());
            for (int i = 0; i < tris.size(); ++i) {
                Triple tri = tris.get(i);
                Log.e("Response(uri)", tri.getS());
                Instance ins = MainActivity.dataLoader.getInstance(Utils.English(courses_all[course_id]), tri.getS());
                Log.e("Response(name)", ins.getName());
                inss.set(i, ins);
            }
            callBack.onResponse(inss);
        };
    }
    interface CallBack  {
        void onResponse(Vector<Instance> inss);
    }

    /**
     * 查看实体详情
     * @param pos 实体的序号
     */
    public void showDetail (int pos) {
        Log.e("showDetail", String.valueOf(pos + 1));
        Triple tri = (Triple) triNow[course_id].get(pos);
        String uri = tri.getS();
        String name = ((Instance) insLists[course_id].get(pos)).getName();

//        seenLists[course_id].removeElementAt(pos);
//        seenLists[course_id].insertElementAt(new Boolean(true), pos);

        FragmentInstance fi = new FragmentInstance(uri, name, course_name());
        MainActivity.fragments.add(fi);
        forwardSwitchFragment();

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

    public void update () {
        Log.e("Update!", "FragmentHome");
        Log.e("seenList", MainActivity.seenList.toJSONString());
        rv_list.setAdapter(new MyAdapter());
    }
}
