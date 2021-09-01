package com.example.catedu;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
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
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.catedu.widget.RoundImageView;
import com.google.gson.internal.LinkedTreeMap;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class FragmentAvatar extends Fragment {

    RecyclerView mRecycleView;
    AvatarAdapter myAdapter;
    ArrayList<Integer> mData;
    Button submit;
    int submit_avatar = 0;
    private ImageButton backButton;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_avatar, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
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
        mData = new ArrayList<Integer>();
        for (int i = 0; i < 12; i++) {
            String image_name = "avatar_icon_" + String.valueOf(i + 1);
            int src = getActivity().getResources().getIdentifier(image_name, "drawable", getActivity().getPackageName());
            mData.add(src);
        }
    }

    void initview(View view) {
        submit = (Button) view.findViewById(R.id.submit_avatar);
        mRecycleView = (RecyclerView) view.findViewById(R.id.recycleview);


        myAdapter = new AvatarAdapter(getActivity(), mData);
        mRecycleView.setAdapter(myAdapter);//设置适配器
//        myAdapter.setOnAvatarClickListener();
        myAdapter.setOnAvatarClickListener(new AvatarAdapter.OnAvatarClickListener() {
            @Override
            public void onItemClick(int position) {
                submit_avatar = position + 1;
                getActivity().runOnUiThread(() -> {
                    for (int i = 0; i < myAdapter.getItemCount(); i++) {
                        if (i != position) {
                            ((RelativeLayout) (mRecycleView.getChildAt(i))).setBackgroundResource(0);
                        } else {
                            ((RelativeLayout) (mRecycleView.getChildAt(i))).setBackgroundResource(R.drawable.checked_avatar_corner);
                        }
                    }
                });


            }
        });
        //设置布局管理器 , 将布局设置成纵向
        mRecycleView.setLayoutManager(new GridLayoutManager(getActivity(), 3));
        //设置分隔线

        mRecycleView.setItemAnimator(new DefaultItemAnimator());

        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Map<String, Object> map = null;
                NetWorkTask netWorkTask = new NetWorkTask(3, getActivity().getSharedPreferences("user", Context.MODE_PRIVATE).getString("token", null), String.valueOf(submit_avatar));
                Thread newThread = new Thread(netWorkTask);
                newThread.start();
                try {
                    newThread.join();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                map = netWorkTask.getMap();
                if (map != null) {
                    boolean suc = (boolean) map.get("success");
                    String msg = (String) map.get("msg");
                    Toast.makeText(getActivity(), msg, Toast.LENGTH_SHORT).show();
                    if (suc) {
                        backSwitchFragment();
                    }
                } else {
                    Toast.makeText(getActivity(), "网络出错", Toast.LENGTH_SHORT).show();
                }
            }
        });

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

class MyViewHolder extends RecyclerView.ViewHolder {

    RoundImageView roundImageView;
    RelativeLayout relativeLayout;

    public MyViewHolder(View itemView) {
        super(itemView);
        relativeLayout = (RelativeLayout) itemView;
        roundImageView = (RoundImageView) itemView.findViewById(R.id.avatar);
    }

}

class AvatarAdapter extends RecyclerView.Adapter<MyViewHolder> {
    private LayoutInflater inflater;
    private Context mContext;
    private List<Integer> mDatas;
    // add click callback
    OnAvatarClickListener onAvatarClickListener;

    //创建构造参数
    public AvatarAdapter(Context context, List<Integer> datas) {
        this.mContext = context;
        this.mDatas = datas;
        inflater = LayoutInflater.from(context);
    }

    //创建ViewHolder
    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.avatar_item, parent, false);
        MyViewHolder viewHolder = new MyViewHolder(view);
        return viewHolder;
    }

    //绑定ViewHolder
    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        holder.roundImageView.setBackgroundResource(mDatas.get(position));
        holder.roundImageView.setTag(position);
        holder.relativeLayout.setTag(position);
        holder.roundImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Logger.e("FA", "clicked!");
                if (onAvatarClickListener != null)
                    onAvatarClickListener.onItemClick(holder.getAdapterPosition());
            }
        });
    }


    @Override
    public int getItemCount() {
        return mDatas.size();
    }

    public static interface OnAvatarClickListener {
        public void onItemClick(int position);
    }

    public void setOnAvatarClickListener(OnAvatarClickListener onAvatarClickListener) {
        this.onAvatarClickListener = onAvatarClickListener;
    }
}
