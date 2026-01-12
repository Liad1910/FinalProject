package com.example.finalproject;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Locale;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class HfClient {

    // ✅ מודל נתמך ב-HF Inference
    private static final String MODEL_URL =
            "https://router.huggingface.co/hf-inference/models/ProsusAI/finbert";

    private static final OkHttpClient client = new OkHttpClient();
    private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");

    private static final String TAG = "HF_AI";

    public interface HfCallback {
        void onSuccess(String label, double score);
        void onError(String error);
    }

    public static void classifyText(String text, HfCallback cb) {
        String key = BuildConfig.HF_API_KEY;

        Log.e(TAG, "HF_API_KEY startsWith hf_: " + (key != null && key.startsWith("hf_")));

        if (key == null || key.trim().isEmpty()) {
            cb.onError("HF_API_KEY ריק. בדקי local.properties + Sync/Rebuild");
            return;
        }

        // body: {"inputs":"..."}
        JSONObject bodyObj = new JSONObject();
        try {
            bodyObj.put("inputs", text);
        } catch (JSONException e) {
            cb.onError("JSON build failed: " + e.getMessage());
            return;
        }

        RequestBody body = RequestBody.create(bodyObj.toString(), JSON);

        Request req = new Request.Builder()
                .url(MODEL_URL)
                .addHeader("Authorization", "Bearer " + key)
                .addHeader("Content-Type", "application/json")
                .post(body)
                .build();

        client.newCall(req).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                cb.onError("Network fail: " + e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response res) throws IOException {
                String raw = res.body() != null ? res.body().string() : "";
                int code = res.code();

                if (!res.isSuccessful()) {
                    Log.e(TAG, "HTTP=" + code + " preview=" + preview(raw));
                    cb.onError("HTTP " + code + ": " + preview(raw));
                    return;
                }

                // HF מחזיר לרוב מערך:
                // [ { "label": "positive", "score": 0.99 }, ... ]
                try {
                    String trimmed = raw.trim();

                    // לפעמים זה מגיע כמערך של מערכים, אז נטפל בשני המקרים
                    JSONArray arr = new JSONArray(trimmed);

                    // אם זה [ [ {...}, {...} ] ]
                    if (arr.length() > 0 && arr.get(0) instanceof JSONArray) {
                        arr = arr.getJSONArray(0);
                    }

                    if (arr.length() == 0) {
                        cb.onError("Empty response: " + preview(raw));
                        return;
                    }

                    JSONObject best = arr.getJSONObject(0);
                    String label = best.optString("label", "UNKNOWN");
                    double score = best.optDouble("score", 0.0);

                    // normalize label (FinBERT מחזיר לפעמים uppercase)
                    label = label.toUpperCase(Locale.ROOT);

                    cb.onSuccess(label, score);

                } catch (JSONException e) {
                    cb.onError("JSON parse failed: " + e.getMessage() + " raw=" + preview(raw));
                }
            }
        });
    }

    private static String preview(String s) {
        if (s == null) return "";
        s = s.replace("\n", " ").replace("\r", " ").trim();
        return s.length() > 160 ? s.substring(0, 160) + "..." : s;
    }
}
