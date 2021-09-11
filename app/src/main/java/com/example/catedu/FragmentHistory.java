package com.example.catedu;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.alibaba.fastjson.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class FragmentHistory extends Fragment {

    RecyclerView mRecycleView;
    HistoryAdapter myAdapter;
    ArrayList<String> historyData;
    private ImageButton backButton;
    MainActivity main;
    Button clear_his;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_history, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        main = (MainActivity) getActivity();
        initdata();
        initview(view);
        clear_his=view.findViewById(R.id.clear_his);
        clear_his.setVisibility(View.INVISIBLE);

        backButton = view.findViewById(R.id.detail_back);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                backSwitchFragment();
            }
        });
        TextView title_text = view.findViewById(R.id.title_text);
        title_text.setText("本地缓存");
    }

    private void initdata() {
        historyData = new ArrayList<String>();
        JSONObject jsonObject = main.readCache();
        if (jsonObject != null && jsonObject.size() > 0) {

            for (String str :
                    jsonObject.keySet()) {
                JSONObject newJSON = new JSONObject();
                newJSON.put("insUri", str);
                newJSON.put("detail", jsonObject.get(str).toString());
                historyData.add(newJSON.toString());
            }
        } else {
            historyData.add("暂无本地缓存");
        }

    }

    void initview(View view) {
        mRecycleView = (RecyclerView) view.findViewById(R.id.recycleview);
        myAdapter = new HistoryAdapter(getActivity(), historyData);
        mRecycleView.setAdapter(myAdapter);//设置适配器

        mRecycleView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mRecycleView.addItemDecoration(new DividerItemDecoration(getActivity(), DividerItemDecoration.VERTICAL));
        mRecycleView.setItemAnimator(new DefaultItemAnimator());

        myAdapter.setOnHistoryClickListener(new HistoryAdapter.OnHistoryClickListener() {
            @Override
            public void onItemClick(int position) {
                RelativeLayout relativeLayout = (RelativeLayout) mRecycleView.getChildAt(position);
                TextView textView = relativeLayout.findViewById(R.id.history_item);
                String raw = historyData.get(position);
                JSONObject jsonObject = JSONObject.parseObject(raw);
                String uri = jsonObject.getString("insUri");
                JSONObject detail = JSONObject.parseObject(jsonObject.get("detail").toString());
                String name = detail.getString("entity_name");
                String course = detail.getString("course");
                Logger.e("FS", "onclick");
                MainActivity.fragments.add(new FragmentInsDetail(uri, name, course, true));
                forwardSwitchFragment();
            }
        });
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

    public void backSwitchFragment() {
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

class HistoryViewHolder extends RecyclerView.ViewHolder {

    TextView textView;
    RelativeLayout relativeLayout;

    public HistoryViewHolder(View itemView) {
        super(itemView);
        relativeLayout = (RelativeLayout) itemView;
        textView = (TextView) itemView.findViewById(R.id.history_item);
    }

}

class HistoryAdapter extends RecyclerView.Adapter<HistoryViewHolder> {
    private LayoutInflater inflater;
    private Context mContext;
    private List<String> mDatas;
    // add click callback
    OnHistoryClickListener onHistoryClickListener;

    //创建构造参数
    public HistoryAdapter(Context context, List<String> datas) {
        this.mContext = context;
        this.mDatas = datas;
        inflater = LayoutInflater.from(context);
    }

    //创建ViewHolder
    @Override
    public HistoryViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.history_item, parent, false);
        HistoryViewHolder viewHolder = new HistoryViewHolder(view);
        return viewHolder;
    }

    //绑定ViewHolder
    @Override
    public void onBindViewHolder(HistoryViewHolder holder, int position) {
        String raw = mDatas.get(position);
        if (raw == "暂无本地缓存") {
            holder.textView.setText(raw);
        } else {
            JSONObject jsonObject = JSONObject.parseObject(raw);
            JSONObject detail = JSONObject.parseObject(jsonObject.get("detail").toString());
            String name = detail.getString("entity_name");
            holder.textView.setText(name);
            holder.textView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (onHistoryClickListener != null)
                        onHistoryClickListener.onItemClick(holder.getAdapterPosition());
                }
            });
        }
    }


    @Override
    public int getItemCount() {
        return mDatas.size();
    }

    public static interface OnHistoryClickListener {
        public void onItemClick(int position);
    }

    public void setOnHistoryClickListener(HistoryAdapter.OnHistoryClickListener onHistoryClickListener) {
        this.onHistoryClickListener = onHistoryClickListener;
    }

}
