package com.example.catedu;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Sensor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.github.ybq.android.spinkit.SpinKitView;
import com.googlecode.tesseract.android.TessBaseAPI;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Objects;

public class FragmentCamera extends Fragment {
    ImageButton camera_do;
    ImageButton back_home;

    SpinKitView skv;

    private String mDataPath = Environment.getExternalStorageDirectory().getPath() + "/tessdata";
    TessBaseAPI mTess;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_camera, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        skv = view.findViewById(R.id.spin_kit);
        skv.setVisibility(View.INVISIBLE);

        back_home = view.findViewById(R.id.detail_back_home);
        back_home.setOnClickListener(v -> {
            try {
                backSwitchFragment();
            } catch (Throwable throwable) {
                throwable.printStackTrace();
            }
        });

        camera_do = view.findViewById(R.id.button_camera);
        camera_do.setOnClickListener(v -> {
            MainActivity main = (MainActivity) getActivity();
            assert main != null;
            main.checkPermissionAndCamera();
        });

        //创建父目录
        File parentfile = new File(mDataPath);
        if (!parentfile.exists()){
            Log.e("file", "父目录创建");
            parentfile.mkdir();
        }
        copyLanguagePackageToSDCard(); //复制字库
        String lang = "chi_sim"; // 中文简体+英文
        mTess = new TessBaseAPI();
        mTess.init(Environment.getExternalStorageDirectory().getPath(), lang);
    }

    // 把字库文件拷贝到SD卡，要求SD卡根目录有tessdata文件夹，字库在该文件夹下
    private void copyLanguagePackageToSDCard() {
        String filePath = Environment.getExternalStorageDirectory().getPath() + "/tessdata/chi_sim.traineddata";
        try {
            File lvFile = new File(filePath);
            if (!lvFile.exists()) {
                Log.e("file", "创建路径");
                if (lvFile.createNewFile()) {
                    Log.e("file", "创建成功");
                } else {
                    Log.e("file", "创建失败");
                }
            } else {
                Log.e("file", "文件已存在");
            }
            InputStream lvInputStream;
            OutputStream lvOutputStream = new FileOutputStream(lvFile);
            // 拷贝文件
            lvInputStream = requireActivity().getAssets().open("chi_sim.traineddata");
            byte[] buffer = new byte[1024];
            int length = lvInputStream.read(buffer);
            while (length > 0) {
                lvOutputStream.write(buffer, 0, length);
                length = lvInputStream.read(buffer);
            }

            lvOutputStream.flush();
            lvInputStream.close();
            lvOutputStream.close();
        }
        catch (Exception e) {
            Log.e("OCR<copy>", e.getMessage());
        }
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

    public Handler mHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage (Message msg) {
            super.handleMessage(msg);
            Uri uri = (Uri) msg.obj;
            String path = getFilePathFromUri(getContext(), uri);
            Log.e("FragmentCamera", path);
            try {
                sendImage(path);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
    };

    public void sendImage(String path) throws FileNotFoundException {
        FileInputStream fs = new FileInputStream(path);
        Bitmap bitmap  = BitmapFactory.decodeStream(fs);
        new Thread(() -> {
//            String content = Utils.bitmapToString(bitmap);
//            Log.e("sendImage", content);
            requireActivity().runOnUiThread(() -> Toast.makeText(getContext(), "解析中...",Toast.LENGTH_SHORT).show());
            mTess.setImage(bitmap);
            Log.e("Bitmap set", "Begin parsing...");
            requireActivity().runOnUiThread(() -> skv.setVisibility(View.VISIBLE));
            String res = "";
            res = mTess.getUTF8Text();
            Log.e("OCR", res);
            String finalRes = res;
            requireActivity().runOnUiThread(() -> {
                skv.setVisibility(View.INVISIBLE);
                if (finalRes.equals("")) Toast.makeText(getContext(), "解析失败",Toast.LENGTH_SHORT).show();
                else Toast.makeText(getContext(), "解析成功",Toast.LENGTH_SHORT).show();
            });
        }).start();
    }

    public static String getFilePathFromUri(Context context, Uri contentUri) {
        File rootDataDir = context.getExternalFilesDir(null);
//        MyApplication.getMyContext().getExternalFilesDir(null).getPath();
        String fileName = getFileName(contentUri);
        if (!TextUtils.isEmpty(fileName)) {
            File copyFile = new File(rootDataDir + File.separator + fileName);
            copyFile(context, contentUri, copyFile);
            return copyFile.getAbsolutePath();
        }
        return null;
    }
    public static String getFileName(Uri uri) {
        if (uri == null) return null;
        String fileName = null;
        String path = uri.getPath();
        int cut = path.lastIndexOf('/');
        if (cut != -1) {
            fileName = path.substring(cut + 1);
        }
        return fileName;
    }

    public static void copyFile(Context context, Uri srcUri, File dstFile) {
        try {
            InputStream inputStream = context.getContentResolver().openInputStream(srcUri);
            if (inputStream == null) return;
            OutputStream outputStream = new FileOutputStream(dstFile);
            copyStream(inputStream, outputStream);
            inputStream.close();
            outputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static int copyStream(InputStream input, OutputStream output) throws Exception, IOException {
        final int BUFFER_SIZE = 1024 * 2;
        byte[] buffer = new byte[BUFFER_SIZE];
        BufferedInputStream in = new BufferedInputStream(input, BUFFER_SIZE);
        BufferedOutputStream out = new BufferedOutputStream(output, BUFFER_SIZE);
        int count = 0, n = 0;
        try {
            while ((n = in.read(buffer, 0, BUFFER_SIZE)) != -1) {
                out.write(buffer, 0, n);
                count += n;
            }
            out.flush();
        } finally {
            try {
                out.close();
            } catch (IOException e) {
            }
            try {
                in.close();
            } catch (IOException e) {
            }
        }
        return count;
    }


}
