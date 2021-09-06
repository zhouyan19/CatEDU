package com.example.catedu;

import static androidx.core.content.ContextCompat.getSystemService;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Rect;
import android.os.Bundle;

import com.example.catedu.data.*;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.text.Editable;
import android.text.Spannable;
import android.text.TextWatcher;
import android.text.style.ForegroundColorSpan;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.AdapterView;
import android.widget.TextView;

import org.angmarch.views.NiceSpinner;
import org.jetbrains.annotations.NotNull;
import org.json.JSONException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;
import java.util.Vector;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link FragmentQuesLink#newInstance} factory method to
 * create an instance of this fragment.
 */
public class FragmentQuesLink extends Fragment {
    private final String[] historyStrs = { };

    private final String[] courses = {"语文", "数学", "英语", "物理", "化学", "生物", "历史", "地理", "政治"};
    private static Vector<InstanceWithUri> resultList;
    int courseId = 0;
    String queryText = "";
//    SearchView mSearchView;
    private EditText input;
    ListView mListView;
    RecyclerView mRecyclerView;

    Spinner mSpinner;
    ImageButton back_home;
    Button mBtn;
    private static int inputDownHeightDiff;

    public FragmentQuesLink() {
        resultList = new Vector<>();
    }


    // TODO: Rename and change types and number of parameters
    public static FragmentQuesLink newInstance(String param1, String param2) {
        FragmentQuesLink fragment = new FragmentQuesLink();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        return inflater.inflate(R.layout.fragment_ques_link, container, false);
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

        input = view.findViewById(R.id.edittext);

//        mSearchView = view.findViewById(R.id.sv_retrieval);
//        mListView = view.findViewById(R.id.lv);
//        mListView.setAdapter(new ArrayAdapter<String>(this.getContext(), android.R.layout.simple_list_item_1, historyStrs));
//        mListView.setTextFilterEnabled(true);

        mSpinner = view.findViewById(R.id.sp_course);
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<String>(this.getContext(), R.layout.nav_spinner_item, courses);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mSpinner.setAdapter(spinnerAdapter);
//        List<String> courseList = new LinkedList<String>(Arrays.asList(courses));
//        mSpinner.attachDataSource(courseList);

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

        mBtn = view.findViewById(R.id.btn_search);
        mBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                queryText = input.getText().toString();
                Log.i("String query", queryText);
                clearTextSpan();
//                input.clearFocus();
                getRetrievalResults();
                //hide input method
                InputMethodManager im =(InputMethodManager)getActivity().getApplicationContext().
                        getSystemService(Context.INPUT_METHOD_SERVICE);
                im.hideSoftInputFromWindow(view.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
            }
        });



//        mSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
//            // 当点击搜索按钮时触发该方法
//            @Override
//            public boolean onQueryTextSubmit(String query) {
//                Log.i("String query", query);
//                queryText = query;
//                getRetrievalResults();
//                return false;
//            }
//            // 当搜索内容改变时触发该方法
//            @Override
//            public boolean onQueryTextChange(String newText) {
////                if (!TextUtils.isEmpty(newText)) {
////                    mListView.setFilterText(newText);
////                } else {
////                    mListView.clearTextFilter();
////                }
//                return false;
//            }
//        });
        input.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) { }
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) { }
            @Override
            public void afterTextChanged(Editable editable) {
                clearTextSpan();
            }
        });

        input.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    mBtn.setVisibility(View.VISIBLE);
                } else {
                    // 此处为失去焦点时的处理内容
                    mBtn.setVisibility(View.GONE);
                }
            }
        });


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


    // 去除高亮效果
    private void clearTextSpan(){
        Editable et = input.getText();
        ForegroundColorSpan[] toRemoveSpans = et.getSpans(0, et.length(), ForegroundColorSpan.class);
        for (int i = 0; i < toRemoveSpans.length; i++)
            et.removeSpan(toRemoveSpans[i]);
    }

    //load info
    public void getRetrievalResults() {
        new Thread(() -> {
            try {
                new Response().handle(res -> {
                    // remove duplicate
//                    Set<InstanceWithUri> set = new TreeSet<>(new Comparator<InstanceWithUri>() {
//                        @Override
//                        public int compare(InstanceWithUri insA, InstanceWithUri insB) {
//                            return insA.getName().hashCode() - insB.getName().hashCode(); //要实现两个对象大小可比
//                        }
//                    });
//                    for(InstanceEnbedding insEnb : res)
//                        set.add(insEnb.getInstanceWithUri());
//                    resultList.clear();
//                    resultList.addAll(set);
                    // remove duplicate while keeping order
                    for(InstanceEnbedding insEnb : res)
                        if(!resultList.contains(insEnb.getInstanceWithUri()))
                            resultList.add(insEnb.getInstanceWithUri());


                    requireActivity().runOnUiThread(() -> {
                        // set highlight span
                        Editable editable = input.getText();
                        for(InstanceEnbedding insEnb : res) {
                            Log.i("Enbed", insEnb.getInstanceWithUri().getName() + "  " + insEnb.getStart() + "  " + insEnb.getEnd());
                            editable.setSpan(
                                    new ForegroundColorSpan(ContextCompat.getColor(requireContext(),R.color.main_highlight)),
                                    insEnb.getStart(), insEnb.getEnd() + 1,
                                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                        }
                        for(InstanceWithUri ins : resultList)
                            Log.i("Result", ins.getName() + "  " + ins.getType() + "  " + ins.getUri());
                        mRecyclerView.setAdapter(new InsAdapter());
                    });
                });
            } catch (JSONException | IOException | InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
    }
    public class Response {
        public void handle (FragmentQuesLink.CallBack callBack) throws IOException, JSONException, InterruptedException {
            Vector<InstanceEnbedding> res = MainActivity.dataLoader.getLinkInstanceList(Utils.English(courses[courseId]), queryText);
            callBack.onResponse(res);
        }
    }
    interface CallBack  {
        void onResponse(Vector<InstanceEnbedding> res) throws IOException;
    }
    // end fetching thread

    /**
     * RecyclerView 的 Adapter
     */
    class InsAdapter extends RecyclerView.Adapter<FragmentQuesLink.InsAdapter.ViewHolder> {

        @NonNull
        @NotNull
        @Override
        public FragmentQuesLink.InsAdapter.ViewHolder onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.ins_item, parent, false);
            return new FragmentQuesLink.InsAdapter.ViewHolder(v);
        }

        @SuppressLint("UseCompatLoadingForDrawables")
        @Override
        public void onBindViewHolder(@NonNull @NotNull FragmentQuesLink.InsAdapter.ViewHolder holder, int position) {
            InstanceWithUri item = resultList.get(position);
            holder.ins_item.setOnClickListener(v -> showDetail(position));
            holder.ins_number.setOnClickListener(v -> showDetail(position));
            holder.ins_name.setOnClickListener(v -> showDetail(position));
            String number = String.valueOf(position + 1);
            holder.ins_number.setText(number);
            holder.ins_name.setText(item.getName());
        }

        @Override
        public int getItemCount() {
            return resultList.size();
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