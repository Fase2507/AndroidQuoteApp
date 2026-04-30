package tr.duzce.edu.bm.androidquoteapp;

import android.os.Handler;
import android.os.Looper;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

// Explicitly importing BuildConfig can sometimes resolve issues in the IDE
import tr.duzce.edu.bm.androidquoteapp.BuildConfig;

public class GeminiService {
    private final String apiKey = BuildConfig.GEMINI_API_KEY;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    public interface Callback {
        void onSuccess(String result);
        void onError(Exception e);
    }

    public void categorizeQuote(String quote, Callback callback) {
        String prompt = "Categorize the following quote into one of these categories: Love, Life, Motivation, Wisdom, Humor. Only return the category name: " + quote;
        generateContent(prompt, callback);
    }

    public void translateQuote(String quote, String targetLanguage, Callback callback) {
        String prompt = "Translate the following quote to " + targetLanguage + ". Only return the translated text: " + quote;
        generateContent(prompt, callback);
    }

    private void generateContent(String prompt, Callback callback) {
        executor.execute(() -> {
            try {
                URL url = new URL("https://generativelanguage.googleapis.com/v1beta/models/gemini-flash-latest:generateContent?key=" + apiKey);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setDoOutput(true);

                // Proper Gemini API structure
                JSONObject root = new JSONObject();
                JSONArray contentsArray = new JSONArray();
                JSONObject contentItem = new JSONObject();
                JSONArray partsArray = new JSONArray();
                JSONObject partItem = new JSONObject();
                partItem.put("text", prompt);
                partsArray.put(partItem);
                contentItem.put("parts", partsArray);
                contentsArray.put(contentItem);
                root.put("contents", contentsArray);

                try (OutputStream os = conn.getOutputStream()) {
                    byte[] input = root.toString().getBytes(StandardCharsets.UTF_8);
                    os.write(input, 0, input.length);
                }

                int code = conn.getResponseCode();
                if (code == 200) {
                    BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8));
                    StringBuilder response = new StringBuilder();
                    String responseLine;
                    while ((responseLine = br.readLine()) != null) {
                        response.append(responseLine.trim());
                    }
                    
                    JSONObject responseJson = new JSONObject(response.toString());
                    String result = responseJson.getJSONArray("candidates")
                            .getJSONObject(0)
                            .getJSONObject("content")
                            .getJSONArray("parts")
                            .getJSONObject(0)
                            .getString("text");
                    
                    mainHandler.post(() -> callback.onSuccess(result.trim()));
                } else {
                    mainHandler.post(() -> callback.onError(new Exception("HTTP error code: " + code)));
                }
            } catch (Exception e) {
                mainHandler.post(() -> callback.onError(e));
            }
        });
    }
}
