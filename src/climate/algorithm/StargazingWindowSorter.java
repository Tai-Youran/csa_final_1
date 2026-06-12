package climate.algorithm;

import climate.model.CityClimateNode;

import java.util.ArrayList;

public final class StargazingWindowSorter {

    private StargazingWindowSorter() {
    }

    public static void selectionSortByStargazingScore(ArrayList<CityClimateNode> nodes) {
        if (nodes == null || nodes.size() < 2) {
            return;
        }

        for (int i = 0; i < nodes.size() - 1; i++) {
            int bestIndex = i;
            for (int j = i + 1; j < nodes.size(); j++) {
                if (score(nodes.get(j)) > score(nodes.get(bestIndex))) {
                    bestIndex = j;
                }
            }
            if (bestIndex != i) {
                CityClimateNode temp = nodes.get(i);
                nodes.set(i, nodes.get(bestIndex));
                nodes.set(bestIndex, temp);
            }
        }
    }

    public static double score(CityClimateNode node) {
        double weatherPenalty = node.calculateRiskScore() * 0.7;
        double humidityPenalty = node.getHumidity() > 70 ? (node.getHumidity() - 70) * 0.45 : 0.0;
        double windPenalty = node.getWindSpeedKmh() > 20 ? (node.getWindSpeedKmh() - 20) * 0.65 : 0.0;
        double rainPenalty = node.getPrecipitationMm() * 24.0;
        double elevationBonus = Math.min(12.0, node.getElevation() / 250.0);
        double typeBonus = "Highland".equals(node.getNodeType()) ? 8.0 : "Coastal".equals(node.getNodeType()) ? -4.0 : 0.0;
        return Math.max(0.0, Math.min(100.0, 100.0 - weatherPenalty - humidityPenalty - windPenalty - rainPenalty + elevationBonus + typeBonus));
    }
}
