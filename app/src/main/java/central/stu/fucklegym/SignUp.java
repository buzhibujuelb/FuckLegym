package central.stu.fucklegym;

import androidx.appcompat.app.AppCompatActivity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import fucklegym.top.entropy.User;
class LoadActivitiresThread extends Thread{
    private User user;
    private Handler handler;
    public LoadActivitiresThread(User user, Handler handler){
        this.user = user;
        this.handler = handler;
    }
    @Override
    public void run() {
        try {
            user.login();

            HashMap<String,String> acts = (HashMap<String, String>) user.getTodayActivities();
            Message msg = handler.obtainMessage();
            msg.what = SignUp.GETACTIVITIES;
            msg.obj = acts;
            handler.sendMessage(msg);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
class SignThread extends Thread{
    private User user;
    private Handler handler;
    private String name;
    public SignThread(User user,Handler handler,String nme){
        this.user = user;
        this.handler = handler;
        this.name = nme;
    }
    @Override
    public void run() {
        try {
            if(user.getTodayActivities().containsKey(name)){
                user.sign(name);
                Message msg = handler.obtainMessage();
                msg.what = SignUp.UPLOADSUCCESS;
                msg.obj = name;
                handler.sendEmptyMessage(SignUp.UPLOADSUCCESS);
            }else handler.sendEmptyMessage(SignUp.ACTIVITYDOESNOTEXIST);

        } catch (IOException e) {
            e.printStackTrace();
            Message msg = handler.obtainMessage();
            msg.what = SignUp.UPLOADFAIL;
            msg.obj = name;
            handler.sendMessage(msg);
        }
    }
}

public class SignUp extends AppCompatActivity {
    public static final int GETACTIVITIES = 0;
    public static final int UPLOADSUCCESS = 1;
    public static final int UPLOADFAIL = 2;
    public static final int ACTIVITYDOESNOTEXIST = 3;
    private TextView textView;
    private User user;
    private EditText editText;
    private HashMap<String,String> activities;
    private Handler handler;
    public HashMap<String ,String> theacts;
    public ArrayList<String> checkedActs = new ArrayList<>();
    private boolean signNotify = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);
        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        user = new User(bundle.getString("username"),bundle.getString("password"));
        Switch notifySwitch = (Switch) findViewById(R.id.switch_sign_notify);
        notifySwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                signNotify = b;
            }
        });
//        this.textView = (TextView)findViewById(R.id.textView_allActivities);
//        this.editText = (EditText)findViewById(R.id.editText_activityName);
        handler = new Handler(){
            public void handleMessage(Message msg) {
                // 处理消息
                super.handleMessage(msg);
                switch (msg.what) {
                    case GETACTIVITIES:
                        HashMap<String ,String> acts = (HashMap<String ,String >)msg.obj;
                        StringBuffer buf = new StringBuffer();
                        LinearLayout layout = (LinearLayout) findViewById(R.id.activities_layout);
                        for(String str:acts.keySet()){
                            buf.append(str+"\n");
                            CheckBox checkBox = (CheckBox) View.inflate(SignUp.this, R.layout.checkbox, null);
                            checkBox.setText(str);
                            layout.addView(checkBox);
                            checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                                @Override
                                public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                                    if(b){
                                        checkedActs.add(str);
                                    }else {
                                        checkedActs.remove(str);
                                    }
                                    Log.d("acac", "onClick: " + checkedActs);
                                }
                            });
                        }
                        theacts = acts;

//                        textView.setText(buf.toString());
                        break;
                    case UPLOADSUCCESS:
                        Toast.makeText(SignUp.this,"打卡成功",Toast.LENGTH_SHORT).show();
                        break;
                    case UPLOADFAIL:
                        Toast.makeText(SignUp.this,msg.obj + " 打卡失败",Toast.LENGTH_SHORT).show();
                        break;
                    case ACTIVITYDOESNOTEXIST:
                        Toast.makeText(SignUp.this,"活动不存在，请检查名称是否写错",Toast.LENGTH_LONG);
                        break;
                }
            }
        };
        new LoadActivitiresThread(user,handler).start();
        ((Button)findViewById(R.id.button_uploadSign)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!checkedActs.isEmpty()){
                    if(signNotify){
//                        Intent intent = new Intent(SignUp.this, SignService.class);
//                        startService(intent);
                    }
                    for(String str: checkedActs){
                        new SignThread(user,handler,str).start();
                    }
                }else {
                    Toast.makeText(SignUp.this, "请先选择一个要签到的活动", Toast.LENGTH_SHORT).show();
                }

//                new SignThread(user,handler,editText.getText().toString()).start();
            }
        });
    }
}
