package algorithm;

import model.TechNewsArticle;

import java.util.List;

/**
 * Builds a 2D sector heat matrix from scraped articles.
 */
public final class CategoryMatrixBuilder {

    public static final String[] CATEGORIES = {
            "AI", "Layoffs", "Funding", "Show HN", "General"
    };

    private CategoryMatrixBuilder() {
    }

    public static CategoryMatrixResult buildMatrix(List<TechNewsArticle> articles) {
        int rows = CATEGORIES.length;
        int cols = 4;
        double[][] matrix = new double[rows][cols];

        double[][] sums = new double[rows][3];
        int[] counts = new int[rows];
        double[] maxImpact = new double[rows];

        if (articles != null) {
            for (TechNewsArticle article : articles) {
                int row = indexOfCategory(article.getCategory());
                if (row < 0) {
                    continue;
                }

                counts[row]++;
                sums[row][0] += article.getPoints();
                sums[row][1] += article.getHypeKeywordCount();
                double impact = article.calculateImpactScore();
                if (impact > maxImpact[row]) {
                    maxImpact[row] = impact;
                }
            }
        }

        for (int r = 0; r < rows; r++) {
            matrix[r][0] = counts[r];
            matrix[r][1] = counts[r] == 0 ? 0.0 : sums[r][0] / counts[r];
            matrix[r][2] = counts[r] == 0 ? 0.0 : sums[r][1] / counts[r];
            matrix[r][3] = maxImpact[r];
        }

        return new CategoryMatrixResult(matrix, CATEGORIES);
    }

    private static int indexOfCategory(String category) {
        for (int i = 0; i < CATEGORIES.length; i++) {
            if (CATEGORIES[i].equalsIgnoreCase(category)) {
                return i;
            }
        }
        return -1;
    }

    public static class CategoryMatrixResult {
        private final double[][] matrix;
        private final String[] labels;

        public CategoryMatrixResult(double[][] matrix, String[] labels) {
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
