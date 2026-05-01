package tr.duzce.edu.bm.androidquoteapp.notifications;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import tr.duzce.edu.bm.androidquoteapp.utils.AlarmUtils;

public class BootReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
            SharedPreferences prefs = context.getSharedPreferences("settings", Context.MODE_PRIVATE);
            int mode = prefs.getInt("notification_mode", AlarmUtils.NOTIF_OFF);
            AlarmUtils.updateAlarms(context, mode);
        }
    }
}
