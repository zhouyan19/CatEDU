package com.example.catedu;

import android.annotation.SuppressLint;
import android.graphics.Rect;
import android.os.Bundle;

import com.example.catedu.data.*;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.widget.ArrayAdapter;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.AdapterView;
import android.widget.TextView;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;

import java.io.IOException;
import java.text.CollationKey;
import java.text.Collator;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Vector;
import java.util.stream.Collectors;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link FragmentQuesRetrieval#newInstance} factory method to
 * create an instance of this fragment.
 */
public class FragmentQuesRetrieval extends Fragment {
    private final String[] historyStrs = { };
    private final String[] courses = {"语文", "数学", "英语", "物理", "化学", "生物", "历史", "地理", "政治"};
    private static Vector<InstanceWithUri> resultList;
    int courseId = 0;
    int sortMode = -1; //default
    int filterMode = -1; //default
    String queryWord = "";
    SearchView mSearchView;
    ListView mListView;
    RecyclerView mRecyclerView;

    Spinner mSpinner;
    ImageButton back_home;
    RadioGroup sortGroup;
    RadioGroup filterGroup;
    ImageButton sortClearBtn;
    ImageButton filterClearBtn;
    private static int inputDownHeightDiff;



    public FragmentQuesRetrieval() {
        resultList = new Vector<>();
    }


    public static FragmentQuesRetrieval newInstance(String param1, String param2) {
        FragmentQuesRetrieval fragment = new FragmentQuesRetrieval();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_ques_retrieval, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        back_home = view.findViewById(R.id.detail_back_home);
        back_home.setOnClickListener(v -> {
            try {
                backSwitchFragment();
            } catch (Throwable throwable) {
                throwable.printStackTrace();
            }
        });

        mRecyclerView = view.findViewById(R.id.rl_result);
        mRecyclerView.addItemDecoration(new DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL));
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        mSearchView = view.findViewById(R.id.sv_retrieval);
//        mListView = view.findViewById(R.id.lv);
//        mListView.setAdapter(new ArrayAdapter<String>(this.getContext(), android.R.layout.simple_list_item_1, historyStrs));
//        mListView.setTextFilterEnabled(true);

        mSpinner = view.findViewById(R.id.sp_course);
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<String>(this.getContext(), android.R.layout.simple_list_item_1, courses);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mSpinner.setAdapter(spinnerAdapter);

        mSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                Log.i("spinner", courses[pos]);
                courseId = pos;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // TODO
                Log.i("spinner", "nothing selected");
            }
        });

        //设置搜索文本监听
        mSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            // 当点击搜索按钮时触发该方法
            @Override
            public boolean onQueryTextSubmit(String query) {
                Log.i("String query", query);
                queryWord = query;
                mSearchView.clearFocus();

                getRetrievalResults();

                return false;
            }

            // 当搜索内容改变时触发该方法
            @Override
            public boolean onQueryTextChange(String newText) {
//                if (!TextUtils.isEmpty(newText)) {
//                    mListView.setFilterText(newText);
//                } else {
//                    mListView.clearTextFilter();
//                }
                return false;
            }
        });

        sortGroup = view.findViewById(R.id.sort_group);
        filterGroup = view.findViewById(R.id.filter_group);
        sortGroup.setOnCheckedChangeListener((radioGroup, i) -> {
            sortMode = i;
            new Thread(() -> {
                refreshAndDisplay(resultList);
            }).start();
            Log.e("run", "thetet");
        });
        filterGroup.setOnCheckedChangeListener((radioGroup, i) -> {
            filterMode = i;
            refreshAndDisplay(resultList);
            Log.e("run", "herehe");
        });
        sortClearBtn = view.findViewById(R.id.sort_clear);
        filterClearBtn = view.findViewById(R.id.filter_clear);
        sortClearBtn.setOnClickListener(v->sortGroup.clearCheck());
        filterClearBtn.setOnClickListener(v->filterGroup.clearCheck());

        final View rootView = getActivity().getWindow().getDecorView();
//        final View rootView = getActivity().getWindow().getDecorView().findViewById(android.R.id.content);
        rootView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                if (inputDownHeightDiff < getHeightDiff()) {
                    MainActivity.nav_view.setVisibility(View.GONE);
                    Log.i("KeyboardChange", "Up");
                } else {
                    MainActivity.nav_view.setVisibility(View.VISIBLE);
                    Log.i("KeyboardChange", "Down");
                }
                //如果只想检测一次，需要注销
                //rootView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
            }
        });
        inputDownHeightDiff = getHeightDiff();
    }
    public int getHeightDiff() {
        final View rootView = getActivity().getWindow().getDecorView();
        Rect r = new Rect();
        rootView.getWindowVisibleDisplayFrame(r);
        Log.i("height", r.bottom + "   " + rootView.getRootView().getHeight());

//        DisplayMetrics dm = rootView.getResources().getDisplayMetrics();
        return rootView.getRootView().getHeight() - r.bottom;
    }

    List<InstanceWithUri> sortAndFilterResults(Vector<InstanceWithUri> res){
        switch (sortMode){
            case R.id.sort1:
                Log.e("sort", sortMode+" ");
                res.sort((ins1, ins2) -> {
                    return ins1.getName().length() < ins2.getName().length()?-1:1;
                });
                break;
            case R.id.sort2:
                Log.e("sort", sortMode+" ");
                res.sort((ins1, ins2) -> {
                    return ins1.getName().length() > ins2.getName().length()?-1:1;
                });
                break;
            case R.id.sort3:
                Log.e("sort", sortMode+" ");
                res.sort(Comparator.comparing(InstanceWithUri::getName));
//                res.sort((ins1, ins2) -> {
//                    return ins1.getName().compareTo(ins2.getName());
//                });
                break;
            case R.id.sort4:
                Log.e("sort", sortMode+" ");
                res.sort((ins1, ins2) -> ins2.getName().compareTo(ins1.getName()));
                res.sort((ins1, ins2) -> {
                    Collator collator = Collator.getInstance(Locale.CHINA);
                    CollationKey key1 = collator.getCollationKey(ins1.getName());
                    CollationKey key2 = collator.getCollationKey(ins2.getName());
                    return key1.compareTo(key2);
                });
                break;
            default:
                break;
        }
        switch (filterMode){
            case R.id.filter1:
                return res.stream().filter(ins->ins.getName().startsWith(queryWord)).collect(Collectors.toList());
            case R.id.filter2:
                return res.stream().filter(ins->ins.getName().endsWith(queryWord)).collect(Collectors.toList());
            case R.id.filter3:
                return res.stream().filter(ins->(!ins.getType().isEmpty())).collect(Collectors.toList());
            case R.id.filter4:
                return res.stream().filter(ins->ins.getType().isEmpty()).collect(Collectors.toList());
            default:
                break;
        }

        return res;
    }

    // 重排实体列表并渲染（不含网络请求）
    public void refreshAndDisplay(Vector<InstanceWithUri> res){
        requireActivity().runOnUiThread(() -> {

            for(InstanceWithUri ins : res){
                Log.i("Result", ins.getName() + "|" + ins.getType() + "|" + ins.getUri());
            }
            resultList.clear();
            resultList.addAll(sortAndFilterResults(res));
//                        resultList.addAll(res);
            for(InstanceWithUri ins : resultList){
                Log.i("Result", ins.getName() + "|" + ins.getType() + "|" + ins.getUri());
            }
            mRecyclerView.setAdapter(new InsAdapter());
        });
    }

    //load info
    public void getRetrievalResults() {
        new Thread(() -> {
            try {
                new Response().handle(this::refreshAndDisplay);
            } catch (JSONException | IOException | InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
    }
    public class Response {
        public void handle (FragmentQuesRetrieval.CallBack callBack) throws IOException, JSONException, InterruptedException {
            Vector<InstanceWithUri> res = MainActivity.dataLoader.getInstanceListByString(Utils.English(courses[courseId]), queryWord);
            callBack.onResponse(res);
        }
    }
    interface CallBack  {
        void onResponse(Vector<InstanceWithUri> res) throws IOException;
    }
    // end fetching thread

    /**
     * RecyclerView 的 Adapter
     */
    class InsAdapter extends RecyclerView.Adapter<FragmentQuesRetrieval.InsAdapter.ViewHolder> {

        @NonNull
        @NotNull
        @Override
        public FragmentQuesRetrieval.InsAdapter.ViewHolder onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.ins_item, parent, false);
            return new FragmentQuesRetrieval.InsAdapter.ViewHolder(v);
        }

        @SuppressLint("UseCompatLoadingForDrawables")
        @Override
        public void onBindViewHolder(@NonNull @NotNull FragmentQuesRetrieval.InsAdapter.ViewHolder holder, int position) {
            InstanceWithUri item = resultList.get(position);
            holder.ins_item.setOnClickListener(v -> showDetail(position));
            holder.ins_number.setOnClickListener(v -> showDetail(position));
            holder.ins_name.setOnClickListener(v -> showDetail(position));
            holder.ins_type.setOnClickListener(v -> showDetail(position));
            String number = String.valueOf(position + 1);
            holder.ins_number.setText(number);
            holder.ins_name.setText(item.getName());
            holder.ins_type.setText(item.getType());
        }

        @Override
        public int getItemCount() {
            return resultList.size();
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
     * 查看实体详情
     * @param pos 实体的序号
     */
    public void showDetail (int pos) {
        InstanceWithUri ins = resultList.get(pos);
        Log.i("showDetail", String.valueOf(pos + 1));

        FragmentInstance fi = new FragmentInstance(ins.getUri(), ins.getName(), Utils.English(courses[courseId]));
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


    protected void backSwitchFragment() throws Throwable {
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
        finalize();
    }

}