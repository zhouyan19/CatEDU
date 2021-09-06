package com.example.catedu;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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

public class FragmentStars extends Fragment {

    RecyclerView mRecycleView;
    StarsAdapter myAdapter;
    ArrayList<String> StarsData;
    private ImageButton backButton;
    MainActivity main;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_stars, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        main=(MainActivity) getActivity();
        initdata();
        initview(view);
        backButton = view.findViewById(R.id.detail_back);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                backSwitchFragment();
            }
        });
    }

    private void initdata() {
        JSONObject jsonObject=main.readCache();
        StarsData = new ArrayList<String>();
        for (String str :
                jsonObject.keySet()) {
            StarsData.add(str);
        }
//        for (int i = 0; i < 12; i++) {
//            String Stars=String.valueOf(i);
//            StarsData.add(Stars);
//        }
    }

    void initview(View view) {
        mRecycleView = (RecyclerView) view.findViewById(R.id.recycleview);
        myAdapter = new StarsAdapter(getActivity(), StarsData);
        mRecycleView.setAdapter(myAdapter);//设置适配器

        mRecycleView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mRecycleView.addItemDecoration(new DividerItemDecoration(getActivity(),DividerItemDecoration.VERTICAL));
        mRecycleView.setItemAnimator(new DefaultItemAnimator());

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
        holder.textView.setText(mDatas.get(position));
    }


    @Override
    public int getItemCount() {
        return mDatas.size();
    }

}