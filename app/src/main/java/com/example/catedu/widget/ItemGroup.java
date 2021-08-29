package com.example.catedu.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.AttrRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.catedu.R;

public class ItemGroup extends FrameLayout {

    private LinearLayout itemGroupLayout; //组合控件的布局
    private TextView titleTv; //标题
    private TextView contentEdt; //输入框
    private ImageView jtRightIv; //向右的箭头

    public TextView getContentEdt() {
        return contentEdt;
    }

    public ItemGroup(@NonNull Context context) {
        super(context);
        initView(context);
    }

    public ItemGroup(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initView(context);
        initAttrs(context, attrs);
    }

    public ItemGroup(@NonNull Context context, @Nullable AttributeSet attrs, @AttrRes int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView(context);
        initAttrs(context,attrs);
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
    /**
     * 初始化相关属性，引入相关属性
     *
     * @param context
     * @param attrs
     */
    private void initAttrs(Context context, AttributeSet attrs) {
    }
}
