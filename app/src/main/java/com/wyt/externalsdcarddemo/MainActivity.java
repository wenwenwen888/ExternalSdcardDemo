package com.wyt.externalsdcarddemo;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.provider.DocumentFile;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.OutputStream;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends Activity {

    @Bind(R.id.version)
    TextView version;
    @Bind(R.id.query)
    Button query;
    @Bind(R.id.creat)
    Button creat;
    @Bind(R.id.progress)
    ProgressBar progressbar;
    @Bind(R.id.spaceinfo)
    TextView spaceinfo;
    @Bind(R.id.linearlayout)
    LinearLayout linearlayout;

    private QuerySpace querySpace;
    private String TFCardPath = null;
    //手机系统版本
    private int VersionSdk;
    //用于保存文件
    private SharedPreferences mSharedPreferences;
    private String uri_string;
    //用于判断跳转Intent类型
    private static final int DIRECTORY_CHOOSE_REQ_CODE = 1;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        VersionSdk = Build.VERSION.SDK_INT;

        version.setText("我的手机系统版本为:Android" + Build.VERSION.RELEASE + "(" + VersionSdk + ")");

        querySpace = new QuerySpace(this);

        //实例化SharedPreferences
        mSharedPreferences = getSharedPreferences("TFCardUri", 0);

    }

    @OnClick({R.id.query, R.id.creat})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.query:
                queryspace();
                break;
            case R.id.creat:
                creatfile();
                break;
        }
    }

    /**
     * 在外置SD卡创建文件
     */
    private void creatfile() {
        //获取本机SDK版本
        if (VersionSdk <= 19) {
            File file = null;
            try {
                file = new File(TFCardPath + "/WENWENWEN");
                if (!file.exists()) {
                    file.mkdirs();
                    Toast.makeText(this, "创建成功~", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "已经有此文件啦~", Toast.LENGTH_SHORT).show();
                }
            } catch (Exception e) {
                Toast.makeText(this, "创建失败~", Toast.LENGTH_SHORT).show();
            }
        } else {
            /**
             * 当存在外置SD卡的时候
             * 检查是否存在外置SD卡路径的uri
             * 没有就跳转获取,有就直接创建文件夹
             */
            uri_string = mSharedPreferences.getString("uri", null);
            if (uri_string == null || uri_string.equals("")) {
                Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
                startActivityForResult(intent, DIRECTORY_CHOOSE_REQ_CODE);
                Toast.makeText(this, "请选择外置SD卡的根目录~", Toast.LENGTH_SHORT).show();
            } else {
                mkdirsOnTFCard(Uri.parse(uri_string));
            }
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == DIRECTORY_CHOOSE_REQ_CODE) {
            //获取返回的Uri
            Uri uri = data.getData();
            //保存uri避免每次都调用Intent
            SharedPreferences.Editor mEditor = mSharedPreferences.edit();
            mEditor.putString("uri", uri.toString());
            mEditor.apply();
            mkdirsOnTFCard(uri);
        } else {
            Log.e("onActivityResult", "没有返回的resultCode");
        }
    }

    /**
     * DocumentFile外置SD卡创建文件夹
     */
    public void mkdirsOnTFCard(Uri uri) {
        //创建DocumentFile
        DocumentFile pickedDir = DocumentFile.fromTreeUri(this, uri);
        /**
         * 如果没有该文件夹,则创建一个新的文件并写入内容
         * 查询文件是否存在时,假如文件存在,则返回true;不存在时不会返回false,而是返回null
         * 所以这里应该用try-catch来判断,出现异常则不存在此文件
         */
        boolean ishasDirectory;
        try {
            ishasDirectory = pickedDir.findFile("WENWENWEN").exists();
        } catch (Exception e) {
            ishasDirectory = false;
        }
        if (!ishasDirectory) {
            try {
                //创建新的一个文件夹
                pickedDir.createDirectory("WENWENWEN");
                //找到新文件夹的路径
                pickedDir = pickedDir.findFile("WENWENWEN");
                //创建新的文件
                DocumentFile newFile = pickedDir.createFile("text/plain", "new_file");
                //写入内容到新建文件
                OutputStream out = getContentResolver().openOutputStream(newFile.getUri());
                if (out != null) {
                    out.write("测试".getBytes());
                    out.close();
                }
                Toast.makeText(this, "创建成功", Toast.LENGTH_SHORT).show();
            } catch (Exception e) {
                Toast.makeText(this, "创建失败", Toast.LENGTH_SHORT).show();
                Log.e("Exception", "DocumentFile创建失败:" + e);
            }
        }
    }

    /**
     * 查询外置SD卡路径,Android6.0查询也不用权限
     */
    private void queryspace() {
        //获取本机SDK版本
        VersionSdk = Build.VERSION.SDK_INT;
        //SDK为19即Android4.4
        if (VersionSdk == 19) {
            TFCardPath = querySpace.getTFCardPath1();
            if (TFCardPath == null) {
                Toast.makeText(this, "您的手机没有外置的SD卡哦~", Toast.LENGTH_SHORT).show();
            } else {
                calculateSpace();
            }
        } else {
            TFCardPath = querySpace.getTFCardPath2();
            if (TFCardPath == null) {
                Toast.makeText(this, "您的手机没有外置的SD卡哦~", Toast.LENGTH_SHORT).show();
            } else {
                long size = 0;
                //这里使用try-catch是因为在Android6.0中虽然有返回路径,但查询内存会报"路径无效"错误
                //而Android6.0以下查询内存为O
                try {
                    size = querySpace.getTotalPathMemorySize(TFCardPath);
                } catch (Exception e) {
                    size = 0;
                }
                if (size < 1) {
                    Toast.makeText(this, "您的手机没有外置的SD卡哦~", Toast.LENGTH_SHORT).show();
                } else {
                    calculateSpace();
                }
            }
        }

    }

    /**
     * 计算外置SD卡的内存使用情况
     */
    private void calculateSpace() {

        //查询可用与总内存
        long available = querySpace.getAvailablePathMemorySize(TFCardPath);
        long total = querySpace.getTotalPathMemorySize(TFCardPath);
        //已用内存
        long used = total - available;

        //计算已使用的百分比
        float usePercent = (float) available / (float) total;
        int progress = 100 - (int) (usePercent * 100);

        //已使用的空间转换为String类型
        String freeSize = querySpace.formatFileSize(available, false);
        String usedSize = querySpace.formatFileSize(used, false);
        String spaceInfo = "(已用:" + usedSize + "/可用:" + freeSize + ")";

        //显示信息
        linearlayout.setVisibility(View.VISIBLE);
        progressbar.setProgress(progress);
        spaceinfo.setText(spaceInfo);
    }
}
