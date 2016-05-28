package cn.edu.bit.cs.moecleaner.receiver;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.util.Calendar;

import cn.edu.bit.cs.moecleaner.R;

public class AlarmReceiver extends BroadcastReceiver {
    public AlarmReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        /*
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.MONTH, 6 - 1);
        calendar.set(Calendar.DAY_OF_MONTH, 3);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);

        long interval = System.currentTimeMillis() - calendar.getTimeInMillis();
        interval = Math.abs(interval) / 1000;
        if(Math.abs(interval) <= 9 * 60 * 60) {
            System.out.println("receive: " + intent.getAction());
            NotificationManager notificationManager =
                    (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.peppermint_candy);
            Notification notification = new Notification.Builder(context)
                    .setSmallIcon(R.drawable.night_mode)
                    .setLargeIcon(bitmap)
                    .setTicker("")
                    .setContentTitle("星佑酱生日快乐!")
                    .setContentText("接下来的一年也要开开心心呀")
                    .setNumber(1)
                    .build();
            notification.flags |= (Notification.FLAG_NO_CLEAR);
            notificationManager.notify(1, notification);
        }
        */
    }
}
