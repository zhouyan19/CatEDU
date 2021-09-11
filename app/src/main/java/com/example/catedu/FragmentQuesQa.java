package com.example.catedu;

import static com.google.android.material.resources.MaterialResources.getDrawable;
import static cn.jiguang.imui.commons.models.IMessage.MessageType.RECEIVE_TEXT;
import static cn.jiguang.imui.commons.models.IMessage.MessageType.SEND_TEXT;

import android.annotation.SuppressLint;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.os.Message;
import android.text.Editable;
import android.text.Spannable;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Spinner;

import com.example.catedu.data.InstanceDetail;
import com.example.catedu.data.InstanceEnbedding;
import com.example.catedu.data.InstanceWithUri;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import cn.jiguang.imui.commons.ImageLoader;
import cn.jiguang.imui.commons.models.IMessage;
import cn.jiguang.imui.commons.models.IUser;
import cn.jiguang.imui.messages.MessageList;
import cn.jiguang.imui.messages.MsgListAdapter;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;
import java.util.Vector;


public class FragmentQuesQa extends Fragment {
    private final String[] courses = {"语文", "数学", "英语", "物理", "化学", "生物", "历史", "地理", "政治"};
    private int courseId = 0;
    private String question = "";
    ImageButton back_home;
    Spinner spinner;
    MessageList messageList;
    ImageLoader imageLoader;
    MsgListAdapter adapter;
    int selfie_num = (int)(Math.random() * 11) + 1;
    private static int inputDownHeightDiff;


    Button testBtn;
    EditText editText;



    public FragmentQuesQa() {
        // Required empty public constructor
    }


    // TODO: Rename and change types and number of parameters
    public static FragmentQuesQa newInstance(String param1, String param2) {
        FragmentQuesQa fragment = new FragmentQuesQa();
        Bundle args = new Bundle();

        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_ques_qa, container, false);
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
        editText = view.findViewById(R.id.edittext);
        spinner = view.findViewById(R.id.sp_course);
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<String>(this.getContext(), R.layout.nav_spinner_item, courses);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(spinnerAdapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) { courseId = pos; }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
        imageLoader = new ImageLoader() {
            @Override
            public void loadImage(ImageView imageView, String user) {
                if(user=="receive")
                    imageView.setImageResource(R.drawable.robot);
                else{
                    String image_name = "avatar_icon_" + selfie_num;
                    int src = getActivity().getResources().getIdentifier(image_name, "drawable", getActivity().getPackageName());
                    imageView.setImageResource(src);
                }

            }
        };

        messageList = view.findViewById(R.id.msg_list);
//        MsgListAdapter adapter = new MsgListAdapter<>("0", holdersConfig, imageLoader);
        adapter = new MsgListAdapter<>("0", imageLoader);
        messageList.setAdapter(adapter);




        testBtn = view.findViewById(R.id.btn_send);
        testBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                question = editText.getText().toString();
                appendMessage(question, SEND_TEXT, "send");
                editText.setText("");
                fetchAnswer();
            }
        });
        appendMessage("你好，有什么要问我的吗？", RECEIVE_TEXT, "receive");
        final View rootView = getActivity().getWindow().getDecorView();
//        final View rootView = getActivity().getWindow().getDecorView().findViewById(android.R.id.content);
        rootView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                if (inputDownHeightDiff < getHeightDiff()) {
                    MainActivity.nav_view.setVisibility(View.GONE);
                    Log.i("KeyboardChange", "Up");
                } else {
                    MainActivity.nav_view.setVisibility(View.VISIBLE);
                    Log.i("KeyboardChange", "Down");
                }
                //如果只想检测一次，需要注销
                //rootView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
            }
        });
        inputDownHeightDiff = getHeightDiff();
    }
    public int getHeightDiff() {
        final View rootView = getActivity().getWindow().getDecorView();
        Rect r = new Rect();
        rootView.getWindowVisibleDisplayFrame(r);
        Log.i("height", r.bottom + "   " + rootView.getRootView().getHeight());

//        DisplayMetrics dm = rootView.getResources().getDisplayMetrics();
        return rootView.getRootView().getHeight() - r.bottom;
    }

    @SuppressLint("Get Answer from dataloader")
    //load info
    public void fetchAnswer() {
        new Thread(() -> {
            try {
                new FragmentQuesQa.Response().handle(answer -> {


                    requireActivity().runOnUiThread(() -> {
                        appendMessage(answer, RECEIVE_TEXT, "receive");
                    });
                });
            } catch (JSONException | IOException | InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
    }
    public void appendMessage(String content, IMessage.MessageType type, String userTag){
        MyMessage myMessage = new MyMessage(content, type);
        myMessage.setUserInfo(new DefaultUser("00", "", userTag));
        adapter.addToStart(myMessage, true);
        messageList.setAdapter(adapter);
    }

    public class Response {
        public void handle (FragmentQuesQa.CallBack callBack) throws IOException, JSONException, InterruptedException {
            String answer = MainActivity.dataLoader.getAnswer(Utils.English(courses[courseId]), question);
            callBack.onResponse(answer);
        }
    }
    interface CallBack  {
        void onResponse(String answer) throws IOException;
    }

    public class MyMessage implements IMessage {

        private long id;
        private String text;
        private String timeString;
        private MessageType type;
        private IUser user;
        private String contentFile;
        private long duration;

        public MyMessage(String text, MessageType type) {
            this.text = text;
            this.type = type;
            this.id = UUID.randomUUID().getLeastSignificantBits();
        }

        @Override
        public String getMsgId() {
            return String.valueOf(id);
        }

        @Override
        public IUser getFromUser() {
            if (user == null) {
                return new DefaultUser("0", "user1", null);
            }
            return user;
        }

        public void setUserInfo(IUser user) {
            this.user = user;
        }

        public void setMediaFilePath(String path) {
            this.contentFile = path;
        }

        public void setDuration(long duration) {
            this.duration = duration;
        }

        @Override
        public long getDuration() {
            return duration;
        }

        public void setTimeString(String timeString) {
            this.timeString = timeString;
        }

        @Override
        public String getTimeString() {
            return timeString;
        }

        @Override
        public MessageType getType() {
            return type;
        }

        @Override
        public String getText() {
            return text;
        }

        @Override
        public String getMediaFilePath() {
            return contentFile;
        }
    }



    public class DefaultUser implements IUser {

        private String id;
        private String displayName;
        private String avatar;

        public DefaultUser(String id, String displayName, String avatar) {
            this.id = id;
            this.displayName = displayName;
            this.avatar = avatar;
        }

        @Override
        public String getId() {
            return id;
        }

        @Override
        public String getDisplayName() {
            return displayName;
        }

        @Override
        public String getAvatarFilePath() {
            return avatar;
        }
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