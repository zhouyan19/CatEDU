package com.example.catedu;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import com.example.catedu.data.Ques;
import com.github.ybq.android.spinkit.SpinKitView;
import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Collections;
import java.util.HashMap;
import java.util.Vector;

public class FragmentWaiting extends Fragment {
    private boolean logged;
    private String id;
    SpinKitView skv;
    ImageButton backMine;
    int toLoadNum;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        return inflater.inflate(R.layout.fragment_waiting, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        backMine=(ImageButton) view.findViewById(R.id.back_mine);
        backMine.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                backSwitchFragment();
            }
        });
        skv=view.findViewById(R.id.recSpin);
        skv.setVisibility(View.VISIBLE);
        new Thread(()->{
            runThread();
        }).start();
//        new Handler().postDelayed(new Runnable() {
//            @Override
//            public void run() {
//                runThread();
//            }
//        },1000); // ??????1???
        super.onViewCreated(view, savedInstanceState);
    }
    public void backSwitchFragment() {
        int from = MainActivity.last_fragment, to;
        FragmentTransaction transaction = requireActivity().getSupportFragmentManager().beginTransaction();
        transaction.hide(MainActivity.fragments.get(from));
        if (MainActivity.last_fragment == 3) { //????????????
            to = MainActivity.major_fragment;
        } else { //????????????
            to = MainActivity.last_fragment - 1;
        }
        if (!MainActivity.fragments.get(to).isAdded())
            transaction.add(R.id.nav_host_fragment, MainActivity.fragments.get(to));
        transaction.show(MainActivity.fragments.get(to)).commitAllowingStateLoss();
        MainActivity.last_fragment = to; //??????
        MainActivity.fragments.removeElementAt(from); //??????????????????
    }

    void runThread(){
        Vector<Ques> vecQues = new Vector<>();
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("user", Context.MODE_PRIVATE);
        String token = sharedPreferences.getString("token", null);
        if (token != null) {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("token", token);
            NetWorkTask netWorkTask = new NetWorkTask(5, token, jsonObject.toString());
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
            Thread[] threadList=new Thread[jsonArray.size()];
            toLoadNum=jsonArray.size();
            for (int i = 0; i < jsonArray.size(); i++) {
                final int index = i;
                threadList[i] = new Thread(() -> {
                    JSONObject jsonObject1 = (JSONObject) jsonArray.get(index);
                    JSONObject detail = JSONObject.parseObject(jsonObject1.get("detail").toString());
                    String name = detail.getString("entity_name");
                    Logger.e("FW",name);
                    Vector<Ques> tmpVec = null;
                    try {
                        new Response().handle(name,quesVec->{
                            if (quesVec != null)
                                vecQues.addAll(quesVec);
                        });
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                });
                threadList[i].start();
            }
            for (int i = 0; i < threadList.length; i++) {
                try {
                    threadList[i].join();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            Collections.shuffle(vecQues);
            int end = vecQues.size();
            if (end > 10)
                end = 10;

            Vector<Ques> resVec = new Vector<>();
            for (int i = 0; i < end; i++) {
                resVec.add(vecQues.get(i));
            }
//            skv.setVisibility(View.INVISIBLE);
            backSwitchFragment();
            MainActivity.fragments.add(new FragmentRecommend(resVec));
            forwardSwitchFragment();

        } else {
            Looper.prepare();
            Toast.makeText(getActivity(), "?????????", Toast.LENGTH_SHORT).show();
            Looper.loop();
        }
    }
    public class Response {

        public void handle(String name, CallBack callBack) throws InterruptedException, IOException {
            Vector<Ques> quesVector=getInstanceQues(name);
            toLoadNum-=1;
            Logger.e("FW157",quesVector.toString());
            callBack.onResponse(quesVector);
        };
    }
    interface CallBack  {
        void onResponse(Vector<Ques> quesVector);
    }
    public Vector<Ques> getInstanceQues(String name) throws IOException, JSONException, InterruptedException {
        if (!logged) logIn();
        String root = "http://open.edukg.cn/opedukg/api/typeOpen/open/questionListByUriName?uriName=";
        String tail = "&id=" + id;
        URL url = new URL(root + name + tail);
        Logger.e("FW169",url.toString());
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        //Get???????????????DoOutPut
        conn.setDoOutput(false);
        conn.setDoInput(true);
        //?????????????????????????????????????????????

        conn.setConnectTimeout(12000);
        conn.setReadTimeout(12000);

        conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
        //???????????????
        conn.connect();
        // ???????????????????????????Reader??????
        BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
        StringBuilder result = new StringBuilder();
        String line;
        while ((line = in.readLine()) != null) {
            result.append(line);
        }
        in.close();
        JSONObject json = JSONObject.parseObject(result.toString());
        Logger.e("FW192",json.toString());
        return getQuesFromJson(json);
    }

    public Vector<Ques> getQuesFromJson(JSONObject json) throws JSONException {
        Vector<Ques> vector = new Vector<>();
        Gson gson = new Gson(); // ?????? Gson ??????
        JSONArray data = json.getJSONArray("data");
        if (data==null||data.size() == 0) return vector;
        else {
            for (int i = 0; i < data.size(); ++i) {
                JSONObject item = data.getJSONObject(i);
                Ques ques = gson.fromJson(String.valueOf(item), Ques.class);
                String answer = ques.getqAnswer();

                // ???????????????
                if (answer.equals("A") ||
                        answer.equals("B") ||
                        answer.equals("C") ||
                        answer.equals("D")) {
                    ques.print();
                    vector.add(ques);
                }
            }
        }
        return vector;
    }

    public void logIn() throws InterruptedException {
        Thread net_conn_thread = new Thread(() -> {
            try {
                logInOnSubThread();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        });
        net_conn_thread.start();
        net_conn_thread.join();
    }

    /**
     * ?????? POST ?????????????????? ????????? id
     */
    public void logInOnSubThread() throws IOException, JSONException {
        String phone = "18357331593"; // zy????????????
        String password = "2021zyywtwzt"; // zy?????????
        URL login_url = new URL("http://open.edukg.cn/opedukg/api/typeAuth/user/login");
        HttpURLConnection conn = (HttpURLConnection) login_url.openConnection(); // ??????HttpURLConnection??????
        conn.setRequestMethod("POST"); // ??????????????? POST
        conn.setConnectTimeout(12000); // ????????????
        conn.setReadTimeout(12000);
        conn.setDoOutput(true);
        conn.setDoInput(true);
        conn.setUseCaches(false); // Post??????????????????,??????????????????false
        conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8"); // ???????????????
        conn.connect();

        // ????????????
        HashMap map = new HashMap<String, String>();
        map.put("password", password);
        map.put("phone", phone);
        String params = new Gson().toJson(map);

        // ??????????????????????????????
        OutputStream out = conn.getOutputStream();
        out.write(params.getBytes());
        out.flush();
        out.close();

        // ????????????
        StringBuilder res = new StringBuilder();
        int code = conn.getResponseCode();
        Log.e("Response", String.valueOf(code));
        if (code == 200) {
            Logger.e("LogIn", "???????????????");
            logged = true;
            InputStreamReader in = new InputStreamReader(conn.getInputStream());
            BufferedReader bf = new BufferedReader(in);
            String line;
            // ??????????????????
            while ((line = bf.readLine()) != null) {
                res.append(line);
            }
            in.close();
            conn.disconnect();
        }

        if (res.toString().equals("")) {
            Logger.e("LogIn", "Fail");
            return;
        }
        JSONObject login_json = JSONObject.parseObject(res.toString());
        id = login_json.getString("id");
    }

    protected void forwardSwitchFragment() {
        int from = MainActivity.last_fragment, to = MainActivity.fragments.size() - 1;
        FragmentTransaction transaction = requireActivity().getSupportFragmentManager().beginTransaction();
        transaction.hide(MainActivity.fragments.get(from));
        if (!MainActivity.fragments.get(to).isAdded())
            transaction.add(R.id.nav_host_fragment, MainActivity.fragments.get(to));
        transaction.show(MainActivity.fragments.get(to)).commitAllowingStateLoss();
        MainActivity.last_fragment = to; // ??????
    }
}
