package tr.duzce.edu.bm.androidquoteapp;

import java.util.List;
import retrofit2.Call;
import retrofit2.http.GET;

public interface QuoteApi {
    @GET("api/random")
    Call<List<Quote>> getRandomQuote();
}
