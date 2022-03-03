package central.stu.fucklegym;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.icu.text.UnicodeSetSpanner;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.Random;

import fucklegym.top.entropy.PathGenerator;
import fucklegym.top.entropy.User;

class InitUserThread extends Thread {
    private User user;
    private Activity activity;
    private Handler handler;

    public InitUserThread(User user, Activity activity, Handler handler) {
        this.user = user;
        this.handler = handler;
        this.activity = activity;
    }

    public void run() {
        try {
            user.login();
            Message msg = handler.obtainMessage();
            msg.what = FreeRun.WHAT_UPDATE_RESTMILEAGE;
            msg.obj = Double.valueOf(-user.getTotalDailyMileage() + user.getDaliyMileage()).toString();
            handler.sendMessage(msg);
        } catch (IOException e) {
            e.printStackTrace();
            handler.sendEmptyMessage(FreeRun.UPLOAD_FAIL);
        }
    }
}

class UploadThread extends Thread {
    private User user;
    private double tot, effective;
    private Activity activity;
    private Handler handler;
    private String map;
    private String type;

    public UploadThread(User user, double tot, double effective, Activity activity, Handler handler, String map, String type) {
        this.user = user;
        this.tot = tot;
        this.effective = effective;
        this.activity = activity;
        this.handler = handler;
        this.map = map;
        this.type = type;
    }

    public void run() {
        Random random = new Random(System.currentTimeMillis());
        Date endTime = new Date();
        Date startTime = new Date(endTime.getTime() - (10 + random.nextInt(10)) * 60 * 1000 - random.nextInt(60) * 1000);
        try {
            user.uploadRunningDetail(startTime, endTime, tot, effective, map, type);
        } catch (IOException e) {
            e.printStackTrace();
            handler.sendEmptyMessage(FreeRun.UPLOAD_FAIL);
            return;
        }
        handler.sendEmptyMessage(FreeRun.UPLOAD_SUCCESS);
    }
}

public class FreeRun extends AppCompatActivity implements View.OnClickListener {
    private User user;
    public static final int WHAT_UPDATE_RESTMILEAGE = 0;
    public static final int UPLOAD_FAIL = 1;
    public static final int UPLOAD_SUCCESS = 2;
    private int mapIndex = -1;
    private final String[] maps = PathGenerator.RunMaps.keySet().toArray(new String[0]);
    private int typeIndex = -1;
    private final String[] runType = new String[]{"自由跑", "范围跑"};
    Handler handler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_free_run);
        //设置选择地图按钮
        Button selectMapBtn = (Button) findViewById(R.id.select_map);
        selectMapBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectMap();
            }
        });
        //设置选择方式按钮
        Button selectTypeBtn = (Button) findViewById(R.id.select_type);
        selectTypeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectType();
            }
        });

        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        user = new User(bundle.getString("username"), bundle.getString("password"));
        TextView restMileage = (TextView) findViewById(R.id.textView_restMielage);
        handler = new Handler() {
            public void handleMessage(Message msg) {
                // 处理消息
                super.handleMessage(msg);
                switch (msg.what) {
                    case WHAT_UPDATE_RESTMILEAGE:
                        restMileage.setText((String) msg.obj);
                        break;
                    case UPLOAD_FAIL:
                        Toast.makeText(FreeRun.this, "上传失败", Toast.LENGTH_LONG).show();
                        break;
                    case UPLOAD_SUCCESS:
                        Toast.makeText(FreeRun.this, "上传成功", Toast.LENGTH_LONG).show();
                        break;
                }
            }
        };
        InitUserThread thread = new InitUserThread(user, this, handler);
        thread.start();
        Button upload = (Button) findViewById(R.id.button_upload);
        upload.setOnClickListener(this);
        findViewById(R.id.button_force).setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        if(mapIndex < 0){
            Toast.makeText(FreeRun.this, "请先选择跑步区域!", Toast.LENGTH_LONG).show();
        }else if(typeIndex < 0){
            Toast.makeText(FreeRun.this, "请先选择跑步方式!", Toast.LENGTH_LONG).show();
        }
        else{
            if (view.getId() == R.id.button_upload) {
                upload();
            } else if (view.getId() == R.id.button_force) {
                forceUpload();
            }
        }
    }

    private void forceUpload() {
        EditText text = (EditText) findViewById(R.id.editText_mileage);
        TextView view = (TextView) findViewById(R.id.textView_restMielage);
        UploadThread thread = new UploadThread(user, Double.parseDouble(text.getText().toString()), Double.parseDouble(text.getText().toString()), this, handler, maps[mapIndex],runType[typeIndex]);
        thread.start();

    }

    private void upload() {
        EditText text = (EditText) findViewById(R.id.editText_mileage);
        TextView view = (TextView) findViewById(R.id.textView_restMielage);
        double value = Double.parseDouble(text.getText().toString());
        double mx = 3.5;
        if (!"初始化中......".equals(view.getText().toString()))
            mx = Double.parseDouble(view.getText().toString());
        if (value < 0 || value > mx) {
            Toast.makeText(FreeRun.this, "上传失败，请检查数据是否安全", Toast.LENGTH_LONG).show();
        } else {
            UploadThread thread = new UploadThread(user, Double.parseDouble(text.getText().toString()), Double.parseDouble(text.getText().toString()), this, handler, maps[mapIndex], runType[typeIndex]);
            thread.start();
        }
    }
    private void selectType() {
        android.app.AlertDialog alertDialog = new AlertDialog.Builder(this)
                .setTitle("请选择跑步方式")//默认为0表示选中第一个项目
                .setSingleChoiceItems(runType, -1, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        typeIndex = which;
                        Log.d("looog", "onClick: " + typeIndex);
                    }
                })
                .setPositiveButton("确认", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if(typeIndex >= 0){
                            TextView typeTextView = (TextView) findViewById(R.id.type_textView);
                            typeTextView.setText("已选择： " + runType[typeIndex]);
                        }
                    }
                })
                .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        typeIndex = -1;
                    }
                })
                .create();
        alertDialog.show();
    }
    private void selectMap() {
        android.app.AlertDialog alertDialog = new AlertDialog.Builder(this)
                .setTitle("请选择跑步区域")//默认为0表示选中第一个项目
                .setSingleChoiceItems(maps, -1, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mapIndex = which;
                        Log.d("looog", "onClick: " + mapIndex);
                    }
                })
                .setPositiveButton("确认", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if(mapIndex >= 0){
                            TextView mapTextView = (TextView) findViewById(R.id.map_textView);
                            mapTextView.setText("已选择： " + maps[mapIndex]);
                        }
                    }
                })
                .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        mapIndex = -1;
                    }
                })
                .create();
        alertDialog.show();
    }
}