package tr.duzce.edu.bm.androidquoteapp;

import android.content.Context;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import java.util.List;
import retrofit2.Response;

public class QuoteNotificationWorker extends Worker {

    public QuoteNotificationWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        Context context = getApplicationContext();

        if (!NotificationHelper.hasNotificationPermission(context)) {
            return Result.success();
        }

        try {

            Response<List<Quote>> response = RetrofitClient.getQuoteApi().getRandomQuote().execute();

            if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                Quote quote = response.body().get(0);


                NotificationHelper.showQuoteNotification(context, quote);


                DailyQuoteScheduler.reschedule(context);

                return Result.success();
            } else {
                return Result.retry();
            }
        } catch (Exception exception) {
            Log.e("QuoteWorker", "İstek başarısız oldu", exception);
            return Result.retry();
        }
    }
}