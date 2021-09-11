package com.example.catedu;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class FragmentStars extends Fragment {

    RecyclerView mRecycleView;
    StarsAdapter myAdapter;
    ArrayList<String> StarsData;
    private ImageButton backButton;
    MainActivity main;
    Button clear;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_stars, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        main = (MainActivity) getActivity();
        initdata();
        initview(view);
        clear=view.findViewById(R.id.clear);
        clear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SharedPreferences sharedPreferences = getActivity().getSharedPreferences("user", Context.MODE_PRIVATE);
                String token = sharedPreferences.getString("token", null);
                if(token==null)
                {
                    Toast.makeText(getActivity(), "未登录", Toast.LENGTH_SHORT).show();
                    return;
                }
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("token",token);
                NetWorkTask netWorkTask = new NetWorkTask(9, token, jsonObject.toString());
                Thread newThread = new Thread(netWorkTask);
                newThread.start();
                try {
                    newThread.join();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                String res = netWorkTask.getRes();
                JSONObject resJson = JSONObject.parseObject(res);
                boolean success=resJson.getBoolean("success");
                if(success){
                    Toast.makeText(getActivity(), "清除收藏成功", Toast.LENGTH_SHORT).show();
                    backSwitchFragment();
                }
                else {
                    Toast.makeText(getActivity(), "清除失败", Toast.LENGTH_SHORT).show();
                }
            }
        });
        backButton = view.findViewById(R.id.detail_back);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                backSwitchFragment();
            }
        });
    }

    private void initdata() {

        StarsData = new ArrayList<String>();
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("user", Context.MODE_PRIVATE);
        String token = sharedPreferences.getString("token", null);
        if (token != null) {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("token", token);
            NetWorkTask netWorkTask = new NetWorkTask(6, token, jsonObject.toString());
            Thread newThread = new Thread(netWorkTask);
            newThread.start();
            try {
                newThread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            String res = netWorkTask.getRes();
            JSONObject resJson = JSONObject.parseObject(res);
            JSONArray jsonArray = (JSONArray) resJson.get("detail");
            if (jsonArray.size() == 0) {
                StarsData.add("尚无收藏记录");
            } else {
                for (int i = 0; i < jsonArray.size(); i++) {
                    StarsData.add(jsonArray.get(i).toString());
                }
            }
        } else {
            StarsData.add("未登录");
        }

    }

    void initview(View view) {
        mRecycleView = (RecyclerView) view.findViewById(R.id.recycleview);
        myAdapter = new StarsAdapter(getActivity(), StarsData);
        mRecycleView.setAdapter(myAdapter);//设置适配器

        mRecycleView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mRecycleView.addItemDecoration(new DividerItemDecoration(getActivity(), DividerItemDecoration.VERTICAL));
        mRecycleView.setItemAnimator(new DefaultItemAnimator());

        myAdapter.setOnStarsClickListener(new StarsAdapter.OnStarsClickListener() {
            @Override
            public void onItemClick(int position) {
                RelativeLayout relativeLayout = (RelativeLayout) mRecycleView.getChildAt(position);
                TextView textView = relativeLayout.findViewById(R.id.stars_item);
                String raw = StarsData.get(position);
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

class StarsViewHolder extends RecyclerView.ViewHolder {

    TextView textView;
    RelativeLayout relativeLayout;

    public StarsViewHolder(View itemView) {
        super(itemView);
        relativeLayout = (RelativeLayout) itemView;
        textView = (TextView) itemView.findViewById(R.id.stars_item);

    }

}

class StarsAdapter extends RecyclerView.Adapter<StarsViewHolder> {
    private LayoutInflater inflater;
    private Context mContext;
    private List<String> mDatas;
    // add click callback
    OnStarsClickListener onStarsClickListener;

    //创建构造参数
    public StarsAdapter(Context context, List<String> datas) {
        this.mContext = context;
        this.mDatas = datas;
        inflater = LayoutInflater.from(context);
    }

    //创建ViewHolder
    @Override
    public StarsViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.stars_item, parent, false);
        StarsViewHolder viewHolder = new StarsViewHolder(view);
        return viewHolder;
    }

    //绑定ViewHolder
    @Override
    public void onBindViewHolder(StarsViewHolder holder, int position) {

        String raw = mDatas.get(position);
        if (raw.equals("未登录") || raw.equals("尚无收藏记录")) {
            holder.textView.setText(raw);
        } else {
            JSONObject jsonObject = JSONObject.parseObject(raw);
            JSONObject detail = JSONObject.parseObject(jsonObject.get("detail").toString());
            String name = detail.getString("entity_name");
            holder.textView.setText(name);
            holder.textView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (onStarsClickListener != null)
                        onStarsClickListener.onItemClick(holder.getAdapterPosition());
                }
            });
        }

    }


    @Override
    public int getItemCount() {
        return mDatas.size();
    }

    public static interface OnStarsClickListener {
        public void onItemClick(int position);
    }

    public void setOnStarsClickListener(StarsAdapter.OnStarsClickListener onStarsClickListener) {
        this.onStarsClickListener = onStarsClickListener;
    }

}
