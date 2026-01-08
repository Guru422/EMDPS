package perds.algorithm;

import java.util.HashMap;
import java.util.Map;

public class Predictor {

    private final Map<String, Integer> incidentCounts;

    public Predictor() {
        this.incidentCounts = new HashMap<>();
    }

    public void record(String location) {
        if (location == null) return;
        incidentCounts.put(location, incidentCounts.getOrDefault(location, 0) + 1);
    }

    public String predict() {
        String hotspot = null;
        int maxCount = 0;

        for (Map.Entry<String, Integer> entry : incidentCounts.entrySet()) {
            if (entry.getValue() > maxCount) {
                maxCount = entry.getValue();
                hotspot = entry.getKey();
            }
        }

        return hotspot;
    }
}

