package com.example.catedu;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Message;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.widget.NestedScrollView;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.daquexian.flexiblerichtextview.FlexibleRichTextView;
import com.example.catedu.data.Ques;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;

import java.io.IOException;
import java.text.NumberFormat;
import java.util.Objects;
import java.util.Vector;

public class FragmentRecommend extends Fragment {
    Vector<Ques> ques_vec;
    String [] options; // ""表示未选择，否则A/B/C/D
    boolean done; // 是否答题完成
    int correct; // 正确数
    double correction; // 正确率

    ImageButton back_mine;

    RecyclerView rv_ques;
    Button sub_button;
    TextView acc_text;
    TextView no_ques;
    NestedScrollView nested_scroll;

    CustomPopWindow pop_window;

    AlertDialog alert;
    AlertDialog.Builder builder;

    FragmentRecommend (Vector<Ques> vec) {
        Log.e("FragmentInsQues", "New!");
        ques_vec = vec;
        done = false;
        correct = 0;
        correction = 0.0;
    }

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_recommend, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        rv_ques = view.findViewById(R.id.rv_ques);
        rv_ques.addItemDecoration(new DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL));
        rv_ques.setLayoutManager(new LinearLayoutManager(getContext()));
        sub_button = view.findViewById(R.id.submit_button);
        sub_button.setOnClickListener(v -> submitAnswer());
        sub_button.setVisibility(View.INVISIBLE);
        acc_text = view.findViewById(R.id.ques_acc);
        acc_text.setVisibility(View.GONE);
        no_ques = view.findViewById(R.id.no_ques);
        no_ques.setVisibility(View.GONE);
        nested_scroll = view.findViewById(R.id.nested_scroll);

        back_mine = view.findViewById(R.id.back_mine);
        back_mine.setOnClickListener(v -> {
            backSwitchFragment();
        });

        requireActivity().runOnUiThread(() -> {
            if (ques_vec.size() > 0) {
                rv_ques.setAdapter(new QuesAdapter());
                sub_button.setVisibility(View.VISIBLE);
            } else {
                no_ques.setText("暂无试题");
                no_ques.setVisibility(View.VISIBLE);
                nested_scroll.setVisibility(View.GONE);
            }
            Log.e("getQues", "QuesAdapter");
        });

        super.onViewCreated(view, savedInstanceState);
    }

    /**
     * RecyclerView 的 Adapter
     */
    class QuesAdapter extends RecyclerView.Adapter<FragmentRecommend.QuesAdapter.ViewHolder> {

        @NonNull
        @NotNull
        @Override
        public FragmentRecommend.QuesAdapter.ViewHolder onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.ques_item, parent, false);
            return new FragmentRecommend.QuesAdapter.ViewHolder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull @NotNull FragmentRecommend.QuesAdapter.ViewHolder holder, int position) {
            String title = "试题 " + (position + 1);
            holder.ques_title.setText(title);
            Ques ques = ques_vec.get(position);
            holder.ques_content.setText(ques.getqBody());

            if (!done) {
                holder.optA.setOnClickListener(v -> {
                    holder.optA.setImageResource(R.mipmap.a_orange_round);
                    holder.optB.setImageResource(R.mipmap.b_grey_round);
                    holder.optC.setImageResource(R.mipmap.c_grey_round);
                    holder.optD.setImageResource(R.mipmap.d_grey_round);
                    options[position] = "A";
                });
                holder.optB.setOnClickListener(v -> {
                    holder.optA.setImageResource(R.mipmap.a_grey_round);
                    holder.optB.setImageResource(R.mipmap.b_orange_round);
                    holder.optC.setImageResource(R.mipmap.c_grey_round);
                    holder.optD.setImageResource(R.mipmap.d_grey_round);
                    options[position] = "B";
                });
                holder.optC.setOnClickListener(v -> {
                    holder.optA.setImageResource(R.mipmap.a_grey_round);
                    holder.optB.setImageResource(R.mipmap.b_grey_round);
                    holder.optC.setImageResource(R.mipmap.c_orange_round);
                    holder.optD.setImageResource(R.mipmap.d_grey_round);
                    options[position] = "C";
                });
                holder.optD.setOnClickListener(v -> {
                    holder.optA.setImageResource(R.mipmap.a_grey_round);
                    holder.optB.setImageResource(R.mipmap.b_grey_round);
                    holder.optC.setImageResource(R.mipmap.c_grey_round);
                    holder.optD.setImageResource(R.mipmap.d_orange_round);
                    options[position] = "D";
                });
            } else { // 已答完
                String ans = options[position];
                holder.optA.setImageResource(R.mipmap.a_grey_round);
                holder.optB.setImageResource(R.mipmap.b_grey_round);
                holder.optC.setImageResource(R.mipmap.c_grey_round);
                holder.optD.setImageResource(R.mipmap.d_grey_round);
                switch (ans) {
                    case "A":
                        holder.optA.setImageResource(R.mipmap.a_red_round);
                        holder.optA.setBackgroundResource(R.drawable.button_false);
                        break;
                    case "B":
                        holder.optB.setImageResource(R.mipmap.b_red_round);
                        holder.optB.setBackgroundResource(R.drawable.button_false);
                        break;
                    case "C":
                        holder.optC.setImageResource(R.mipmap.c_red_round);
                        holder.optC.setBackgroundResource(R.drawable.button_false);
                        break;
                    case "D":
                        holder.optD.setImageResource(R.mipmap.d_red_round);
                        holder.optD.setBackgroundResource(R.drawable.button_false);
                        break;
                    default:
                        break;
                }
                String ref = ques_vec.get(position).getqAnswer();
                switch (ref) {
                    case "A":
                        holder.optA.setImageResource(R.mipmap.a_green_round);
                        holder.optA.setBackgroundResource(R.drawable.button_correct);
                        break;
                    case "B":
                        holder.optB.setImageResource(R.mipmap.b_green_round);
                        holder.optB.setBackgroundResource(R.drawable.button_correct);
                        break;
                    case "C":
                        holder.optC.setImageResource(R.mipmap.c_green_round);
                        holder.optC.setBackgroundResource(R.drawable.button_correct);
                        break;
                    case "D":
                        holder.optD.setImageResource(R.mipmap.d_green_round);
                        holder.optD.setBackgroundResource(R.drawable.button_correct);
                        break;
                    default:
                        break;
                }
            }

            holder.more_op.setOnClickListener(v -> {
                pop_window = new CustomPopWindow(position);
                pop_window.showAtLocation(v,
                        Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL, 0, 0);
                pop_window.setOnDismissListener(() -> pop_window.backgroundAlpha(1f));
            });

        }

        @Override
        public int getItemCount() {
            return ques_vec.size();
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            TextView ques_title;
            FlexibleRichTextView ques_content;
            ImageButton optA, optB, optC, optD;
            ImageButton more_op;
            public ViewHolder(@NonNull @NotNull View itemView) {
                super(itemView);
                ques_title = itemView.findViewById(R.id.ques_title);
                ques_content = itemView.findViewById(R.id.ques_content);
                optA = itemView.findViewById(R.id.option_a);
                optB = itemView.findViewById(R.id.option_b);
                optC = itemView.findViewById(R.id.option_c);
                optD = itemView.findViewById(R.id.option_d);
                more_op = itemView.findViewById(R.id.more_op);
            }
        }
    }

    @SuppressLint("SetTextI18n")
    public void submitAnswer () {
        done = true;
        sub_button.setVisibility(View.GONE);
        correct = 0;
        for (int i = 0; i < ques_vec.size(); ++i) {
            String ans = options[i];
            String ref = ques_vec.get(i).getqAnswer();
            if (ans.equals(ref)) correct++;
        }
        correction = ((float) correct) / ((float) ques_vec.size());
        NumberFormat nf = NumberFormat.getPercentInstance();
        nf.setMinimumFractionDigits(2);
        String accuracy = nf.format(correction);
        rv_ques.setAdapter(new QuesAdapter());
        acc_text.setText("正确率：" + accuracy);
        acc_text.setVisibility(View.VISIBLE);
    }

    public class CustomPopWindow extends PopupWindow {
        private final View view;
        private int position;

        public CustomPopWindow(int pos) {
            super();
            position = pos;
            LayoutInflater inflater = (LayoutInflater) requireContext().getSystemService(getContext().LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.widget_popupwindow, null);
            initView();
            initPopWindow();
        }

        private void initView() {
            ImageButton shareBtn = view.findViewById(R.id.button_share);
            ImageButton likeBtn = view.findViewById(R.id.button_like);
            TextView cancelTv = view.findViewById(R.id.share_cancel);
            TextView likeText = view.findViewById(R.id.like_text);

            shareBtn.setOnClickListener(v -> {
                doShareQues(position);
            });

            likeBtn.setVisibility(View.GONE);
            likeText.setVisibility(View.GONE);

            cancelTv.setOnClickListener(v -> dismiss());

        }

        private void initPopWindow() {
            this.setContentView(view);
            // 设置弹出窗体的宽
            this.setWidth(ViewGroup.LayoutParams.MATCH_PARENT);
            // 设置弹出窗体的高
            this.setHeight(ViewGroup.LayoutParams.WRAP_CONTENT);
            // 设置弹出窗体可点击()
            this.setFocusable(true);
            this.setOutsideTouchable(true);
            //设置SelectPicPopupWindow弹出窗体动画效果
            this.setAnimationStyle(R.style.mypopwindow_anim_style);
            ColorDrawable dw = new ColorDrawable(0x00FFFFFF);
            //设置弹出窗体的背景
            this.setBackgroundDrawable(dw);
            backgroundAlpha(0.5f); //0.0-1.0
        }

        public void backgroundAlpha(float bgAlpha) {
            WindowManager.LayoutParams lp = getActivity().getWindow().getAttributes();
            lp.alpha = bgAlpha;
            getActivity().getWindow().addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
            getActivity().getWindow().setAttributes(lp);
        }
    }

    public void doShareQues (int pos) {
        Ques ques = ques_vec.get(pos);
        String text = ques.toString();
        MainActivity main = (MainActivity) getActivity();
        assert main != null;

        LinearLayout share_pop = (LinearLayout) getLayoutInflater().inflate(R.layout.share_popupwindow, null);
        share_pop.setBackgroundResource(R.drawable.pop_border);
        TextView share_summary = share_pop.findViewById(R.id.share_summary);
        TextView cancel = share_pop.findViewById(R.id.share_cancel);
        TextView confirm = share_pop.findViewById(R.id.share_confirm);

        String summary;
        if (text.length() > 100) summary = text.substring(0, 50) + "......";
        else summary = text;
        share_summary.setText(summary);
        alert = null;
        builder = new AlertDialog.Builder(getContext());;
        builder.setView(share_pop);
        alert = builder.create();

        cancel.setOnClickListener(v -> {
            alert.dismiss();
        });
        confirm.setOnClickListener(v -> {
            main.doWeiboShare(text, "");
            alert.dismiss();
            pop_window.dismiss();
        });

        alert.show();
    }

    protected void backSwitchFragment() {
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
