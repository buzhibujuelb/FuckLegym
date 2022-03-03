package central.stu.fucklegym;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import com.alibaba.fastjson.*;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.Log;
//import android.view.ContentInfo;
import android.view.View;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;

import fucklegym.top.entropy.*;
class Jump extends Thread{
    private Activity cont;
    public Jump(Activity con){
        this.cont = con;
    }

    @Override
    public void run() {
        EditText username = (EditText)cont.findViewById(R.id.editText_username);
        EditText password = (EditText)cont.findViewById(R.id.editText_password);
        String user = username.getText().toString();
        String pass = password.getText().toString();
        Intent intent = new Intent(cont,FreeRun.class);
        intent.putExtra("username", user);
        intent.putExtra("password", pass);
        cont.startActivity(intent);
//        cont.finish();
    }
}
class SignJump extends Thread{
    private Activity cont;
    public SignJump(Activity con){
        this.cont = con;
    }
    @Override
    public void run() {
        EditText username = (EditText)cont.findViewById(R.id.editText_username);
        EditText password = (EditText)cont.findViewById(R.id.editText_password);
        String user = username.getText().toString();
        String pass = password.getText().toString();
        Intent intent = new Intent(cont,SignUp.class);
        intent.putExtra("username", user);
        intent.putExtra("password", pass);
        cont.startActivity(intent);
//        cont.finish();
    }
    void save(String username, String password){

    }
}
class CourseSign extends Thread{
    private Activity cont;
    public CourseSign(Activity con){
        this.cont = con;
    }

    @Override
    public void run() {
        EditText username = (EditText)cont.findViewById(R.id.editText_username);
        EditText password = (EditText)cont.findViewById(R.id.editText_password);
        String user = username.getText().toString();
        String pass = password.getText().toString();
        Intent intent = new Intent(cont,CourseSignUp.class);
        intent.putExtra("username", user);
        intent.putExtra("password", pass);
        cont.startActivity(intent);
//        cont.finish();
    }
}
public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Button but = (Button)findViewById(R.id.button_freeRun);
        but.setOnClickListener(this);
        ((Button)findViewById(R.id.button_signup)).setOnClickListener(this);
        ((Button)findViewById(R.id.button_course_sign)).setOnClickListener(this);

        EditText username = (EditText)findViewById(R.id.editText_username);
        EditText password = (EditText)findViewById(R.id.editText_password);
        ((Button)findViewById(R.id.save)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String user = username.getText().toString();
                String pass = password.getText().toString();
                MainActivity.this.save(user, pass);
            }
        });

        ((Button)findViewById(R.id.uma)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                jumpWeb("https://www.bilibili.com/bangumi/play/ep199681");
            }
        });
        ((Button)findViewById(R.id.distribute)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                jumpWeb("https://github.com/Foreverddb/FuckLegym");
            }
        });

//        showUpdateMsg();
        FileInputStream in = null;
        BufferedReader reader = null;
        //判断是否更新
        try {
            in = openFileInput("update.txt");
            Log.d("ser10", "onCreate: " + in.toString());
            reader = new BufferedReader(new InputStreamReader(in));
            String version = reader.readLine();
            if(!getVersionName().equals(version)){
                showUpdateMsg();
            }
        }catch (IOException e){
            e.printStackTrace();
            showUpdateMsg();
        }
        //获取保存的账号密码
        try {
            in = openFileInput("data.txt");
            reader = new BufferedReader(new InputStreamReader(in));
            String line = "";
            line = reader.readLine();
            username.setText(line);
            Log.d("logog11", "user: " + line);
            line = reader.readLine();
            password.setText(line);
            Log.d("logog11", "user: " + line);
        }catch (IOException e){
            e.printStackTrace();
        }
    }

    @Override
    public void onClick(View view) {
        if(view.getId()==R.id.button_freeRun){
            jumpFreeRun();
        }else if (view.getId()==R.id.button_signup){
            jumpSignUp();
        }else if (view.getId()==R.id.button_course_sign){
            jumpCourseSignUp();
        }
    }
    private void jumpFreeRun(){
        Jump jmp = new Jump(this);
        jmp.start();
        Button but = (Button)findViewById(R.id.button_freeRun);
//        but.setText("Waiting for jumping pages");
//        but.setEnabled(false);
    }
    private void jumpSignUp(){
        SignJump jmp = new SignJump(this);
        jmp.start();
    }
    private void jumpCourseSignUp(){
        CourseSign jmp = new CourseSign(this);
        jmp.start();
    }
    private void jumpWeb(String url){
        Intent intent = new Intent(MainActivity.this, WebViewStarter.class);
        intent.putExtra("url", url);
        startActivity(intent);
    }
    //保存账号密码
    void save(String username, String password){
        FileOutputStream out = null;
        BufferedWriter writer = null;
        try {
            out = openFileOutput("data.txt", Context.MODE_PRIVATE);
            writer = new BufferedWriter(new OutputStreamWriter(out));
            writer.write(username + "\n" + password);
        }catch (IOException e){
            e.printStackTrace();
        }finally {
            try {
                if(writer != null) {
                    writer.close();
                    Toast.makeText(MainActivity.this, "账号密码保存成功！", Toast.LENGTH_SHORT).show();
                }
            }catch (IOException e){
                e.printStackTrace();
            }
        }
    }
    //显示更新信息
    void showUpdateMsg(){
        androidx.appcompat.app.AlertDialog.Builder alertDialogBuilder = new androidx.appcompat.app.AlertDialog.Builder(this);
        alertDialogBuilder.setMessage("更新日志：\n" +
                "1.新增一键课程签到功能\n" +
                "2.新增了一张高质量好看的赛马娘图片\n" +
                "3.请大家务必去看《赛马娘》！");
        alertDialogBuilder.setPositiveButton("我知道了", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                FileOutputStream out = null;
                BufferedWriter writer = null;
                try {
                    out = openFileOutput("update.txt", Context.MODE_PRIVATE);
                    writer = new BufferedWriter(new OutputStreamWriter(out));
                    writer.write(getVersionName());
                    Log.d("looog", "write:" + getVersionName());

                    jumpWeb("https://www.bilibili.com/bangumi/play/ep199681");
//                    Intent intent = new Intent(Intent.ACTION_VIEW);
//                    intent.setData(Uri.parse("https://www.bilibili.com/bangumi/play/ep199681"));
//                    startActivity(intent);
                }catch (IOException e){
                    e.printStackTrace();
                }finally {
                    try {
                        if(writer != null) {
                            writer.close();
                        }
                    }catch (IOException e){
                        e.printStackTrace();
                    }
                }
            }
        });
        final AlertDialog alertdialog1 = alertDialogBuilder.create();
        alertdialog1.show();
    }
    //获取当前版本号
    private String getVersionName() {
        try {
            // 获取packagemanager的实例
            PackageManager packageManager = getPackageManager();
            // getPackageName()是你当前类的包名，0代表是获取版本信息
            PackageInfo packInfo = packageManager.getPackageInfo(getPackageName(),0);
            String version = packInfo.versionName;
            return version;
        }catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }
}