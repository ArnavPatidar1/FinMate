package arnav.example.finmate.network;

import java.util.List;
import java.util.Map;

public class GeminiRequest {
    private String model;
    private List<Map<String, String>> contents;

    public GeminiRequest(String model, List<Map<String, String>> contents) {
        this.model = model;
        this.contents = contents;
    }
}


