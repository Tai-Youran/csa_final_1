package climate.algorithm;

import climate.model.CityClimateNode;

import java.util.List;

public final class ClimateMatrixBuilder {

    private static final String[] REGIONS = {
            "North America", "South America", "Europe", "Africa", "Asia", "Oceania", "Other"
    };

    private ClimateMatrixBuilder() {
    }

    public static ClimateMatrixResult buildRegionMatrix(List<CityClimateNode> nodes) {
        double[][] matrix = new double[REGIONS.length][5];
        double[] totalRisk = new double[REGIONS.length];
        double[] totalWind = new double[REGIONS.length];
        double[] totalPrecipitation = new double[REGIONS.length];
        int[] counts = new int[REGIONS.length];

        if (nodes != null) {
            for (CityClimateNode node : nodes) {
                int row = indexOf(node.getRegion());
                counts[row]++;
                double risk = node.calculateRiskScore();
                totalRisk[row] += risk;
                totalWind[row] += node.getWindSpeedKmh();
                totalPrecipitation[row] += node.getPrecipitationMm();
                if (risk > matrix[row][3]) {
                    matrix[row][3] = risk;
                }
            }
        }

        for (int row = 0; row < REGIONS.length; row++) {
            matrix[row][0] = counts[row];
            matrix[row][1] = counts[row] == 0 ? 0.0 : totalRisk[row] / counts[row];
            matrix[row][2] = counts[row] == 0 ? 0.0 : totalWind[row] / counts[row];
            matrix[row][4] = counts[row] == 0 ? 0.0 : totalPrecipitation[row] / counts[row];
        }

        return new ClimateMatrixResult(matrix, REGIONS);
    }

    private static int indexOf(String region) {
        for (int i = 0; i < REGIONS.length; i++) {
            if (REGIONS[i].equalsIgnoreCase(region)) {
                return i;
            }
        }
        return REGIONS.length - 1;
    }

    public static class ClimateMatrixResult {
        private final double[][] matrix;
        private final String[] labels;

        public ClimateMatrixResult(double[][] matrix, String[] labels) {
            this.matrix = matrix;
            this.labels = labels;
        }

        public double[][] getMatrix() {
            return matrix;
        }

        public String[] getLabels() {
            return labels;
        }
    }
}
