package climate.algorithm;

import climate.model.CityClimateNode;

import java.util.ArrayList;
import java.util.List;

public final class ClimateRiskSorter {

    private ClimateRiskSorter() {
    }

    public static void selectionSortByRisk(ArrayList<CityClimateNode> nodes) {
        selectionSort(nodes, "risk");
    }

    public static void selectionSortByWind(ArrayList<CityClimateNode> nodes) {
        selectionSort(nodes, "wind");
    }

    public static void selectionSortByPrecipitation(ArrayList<CityClimateNode> nodes) {
        selectionSort(nodes, "precipitation");
    }

    public static ArrayList<CityClimateNode> filterByRiskBand(List<CityClimateNode> nodes, String riskBand) {
        ArrayList<CityClimateNode> filtered = new ArrayList<>();
        if (nodes == null || riskBand == null || riskBand.equalsIgnoreCase("All")) {
            if (nodes != null) {
                filtered.addAll(nodes);
            }
            return filtered;
        }
        for (CityClimateNode node : nodes) {
            if (node.getRiskBand().equalsIgnoreCase(riskBand)) {
                filtered.add(node);
            }
        }
        return filtered;
    }

    private static void selectionSort(ArrayList<CityClimateNode> nodes, String metric) {
        if (nodes == null || nodes.size() < 2) {
            return;
        }

        for (int i = 0; i < nodes.size() - 1; i++) {
            int maxIndex = i;
            for (int j = i + 1; j < nodes.size(); j++) {
                if (metricValue(nodes.get(j), metric) > metricValue(nodes.get(maxIndex), metric)) {
                    maxIndex = j;
                }
            }
            if (maxIndex != i) {
                CityClimateNode temp = nodes.get(i);
                nodes.set(i, nodes.get(maxIndex));
                nodes.set(maxIndex, temp);
            }
        }
    }

    private static double metricValue(CityClimateNode node, String metric) {
        return switch (metric) {
            case "wind" -> node.getWindSpeedKmh();
            case "precipitation" -> node.getPrecipitationMm();
            default -> node.calculateRiskScore();
        };
    }
}
