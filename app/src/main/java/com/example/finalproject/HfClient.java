package com.example.finalproject;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class HfClient {

    public interface HfCallback {
        void onSuccess(String topLabel, double score);
        void onError(String error);
    }

    private static final OkHttpClient client = new OkHttpClient();
    private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");

    // החליפי לפי המודל שבחרת:
    private static final String MODEL_URL =
            "https://api-inference.huggingface.co/models/distilbert-base-uncased-finetuned-sst-2-english";

    public static void classifyText(String text, HfCallback cb) {
        try {
            JSONObject bodyJson = new JSONObject();
            bodyJson.put("inputs", text);

            RequestBody body = RequestBody.create(bodyJson.toString(), JSON);

            Request request = new Request.Builder()
                    .url(MODEL_URL)
                    .addHeader("Authorization", "Bearer " + BuildConfig.HF_API_KEY)
                    .addHeader("Content-Type", "application/json")
                    .post(body)
                    .build();

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    cb.onError(e.getMessage());
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    String raw = response.body() != null ? response.body().string() : "";

                    if (!response.isSuccessful()) {
                        cb.onError("HTTP " + response.code() + ": " + raw);
                        return;
                    }

                    try {
                        // התשובה לרוב: [[{label,score},{label,score}...]]
                        JSONArray outer = new JSONArray(raw);
                        JSONArray arr = outer.getJSONArray(0);

                        JSONObject best = arr.getJSONObject(0);
                        String label = best.getString("label");
                        double score = best.getDouble("score");

                        cb.onSuccess(label, score);
                    } catch (Exception ex) {
                        cb.onError("Parse error: " + ex.getMessage() + " raw=" + raw);
                    }
                }
            });

        } catch (Exception e) {
            cb.onError(e.getMessage());
        }
    }
}
