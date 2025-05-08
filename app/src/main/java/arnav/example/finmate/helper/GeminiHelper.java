package arnav.example.finmate.helper;

import android.util.Log;
import com.google.gson.Gson;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.IOException;
import java.util.*;

import arnav.example.finmate.model.FinancialData;
import okhttp3.*;

public class GeminiHelper {

    private static final String BASE_URL = "https://generativelanguage.googleapis.com/v1/models/gemini-1.5-pro:generateContent";

    private final OkHttpClient client = new OkHttpClient();
    private final Gson gson = new Gson();
    private final String apiKey;

    public interface GeminiCallback {
        void onResponse(String reply);
        void onError(String error);
    }

    public GeminiHelper(String apiKey) {
        this.apiKey = apiKey;
    }


    public String generateContextPrompt(FinancialData financialData, Map<String, Double> categorizedExpenses) {
        StringBuilder context = new StringBuilder();

        context.append("Here is the user's current financial summary:\n");
        context.append("Total Income: ₹").append(financialData.getTotalIncome()).append("\n");
        context.append("Total Expenses: ₹").append(financialData.getTotalExpense()).append("\n\n");

        context.append("Spending by Category:\n");
        for (Map.Entry<String, Double> entry : categorizedExpenses.entrySet()) {
            context.append("- ").append(entry.getKey()).append(": ₹").append(entry.getValue()).append("\n");
        }

        context.append("\nBased on this data, suggest personalized financial advice or improvements. ");

        return context.toString();
    }

    public void sendPersonalizedMessage(String userMessage, FinancialData financialData,
                                        Map<String, Double> categorizedExpenses, GeminiCallback callback) {

        String contextPrompt = generateContextPrompt(financialData, categorizedExpenses);

        String fullPrompt = contextPrompt + "\n\nUser asked: \"" + userMessage + "\"";

        sendMessage(fullPrompt, callback);
    }



    public void sendMessage(String userPrompt, GeminiCallback callback) {
        Map<String, Object> part = new HashMap<>();
        part.put("text", userPrompt);

        Map<String, Object> content = new HashMap<>();
        content.put("parts", Collections.singletonList(part));

        Map<String, Object> bodyMap = new HashMap<>();
        bodyMap.put("contents", Collections.singletonList(content));

        String jsonBody = gson.toJson(bodyMap);

        RequestBody body = RequestBody.create(
                jsonBody,
                MediaType.parse("application/json")
        );

        Request request = new Request.Builder()
                .url(BASE_URL + "?key=" + apiKey)
                .post(body)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                callback.onError("Request failed: " + e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (!response.isSuccessful()) {
                    callback.onError("API Error: " + response.message());
                    return;
                }

                String responseBody = response.body().string();
                try {
                    JSONObject jsonObject = new JSONObject(responseBody);
                    JSONArray candidates = jsonObject.getJSONArray("candidates");

                    // Check if there are no candidates
                    if (candidates.length() == 0) {
                        callback.onResponse("Sorry, I couldn't understand that.");
                        return;
                    }

                    // Get the first candidate and extract the message
                    JSONObject firstCandidate = candidates.getJSONObject(0);
                    JSONObject content = firstCandidate.getJSONObject("content");
                    JSONArray parts = content.getJSONArray("parts");

                    // Check if parts is empty or doesn't contain the expected message
                    if (parts.length() == 0) {
                        callback.onResponse("Sorry, I couldn't retrieve a valid response.");
                        return;
                    }

                    // Get the actual reply from the first part
                    String reply = parts.getJSONObject(0).getString("text");

                    // Send the reply back to the callback
                    callback.onResponse(reply);
                } catch (JSONException e) {
                    // Log error for debugging purposes
                    Log.e("GeminiHelper", "Parsing error: " + e.getMessage());
                    callback.onError("Parsing error: " + e.getMessage());
                }
            }

        });
    }
}

