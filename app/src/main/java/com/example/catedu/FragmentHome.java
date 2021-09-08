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
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

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
import java.util.HashMap;
import java.util.Objects;
import java.util.Set;
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
//    public int ins_cnt = 0;
    public Vector<String> order2 = new Vector();

    RefreshLayout srl;
    RecyclerView rv_list;
    XTabLayout tl;
    AlertDialog alert;
    AlertDialog.Builder builder;
    ImageButton addButton;

    SpinKitView skv;

    RecyclerView rv_course;

    /**
     * @return 当前学科的名字
     */
    private String course_name () {
        return courses_all[course_id];
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
            order2.add(courses_all[i]);
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
        for (int i = 0; i < 9; ++i) {
            int finalI = i;
            Thread local_data_thread = new Thread(() -> {
                getListByCourse(finalI);
            });
            local_data_thread.start();
            try {
                local_data_thread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        skv = view.findViewById(R.id.spin_kit);
        skv.setVisibility(View.VISIBLE);

        srl = view.findViewById(R.id.smart_refresh);
        rv_list = view.findViewById(R.id.rv_list);
        rv_list.addItemDecoration(new DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL));
        rv_list.setLayoutManager(new LinearLayoutManager(getContext()));

        srl.setOnRefreshListener(refreshLayout -> {
            Log.e("Refresh", course_name());
            cntList[course_id] = 0;
            renewAllData();
            for (int i = 0; i < 9; ++i) {
                if (courses_now[i]) initIns(i);
            }
//            int tmp_cnt = course_count();
//            Log.e("tmp_cnt", String.valueOf(tmp_cnt));
//            while (true) {
//                Log.e("ins_cnt", String.valueOf(ins_cnt));
//                if (ins_cnt == (NUM_PER_PAGE * tmp_cnt)) break;
//            }
//            Log.e("ins_cnt", String.valueOf(ins_cnt));
//            rv_list.setAdapter(new MyAdapter());
//            rv_list.setVisibility(View.VISIBLE);
//            Log.e("FragmentHome", "Adapter Set");
            srl.finishRefresh(5000);
        });
        srl.setOnLoadMoreListener(refreshLayout -> {
            Log.e("LoadMore", course_name());
            renewData();
            srl.finishLoadMore(5000);
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
        rv_list.setVisibility(View.INVISIBLE);

        renewAllData();
        for (int i = 0; i < 9; ++i) {
            if (courses_now[i]) initIns(i);
        }
//        int tmp_cnt = course_count();
//        while (true) {
//            Log.e("ins_cnt", String.valueOf(ins_cnt));
//            if (ins_cnt == (NUM_PER_PAGE * tmp_cnt)) break;
//        }

        addButton = view.findViewById(R.id.add_button);
        addButton.setOnClickListener(v -> {
            LinearLayout course_pop = (LinearLayout) getLayoutInflater().inflate(R.layout.course_popupwindow, null);
            course_pop.setBackgroundResource(R.drawable.pop_border);
            TextView cancel = course_pop.findViewById(R.id.share_cancel);
            TextView confirm = course_pop.findViewById(R.id.share_confirm);

            rv_course = course_pop.findViewById(R.id.rv_course);
            rv_course.setLayoutManager(new LinearLayoutManager(getContext()));
            rv_course.setAdapter(new CourseAdapter());

            builder = new AlertDialog.Builder(getContext());
            builder.setView(course_pop);
            alert = builder.create();

            cancel.setOnClickListener(vv -> {
                alert.dismiss();
            });
            confirm.setOnClickListener(vv -> {
                alert.dismiss();
                int tmp_id = -1;
                for (int i = 0; i < 9; ++i) {
                    String c = order2.get(i);
                    int org = indexOfCourse(c);
                    if (courses_now[org]) {
                        tmp_id = org;
                        break;
                    }
                }
                if (tmp_id == -1) {
                    courses_now = new boolean[]{true, true, true, false, false, false, false, false, false};
                    course_id = 0;
                    order2 = new Vector<>();
                    for (int i = 0; i < 9; ++i) {
                        order2.add(courses_all[i]);
                    }
                } else {
                    course_id = tmp_id;
                }
                renewAllData();
                for (int i = 0; i < 9; ++i) {
                    if (courses_now[i]) initIns(i);
                }

                tl.removeAllTabs();
                for (int i = 0; i < 9; ++i) {
                    String c = order2.get(i);
                    if (courses_now[indexOfCourse(c)]) {
                        tl.addTab(tl.newTab().setText(c));
                    }
                }
            });

            alert.show();
        });

        super.onViewCreated(view, savedInstanceState);
    }


    /**
     * 点击不同学科的按钮后获取数据
     * @param id 学科序号
     */
    public void getListByCourse(int id) {
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
            String type = ins.getType();
            if (type.equals("")) {
                holder.ins_type.setVisibility(View.INVISIBLE);
            } else {
                holder.ins_type.setText(type);
                if (position % 2 == 1) holder.ins_type.setBackgroundResource(R.drawable.subject_border);
            }
//            Boolean seen = (Boolean) seenLists[course_id].get(position);
            String u = ((Triple) triLists[course_id].get(position)).getS();
            if (MainActivity.seenList != null) {
                if (MainActivity.seenList.containsKey(u)) {
                    Log.e("Gray", ins.getName());
                    holder.ins_number.setTextColor(Color.LTGRAY);
                    holder.ins_name.setTextColor(Color.LTGRAY);
                    holder.ins_type.setTextColor(Color.LTGRAY);
                }
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

    class CourseAdapter extends RecyclerView.Adapter<CourseAdapter.ViewHolder> {

        @NonNull
        @NotNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.course_item, parent, false);
            return new ViewHolder(v);
        }

        @SuppressLint({"UseCompatLoadingForDrawables", "ResourceAsColor"})
        @Override
        public void onBindViewHolder(@NonNull @NotNull ViewHolder holder, int position) {
            String c = order2.get(position);
            holder.this_name.setText(c);
            holder.this_name.setOnClickListener(v -> {
                if (position == 0) return;
                String tmp = order2.get(position - 1);
                order2.removeElementAt(position - 1);
                order2.insertElementAt(c, position - 1);
                order2.removeElementAt(position);
                order2.insertElementAt(tmp, position);
                rv_course.setAdapter(new CourseAdapter());
            });

            boolean before = courses_now[indexOfCourse(c)];
            if (before) {
                holder.opt.setBackgroundResource(R.drawable.null_content);
                holder.opt.setImageResource(R.mipmap.course_yes);
                holder.opt.setOnClickListener(v -> {
                    courses_now[indexOfCourse(c)] = false;
                    rv_course.setAdapter(new CourseAdapter());
                });
            } else {
                holder.opt.setBackgroundResource(R.drawable.course_border);
                holder.opt.setImageResource(R.drawable.null_content);
                holder.opt.setOnClickListener(v -> {
                    courses_now[indexOfCourse(c)] = true;
                    rv_course.setAdapter(new CourseAdapter());
                });
            }
        }

        @Override
        public int getItemCount() {
            return 9;
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            LinearLayout course_item;
            ImageButton opt;
            TextView this_name;
            public ViewHolder(@NonNull @NotNull View itemView) {
                super(itemView);
                course_item = itemView.findViewById(R.id.course_item);
                opt = itemView.findViewById(R.id.opt);
                this_name = itemView.findViewById(R.id.this_name);
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
//        ins_cnt = 0;
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
                        if (!ins.getName().equals("")) insLists[_id].add(ins);
//                        seenLists[_id].add(new Boolean(false));
//                        ins_cnt++;
                    }
                    if (_id == course_id) {
                        new Thread(() -> {
                            Log.e("学科" + _id, String.valueOf(inss.size()));
                            if (!inss.get(0).getName().equals("")) {
                                requireActivity().runOnUiThread(() -> {
                                    skv.setVisibility(View.INVISIBLE);
                                    rv_list.setAdapter(new MyAdapter());
                                    rv_list.setVisibility(View.VISIBLE);
                                });
                                Log.e("FragmentHome", "Adapter Set");
                            } else  {
                                requireActivity().runOnUiThread(() -> {
                                    skv.setVisibility(View.INVISIBLE);
                                    rv_list.setVisibility(View.GONE);
                                    Toast.makeText(getActivity(), "网络请求错误", Toast.LENGTH_SHORT).show();
                                });
                            }
                        }).start();
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
