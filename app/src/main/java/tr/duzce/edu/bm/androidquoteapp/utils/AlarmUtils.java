package tr.duzce.edu.bm.androidquoteapp.utils;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import java.util.Calendar;
import tr.duzce.edu.bm.androidquoteapp.notifications.NotificationReceiver;

public class AlarmUtils {

    public static final int NOTIF_OFF = 0;
    public static final int NOTIF_ONCE = 1;
    public static final int NOTIF_TWICE = 2;

    public static void updateAlarms(Context context, int mode) {
        cancelAlarms(context);
        if (mode == NOTIF_ONCE) {
            scheduleAlarm(context, 0, 21, 48); // 12:00 AM
        } else if (mode == NOTIF_TWICE) {
            scheduleAlarm(context, 1, 21, 48); // 1:00 PM
            scheduleAlarm(context, 2, 15, 0); // 3:00 PM
        }
    }

    private static void scheduleAlarm(Context context, int requestCode, int hour, int minute) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, NotificationReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                requestCode,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, minute);
        calendar.set(Calendar.SECOND, 0);

        if (calendar.getTimeInMillis() <= System.currentTimeMillis()) {
            calendar.add(Calendar.DAY_OF_MONTH, 1);
        }

        if (alarmManager != null) {
            alarmManager.setInexactRepeating(
                    AlarmManager.RTC_WAKEUP,
                    calendar.getTimeInMillis(),
                    AlarmManager.INTERVAL_DAY,
                    pendingIntent
            );
        }
    }

    public static void cancelAlarms(Context context) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, NotificationReceiver.class);
        
        // Cancel all potential alarms (0, 1, 2)
        for (int i = 0; i <= 2; i++) {
            PendingIntent pendingIntent = PendingIntent.getBroadcast(
                    context,
                    i,
                    intent,
                    PendingIntent.FLAG_NO_CREATE | PendingIntent.FLAG_IMMUTABLE
            );
            if (pendingIntent != null && alarmManager != null) {
                alarmManager.cancel(pendingIntent);
                pendingIntent.cancel();
            }
        }
    }
}
