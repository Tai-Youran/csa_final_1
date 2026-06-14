package algorithm;

import model.TechNewsArticle;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Custom Selection Sort for ranking articles by impact metrics.
 */
public final class HypeScoreSorter {

    private HypeScoreSorter() {
    }

    public static void selectionSortByHypeScore(ArrayList<TechNewsArticle> list) {
        if (list == null || list.size() < 2) {
            return;
        }

        int n = list.size();
        for (int i = 0; i < n - 1; i++) {
            int maxIdx = i;
            for (int j = i + 1; j < n; j++) {
                if (list.get(j).calculateImpactScore() > list.get(maxIdx).calculateImpactScore()) {
                    maxIdx = j;
                }
            }
            if (maxIdx != i) {
                TechNewsArticle temp = list.get(i);
                list.set(i, list.get(maxIdx));
                list.set(maxIdx, temp);
            }
        }
    }

    public static void selectionSortByPoints(ArrayList<TechNewsArticle> list) {
        selectionSort(list, Comparator.comparingInt(TechNewsArticle::getPoints).reversed());
    }

    public static void selectionSortByComments(ArrayList<TechNewsArticle> list) {
        selectionSort(list, Comparator.comparingInt(TechNewsArticle::getComments).reversed());
    }

    private static void selectionSort(ArrayList<TechNewsArticle> list, Comparator<TechNewsArticle> metric) {
        if (list == null || list.size() < 2) {
            return;
        }

        int n = list.size();
        for (int i = 0; i < n - 1; i++) {
            int bestIdx = i;
            for (int j = i + 1; j < n; j++) {
                if (metric.compare(list.get(j), list.get(bestIdx)) < 0) {
                    bestIdx = j;
                }
            }
            if (bestIdx != i) {
                TechNewsArticle temp = list.get(i);
                list.set(i, list.get(bestIdx));
                list.set(bestIdx, temp);
            }
        }
    }

    public static ArrayList<TechNewsArticle> filterByCategory(List<TechNewsArticle> list, String category) {
        ArrayList<TechNewsArticle> filtered = new ArrayList<>();
        if (list == null || category == null) {
            return filtered;
        }

        String target = category.trim();
        for (TechNewsArticle article : list) {
            if (article.getCategory().equalsIgnoreCase(target)) {
                filtered.add(article);
            }
        }
        return filtered;
    }
}
