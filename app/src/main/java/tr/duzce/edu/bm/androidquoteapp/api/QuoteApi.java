package tr.duzce.edu.bm.androidquoteapp.api;

import java.util.List;
import retrofit2.Call;
import retrofit2.http.GET;
import tr.duzce.edu.bm.androidquoteapp.models.Quote;

public interface QuoteApi {
    @GET("api/random")
    Call<List<Quote>> getRandomQuote();
}
