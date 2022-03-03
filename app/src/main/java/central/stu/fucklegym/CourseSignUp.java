package central.stu.fucklegym;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;


import com.alibaba.fastjson.JSONObject;

import java.io.IOException;

import fucklegym.top.entropy.NetworkSupport;
import fucklegym.top.entropy.User;

class getCourseList extends Thread{
    private User user;
    private Handler handler;
    public getCourseList(User user, Handler handler){
        this.user = user;
        this.handler = handler;
    }

    @Override
    public void run() {
        try {
            user.login();

            JSONObject courses = user.getCourseList();
            Message msg = handler.obtainMessage();
            msg.what = CourseSignUp.GETCOURSES;
            msg.obj = courses;
            handler.sendMessage(msg);
        }catch (IOException e){
            e.printStackTrace();
            Message msg = handler.obtainMessage();
            msg.what = CourseSignUp.LOADCOURSESFAIL;
            msg.obj = null;
            handler.sendMessage(msg);
        }
    }
}
class signCourse extends Thread{
    private User user;
    private Handler handler;
    private String courseId;
    private int weekIndex;
    public signCourse(User user, Handler handler, String courseId, int weekIndex){
        this.user = user;
        this.handler = handler;
        this.courseId = courseId;
        this.weekIndex = weekIndex;
    }

    @Override
    public void run() {
        try {
            if ((user.signCourse(courseId, weekIndex)) == NetworkSupport.UploadStatus.SUCCESS){
                Message msg = handler.obtainMessage();
                msg.what = SignUp.UPLOADSUCCESS;
                msg.obj = courseId;
                handler.sendEmptyMessage(CourseSignUp.UPLOADSUCCESS);
            }else {
                Message msg = handler.obtainMessage();
                msg.what = SignUp.UPLOADSUCCESS;
                msg.obj = courseId;
                handler.sendEmptyMessage(CourseSignUp.UPLOADFAIL);
            }
        }catch (Exception e){
            e.printStackTrace();
            Message msg = handler.obtainMessage();
            msg.what = CourseSignUp.UPLOADFAIL;
            msg.obj = courseId;
            handler.sendMessage(msg);
        }
    }
}

public class CourseSignUp extends AppCompatActivity {
    public static final int GETCOURSES = 0;
    public static final int UPLOADSUCCESS = 1;
    public static final int UPLOADFAIL = 2;
    public static final int LOADCOURSESFAIL = 3;
    private User user;
    private JSONObject course;
    private String courseId;
    private int weekIndex;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_course_sign_up);
        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        user = new User(bundle.getString("username"),bundle.getString("password"));

        TextView courseName = (TextView) findViewById(R.id.courseName);
        TextView canSign = (TextView) findViewById(R.id.can_sign);
        Handler handler = new Handler(){
            @Override
            public void handleMessage(@NonNull Message msg) {

                switch (msg.what){
                    case GETCOURSES:
                        JSONObject courses = (JSONObject) msg.obj;
                        courseName.setText(courses.getString("projectName"));
                        long startTime = courses.getLongValue("timeStart");
                        long endTime = courses.getLongValue("timeEnd");
                        long time = System.currentTimeMillis();
                        if(time > startTime && time <= endTime){
                            canSign.setText("处于上课时间段内，请关注老师是否开启签到。");
                            courseId = course.getString("courseActivityId");
                            weekIndex = course.getInteger("weekIndex");
                        }else {
                            canSign.setText("未在签到时间段内，无法签到。");
                        }
                        course = courses;
                        break;
                    case UPLOADSUCCESS:
                        Toast.makeText(CourseSignUp.this, "签到成功！", Toast.LENGTH_SHORT).show();
                        break;
                    case UPLOADFAIL:
                        Toast.makeText(CourseSignUp.this, "签到失败！", Toast.LENGTH_SHORT).show();
                        break;
                    case LOADCOURSESFAIL:
                        courseName.setText("今天没有体育课程哦~");
                        courseName.setText("未在签到时间段内，无法签到。");
                        break;
                }
            }
        };

        new getCourseList(user, handler).start();
        findViewById(R.id.sign_course).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new signCourse(user, handler, courseId, weekIndex).start();
            }
        });
    }
}