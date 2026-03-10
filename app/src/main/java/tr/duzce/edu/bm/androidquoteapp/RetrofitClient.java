package tr.duzce.edu.bm.androidquoteapp;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitClient {
    private static final String BASE_URL = "https://zenquotes.io/";
    private static Retrofit retrofit = null;

    public static QuoteApi getQuoteApi() {
        if (retrofit == null) {
            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit.create(QuoteApi.class);
    }
}
