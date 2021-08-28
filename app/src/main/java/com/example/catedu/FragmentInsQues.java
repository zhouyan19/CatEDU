package com.example.catedu;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.widget.NestedScrollView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DecodeFormat;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.target.Target;
import com.daquexian.flexiblerichtextview.FlexibleRichTextView;
import com.example.catedu.data.InstanceDetail;
import com.example.catedu.data.Ques;
import com.github.ybq.android.spinkit.SpinKitView;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.NumberFormat;
import java.util.Queue;
import java.util.Vector;
import java.util.logging.Logger;


public class FragmentInsQues extends Fragment {
    String name;
    Vector<Ques> ques_vec;
    String [] options; // ""表示未选择，否则A/B/C/D
    boolean done; // 是否答题完成
    int correct; // 正确数
    double correction; // 正确率

    SpinKitView skv;
    RecyclerView rv_ques;
    Button sub_button;
    TextView acc_text;
    TextView no_ques;
    NestedScrollView nested_scroll;

    FragmentInsQues (String _n) {
        Log.e("FragmentInsQues", "New!");
        name = _n;
        ques_vec = new Vector<>();
        done = false;
        correct = 0;
        correction = 0.0;
    }

    public boolean hasQues() {
        Log.e("Number of ques", String.valueOf(ques_vec.size()));
        return (ques_vec.size() > 0);
    }

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_ins_ques, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        skv = view.findViewById(R.id.spin_kit);
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

        getQues();

        super.onViewCreated(view, savedInstanceState);
    }

    public void getQues () {
        new Thread(() -> {
            try {
                new FragmentInsQues.Response().handle(ques_got -> {
                    ques_vec = ques_got;
                    options = new String[ques_vec.size()];
                    for (int i = 0; i < ques_vec.size(); ++i) options[i] = "";
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
                });
            } catch (JSONException | IOException | InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
    }
    public class Response {
        public void handle (FragmentInsQues.CallBack callBack) throws IOException, JSONException, InterruptedException {
            Vector<Ques> ques_got = MainActivity.dataLoader.getInstanceQues(name);
            callBack.onResponse(ques_got);
        }
    }
    interface CallBack {
        void onResponse(Vector<Ques> ques_got);
    }

    /**
     * RecyclerView 的 Adapter
     */
    class QuesAdapter extends RecyclerView.Adapter<FragmentInsQues.QuesAdapter.ViewHolder> {

        @NonNull
        @NotNull
        @Override
        public FragmentInsQues.QuesAdapter.ViewHolder onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.ques_item, parent, false);
            return new FragmentInsQues.QuesAdapter.ViewHolder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull @NotNull ViewHolder holder, int position) {
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

        }

        @Override
        public int getItemCount() {
            return ques_vec.size();
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            TextView ques_title;
            FlexibleRichTextView ques_content;
            ImageButton optA, optB, optC, optD;
            public ViewHolder(@NonNull @NotNull View itemView) {
                super(itemView);
                ques_title = itemView.findViewById(R.id.ques_title);
                ques_content = itemView.findViewById(R.id.ques_content);
                optA = itemView.findViewById(R.id.option_a);
                optB = itemView.findViewById(R.id.option_b);
                optC = itemView.findViewById(R.id.option_c);
                optD = itemView.findViewById(R.id.option_d);
            }
        }
    }

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

}
