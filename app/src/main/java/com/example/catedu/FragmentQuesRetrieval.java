package com.example.catedu;

import android.os.Bundle;
import com.example.catedu.data.*;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ListView;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.FragmentTransaction;

import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.AdapterView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Vector;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link FragmentQuesRetrieval#newInstance} factory method to
 * create an instance of this fragment.
 */
public class FragmentQuesRetrieval extends Fragment {
    private DataLoader dataLoader;
    private final String[] mStrs = {"aaa", "bbb", "ccc", "airsaid"};
    private final String[] courses = {"语文", "数学", "英语", "物理", "化学", "生物", "历史", "地理", "政治"};
    int courseId = 0;
    String queryWord = "";
    SearchView mSearchView;
    ListView mListView;
    Spinner mSpinner;
    ImageButton back_home;

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public FragmentQuesRetrieval() {
        dataLoader = new DataLoader();
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment FragmentQuesRetrieval.
     */
    // TODO: Rename and change types and number of parameters
    public static FragmentQuesRetrieval newInstance(String param1, String param2) {
        FragmentQuesRetrieval fragment = new FragmentQuesRetrieval();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

//    @Override
//    public void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        if (getArguments() != null) {
//            mParam1 = getArguments().getString(ARG_PARAM1);
//            mParam2 = getArguments().getString(ARG_PARAM2);
//        }
//    }

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


        mSearchView = view.findViewById(R.id.sv_retrieval);
        mListView = view.findViewById(R.id.lv);
        mListView.setAdapter(new ArrayAdapter<String>(this.getContext(), android.R.layout.simple_list_item_1, mStrs));
        mListView.setTextFilterEnabled(true);

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

                getRetrievalResults();

                return false;
            }

            // 当搜索内容改变时触发该方法
            @Override
            public boolean onQueryTextChange(String newText) {
                if (!TextUtils.isEmpty(newText)) {
                    mListView.setFilterText(newText);
                } else {
                    mListView.clearTextFilter();
                }
                return false;
            }
        });
    }


    //load info
    public void getRetrievalResults() {
        new Thread(() -> {
            try {
                new Response().handle(res -> {
                    for(Triple tri : res){
                        Log.i("Result", tri.getS() + "  " + tri.getS() + "  " + tri.getS());
                    }
//                    instance = ins;
//                    Log.e("getInstanceDetail", instance.getEntity_name());
//                    String name = "实体名称：" + ((instance.getEntity_name().equals("")) ? "无" : instance.getEntity_name());
//                    String type = "实体类别：" + ((instance.getEntity_type().equals("")) ? "无" : instance.getEntity_type());
//                    requireActivity().runOnUiThread(() -> {
//                        detail_name.setText(name);
//                        detail_type.setText(type);
//                        JSONArray features = ins.getEntity_features();
//                        try {
//                            for (int i = 0; i < features.length(); ++i) {
//                                JSONObject feature = features.getJSONObject(i);
//                                feature_list.add(feature);
//                            }
//                        } catch (Exception e) {
//                            e.printStackTrace();
//                        }
//                        detail_feature.setAdapter(new FeatureAdapter());
//                        requireActivity().runOnUiThread(() -> skv.setVisibility(View.INVISIBLE));
//                        Log.e("getInstanceDetail", "FeatureAdapter");
//                    });
                });
            } catch (JSONException | IOException | InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
    }
    public class Response {
        public void handle (FragmentQuesRetrieval.CallBack callBack) throws IOException, JSONException, InterruptedException {
            Vector<Triple> res = dataLoader.getInstanceListByString(Utils.English(courses[courseId]), queryWord);
            callBack.onResponse(res);
        }
    }
    interface CallBack  {
        void onResponse(Vector<Triple> res) throws IOException;
    }
    // end fetching thread

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