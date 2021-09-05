package com.example.catedu;

import static cn.jiguang.imui.commons.models.IMessage.MessageType.RECEIVE_TEXT;
import static cn.jiguang.imui.commons.models.IMessage.MessageType.SEND_TEXT;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;

import cn.jiguang.imui.commons.ImageLoader;
import cn.jiguang.imui.commons.models.IMessage;
import cn.jiguang.imui.commons.models.IUser;
import cn.jiguang.imui.messages.MessageList;
import cn.jiguang.imui.messages.MsgListAdapter;

import java.util.UUID;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link FragmentQuesQa#newInstance} factory method to
 * create an instance of this fragment.
 */
public class FragmentQuesQa extends Fragment {

    ImageButton back_home;
    MessageList messageList;
    ImageLoader imageLoader;

    Button testBtn;



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
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
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
        messageList = view.findViewById(R.id.msg_list);
//        MsgListAdapter adapter = new MsgListAdapter<>("0", holdersConfig, imageLoader);
        MsgListAdapter adapter = new MsgListAdapter<>("0", imageLoader);
        messageList.setAdapter(adapter);


        testBtn = view.findViewById(R.id.btn_test);
        testBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                adapter.addToStart(new MyMessage("测试提问", SEND_TEXT), true);
                messageList.setAdapter(adapter);
                try{
                    Thread.sleep(1000);
                }catch (Throwable e){}

                adapter.addToStart(new MyMessage("测试回答", RECEIVE_TEXT), true);
                messageList.setAdapter(adapter);

            }
        });

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