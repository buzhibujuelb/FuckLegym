package central.stu.fucklegym;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

public class SignService extends Service {
    public SignService() {
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                Long currentTime = System.currentTimeMillis();
                NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
                NotificationChannel channel = new NotificationChannel("testCH1", "haf", NotificationManager.IMPORTANCE_DEFAULT);
                manager.createNotificationChannel(channel);
                Notification.Builder builder = new Notification.Builder(SignService.this, "testCH1");
                builder.setSmallIcon(R.drawable.ic_launcher);
                builder.setLargeIcon(BitmapFactory.decodeResource(getResources(), R.drawable.ic_launcher));
                builder.setContentTitle("第二次签到时间到啦！");

                builder.setContentText("进入签到进行二次签到。");
                Notification notification = builder.build();
                manager.notify(1, notification);
                Log.d("ser10", "time" + currentTime);
            }
        });
        thread.start();
//        Timer timer = new Timer();
//        timer.schedule(new TimerTask() {
//            @Override
//            public void run() {
//                Log.d("ser10", "run: ");
//            }
//        }, 10000);
        stopSelf();
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }
}