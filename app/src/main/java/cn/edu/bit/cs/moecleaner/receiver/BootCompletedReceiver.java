package cn.edu.bit.cs.moecleaner.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * Created by entalent on 2016/5/25.
 */
public class BootCompletedReceiver extends BroadcastReceiver{
    @Override
    public void onReceive(Context context, Intent intent) {
        AlarmSetter.setAlarm(context);
    }
}
