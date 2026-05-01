package tr.duzce.edu.bm.androidquoteapp.notifications;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.media.AudioAttributes;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.util.Log;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import tr.duzce.edu.bm.androidquoteapp.R;
import tr.duzce.edu.bm.androidquoteapp.activities.MainActivity;
import tr.duzce.edu.bm.androidquoteapp.api.RetrofitClient;
import tr.duzce.edu.bm.androidquoteapp.models.Quote;

public class NotificationReceiver extends BroadcastReceiver {
    private static final String TAG = "NotificationReceiver";
    // Fresh ID (v10) to force the system to apply new High Importance/Sound settings
    private static final String CHANNEL_ID = "daily_quote_popup_v10";

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "onReceive triggered");
        
        Toast.makeText(context, "Fetching Daily Quote...", Toast.LENGTH_SHORT).show();

        final PendingResult pendingResult = goAsync();
        
        RetrofitClient.getQuoteApi().getRandomQuote().enqueue(new Callback<List<Quote>>() {
            @Override
            public void onResponse(Call<List<Quote>> call, Response<List<Quote>> response) {
                try {
                    String quoteText = null;
                    String quoteAuthor = null;
                    String displayMsg = "Check out today's inspiring quote!";
                    
                    if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                        Quote quote = response.body().get(0);
                        quoteText = quote.getText();
                        quoteAuthor = (quote.getAuthor() != null ? quote.getAuthor() : "Unknown");
                        displayMsg = "\"" + quoteText + "\" - " + quoteAuthor;
                        Log.d(TAG, "Quote fetched: " + displayMsg);
                    }
                    showNotification(context, displayMsg, quoteText, quoteAuthor);
                } catch (Exception e) {
                    Log.e(TAG, "Error in onResponse", e);
                } finally {
                    pendingResult.finish();
                }
            }

            @Override
            public void onFailure(Call<List<Quote>> call, Throwable t) {
                Log.e(TAG, "API Call failed", t);
                try {
                    showNotification(context, "Check out today's inspiring quote!", null, null);
                } finally {
                    pendingResult.finish();
                }
            }
        });
    }

    private void showNotification(Context context, String displayMsg, String quoteText, String quoteAuthor) {
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Daily Inspirations",
                    NotificationManager.IMPORTANCE_HIGH // Necessary for pop-up
            );
            channel.setDescription("Shows a pop-up with your daily quote");
            channel.enableLights(true);
            channel.setLightColor(Color.CYAN);
            channel.enableVibration(true);
            channel.setVibrationPattern(new long[]{100, 200, 300, 400, 500, 400, 300, 200, 400});
            channel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
            
            // Explicitly attach sound to the channel
            AudioAttributes audioAttributes = new AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build();
            channel.setSound(defaultSoundUri, audioAttributes);

            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }

        Intent intent = new Intent(context, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        
        if (quoteText != null) {
            intent.putExtra("quote_text", quoteText);
            intent.putExtra("quote_author", quoteAuthor);
        }

        PendingIntent pendingIntent = PendingIntent.getActivity(
                context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.notificiation_icon)
                .setContentTitle("Daily Quote")
                .setContentText(displayMsg)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(displayMsg))
                .setPriority(NotificationCompat.PRIORITY_MAX) // Required for heads-up on older devices
                .setSound(defaultSoundUri)
                .setVibrate(new long[]{100, 200, 300, 400, 500, 400, 300, 200, 400})
                .setDefaults(NotificationCompat.DEFAULT_ALL)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .setCategory(NotificationCompat.CATEGORY_MESSAGE)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC);

        if (notificationManager != null) {
            notificationManager.notify((int) System.currentTimeMillis(), builder.build());
            Log.d(TAG, "Notification triggered with Sound and High Importance");
        }
    }
}
