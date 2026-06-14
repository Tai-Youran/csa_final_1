package climate.scraper;

import climate.model.CityClimateNode;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ClimateScraperResult {

    private final List<CityClimateNode> nodes;
    private final int geocodedCount;
    private final long fetchedAt;

    public ClimateScraperResult(List<CityClimateNode> nodes, int geocodedCount) {
        this.nodes = new ArrayList<>(nodes);
        this.geocodedCount = geocodedCount;
        this.fetchedAt = System.currentTimeMillis();
    }

    public List<CityClimateNode> getNodes() {
        return Collections.unmodifiableList(nodes);
    }

    public int getNodeCount() {
        return nodes.size();
    }

    public int getGeocodedCount() {
        return geocodedCount;
    }

    public long getFetchedAt() {
        return fetchedAt;
    }
}
