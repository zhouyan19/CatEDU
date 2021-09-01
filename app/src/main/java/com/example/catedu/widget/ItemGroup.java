package com.example.catedu.widget;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.AttrRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentTransaction;

import com.example.catedu.FragmentChangeInfo;
import com.example.catedu.Logger;
import com.example.catedu.MainActivity;
import com.example.catedu.NetWorkTask;
import com.example.catedu.R;
import com.google.gson.internal.LinkedTreeMap;

import java.util.HashMap;
import java.util.Map;

public class ItemGroup extends FrameLayout {

    private LinearLayout itemGroupLayout; //组合控件的布局
    private TextView titleTv; //标题
    private TextView contentEdt; //输入框
    private ImageView jtRightIv; //向右的箭头
    AttributeSet attrs;
    TypedArray typedArray;

    public TextView getContentEdt() {
        return contentEdt;
    }

    public ItemGroup(@NonNull Context context) {
        super(context);
        initView(context);
    }

    public ItemGroup(@NonNull Context context, @Nullable AttributeSet attrs) throws InterruptedException {
        super(context, attrs);
        initView(context);
//        this.attrs=attrs;
        typedArray = context.obtainStyledAttributes(attrs, R.styleable.ItemGroup);
        initAttrs();
    }

    public ItemGroup(@NonNull Context context, @Nullable AttributeSet attrs, @AttrRes int defStyleAttr) throws InterruptedException {
        super(context, attrs, defStyleAttr);
        initView(context);
        typedArray = context.obtainStyledAttributes(attrs, R.styleable.ItemGroup);
        initAttrs();
    }

    //初始化View
    private void initView(Context context) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_group_layout, null);
        itemGroupLayout = (LinearLayout) view.findViewById(R.id.item_group_layout);
        titleTv = (TextView) view.findViewById(R.id.title_tv);
        contentEdt = (TextView) view.findViewById(R.id.content_edt);
        jtRightIv = (ImageView) view.findViewById(R.id.jt_right_iv);
        addView(view); //把自定义的这个组合控件的布局加入到当前FramLayout
    }


    public void initAttrs() throws InterruptedException {

        String title = typedArray.getString(R.styleable.ItemGroup_title);
        titleTv.setText(title);
        String url=getResources().getString(R.string.ip)+"/user/info";
        SharedPreferences sharedPref = getContext().getSharedPreferences("user", Context.MODE_PRIVATE);
        String token= sharedPref.getString("token",null);
        Map<String,Object> map=null;
        if(token!=null)
        {
            NetWorkTask netWorkTask=new NetWorkTask(url,"token",token);
            Thread newThread= new Thread(netWorkTask);
            newThread.start();
            newThread.join();
            map=netWorkTask.getMap();
//            Logger.e("Item88",map.toString());
        }
        if(map!=null)
        {
            String content_name=typedArray.getString(R.styleable.ItemGroup_edt_content);
            if(content_name!=null && !content_name.equals("")){
                LinkedTreeMap linkedTreeMap=(LinkedTreeMap) map.get("detail");
                String content=(String) linkedTreeMap.get(content_name);
//                Logger.e("ItemGroup",title);
                contentEdt.setText(content);
            }
        }
        else{
            Toast.makeText(getContext(),"获取信息失败",Toast.LENGTH_LONG);
        }

        jtRightIv.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                MainActivity.fragments.add(new FragmentChangeInfo(title));
                int from = MainActivity.last_fragment, to = MainActivity.fragments.size() - 1;
                FragmentTransaction transaction = MainActivity.getFragmentActivityFromView(view).getSupportFragmentManager().beginTransaction();
                transaction.hide(MainActivity.fragments.get(from));
                if (!MainActivity.fragments.get(to).isAdded())
                    transaction.add(R.id.nav_host_fragment, MainActivity.fragments.get(to));
                transaction.show(MainActivity.fragments.get(to)).commitAllowingStateLoss();
                MainActivity.last_fragment = to; // 更新
            }
        });
    }
}
