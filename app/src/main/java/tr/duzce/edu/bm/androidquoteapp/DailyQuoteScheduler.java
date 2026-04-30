package tr.duzce.edu.bm.androidquoteapp;

import android.content.Context;
import androidx.work.Constraints;
import androidx.work.ExistingWorkPolicy;
import androidx.work.NetworkType;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

public final class DailyQuoteScheduler {

    private static final String UNIQUE_WORK_NAME = "daily_quote_work";

    private DailyQuoteScheduler() {}

    public static void ensureScheduled(Context context) {
        enqueueWork(context, ExistingWorkPolicy.KEEP);
    }

    public static void reschedule(Context context) {
        enqueueWork(context, ExistingWorkPolicy.REPLACE);
    }


    public static void cancel(Context context) {
        WorkManager.getInstance(context.getApplicationContext()).cancelUniqueWork(UNIQUE_WORK_NAME);
    }

    private static void enqueueWork(Context context, ExistingWorkPolicy policy) {
        SettingsManager settingsManager = new SettingsManager(context);


        if (!settingsManager.isNotificationsEnabled()) {
            return;
        }


        long initialDelay = calculateInitialDelay(
                settingsManager.getNotificationHour(),
                settingsManager.getNotificationMinute()
        );

        Constraints constraints = new Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build();

        OneTimeWorkRequest workRequest = new OneTimeWorkRequest.Builder(QuoteNotificationWorker.class)
                .setInitialDelay(initialDelay, TimeUnit.MILLISECONDS)
                .setConstraints(constraints)
                .build();

        WorkManager.getInstance(context.getApplicationContext())
                .enqueueUniqueWork(UNIQUE_WORK_NAME, policy, workRequest);
    }

    private static long calculateInitialDelay(int hour, int minute) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime nextRun = now.withHour(hour).withMinute(minute).withSecond(0).withNano(0);

        if (!nextRun.isAfter(now)) {
            nextRun = nextRun.plusDays(1);
        }
        return Duration.between(now, nextRun).toMillis();
    }
}