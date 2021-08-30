package com.example.catedu;

import android.os.Bundle;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.widget.NestedScrollView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.daquexian.flexiblerichtextview.FlexibleRichTextView;
import com.example.catedu.data.Ques;
import com.example.catedu.data.Subject;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;

import java.io.IOException;
import java.util.Vector;

public class FragmentInsRelated extends Fragment {
    String name;
    String course;
    Vector<Subject> sub_vec;

    RecyclerView rv_subjects;
    TextView no_subjects;
    NestedScrollView nested_scroll;

    FragmentInsRelated (String _n, String _c) {
        Log.e("FragmentInsRelated", "New!");
        name = _n;
        course = _c;
        sub_vec = new Vector<>();
        getSubjects();
    }

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_ins_related, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        rv_subjects = view.findViewById(R.id.rv_subjects);
        rv_subjects.addItemDecoration(new DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL));
        rv_subjects.setLayoutManager(new LinearLayoutManager(getContext()));
        no_subjects = view.findViewById(R.id.no_subjects);
        no_subjects.setVisibility(View.GONE);
        nested_scroll = view.findViewById(R.id.nested_scroll_sub);

        requireActivity().runOnUiThread(() -> {
            if (sub_vec.size() > 0) {
                rv_subjects.setAdapter(new FragmentInsRelated.SubAdapter());
            } else {
                no_subjects.setText("暂无相关实体");
                no_subjects.setVisibility(View.VISIBLE);
                nested_scroll.setVisibility(View.GONE);
            }
            Log.e("getSubjects", "SubAdapter");
        });

        super.onViewCreated(view, savedInstanceState);
    }

    public void getSubjects () {
        new Thread(() -> {
            try {
                new FragmentInsRelated.Response().handle(subjects_got -> {
                    sub_vec = subjects_got;
                    FragmentInstance.mHandler.sendMessage(new Message());
//                    requireActivity().runOnUiThread(() -> {
//                        if (sub_vec.size() > 0) {
//                            rv_subjects.setAdapter(new FragmentInsRelated.SubAdapter());
//                        } else {
//                            no_subjects.setText("暂无相关实体");
//                            no_subjects.setVisibility(View.VISIBLE);
//                            nested_scroll.setVisibility(View.GONE);
//                        }
//                        Log.e("getSubjects", "SubAdapter");
//                    });
                });
            } catch (JSONException | IOException | InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
    }

    public class Response {
        public void handle (FragmentInsRelated.CallBack callBack) throws IOException, JSONException, InterruptedException {
            Vector<Subject> subjects_got = MainActivity.dataLoader.getLinkSubjects(name, course);
            callBack.onResponse(subjects_got);
        }
    }
    interface CallBack {
        void onResponse(Vector<Subject> subjects_got);
    }

    /**
     * RecyclerView 的 Adapter
     */
    class SubAdapter extends RecyclerView.Adapter<FragmentInsRelated.SubAdapter.ViewHolder> {

        @NonNull
        @NotNull
        @Override
        public FragmentInsRelated.SubAdapter.ViewHolder onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.subject_item, parent, false);
            return new FragmentInsRelated.SubAdapter.ViewHolder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull @NotNull ViewHolder holder, int position) {
            Subject sub = sub_vec.get(position);
            holder.sub_number.setText(position + 1);
            holder.sub_pre.setText(sub.getPredicate());
            holder.sub_name.setText(sub.getName());
            if (sub.isType()) { // object
                holder.sub_name.setBackgroundResource(R.drawable.object_border);
            } else { // subject
                holder.sub_name.setBackgroundResource(R.drawable.subject_border);
            }
        }

        @Override
        public int getItemCount() {
            return sub_vec.size();
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            TextView sub_number, sub_pre, sub_name;
            public ViewHolder(@NonNull @NotNull View itemView) {
                super(itemView);
                sub_number = itemView.findViewById(R.id.sub_number);
                sub_pre = itemView.findViewById(R.id.sub_pre);
                sub_name = itemView.findViewById(R.id.sub_name);
            }
        }
    }

}
