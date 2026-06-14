package climate.scraper;

import climate.model.CityClimateNode;
import climate.model.CoastalClimateNode;
import climate.model.HighAltitudeClimateNode;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ClimateDataScraper {

    private static final int TARGET_NODES = 72;
    private static final int SEARCH_COUNT = 80;
    private static final String[] SEARCH_SEEDS = {
            "San", "New", "Port", "Lake", "Mount", "City", "Saint", "University", "Fort", "Santa"
    };
    private static final HttpClient CLIENT = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(12))
            .build();

    public ClimateScraperResult scrape() throws ClimateScraperException {
        try {
            List<StationSeed> stations = fetchStationSeeds();
            if (stations.size() < TARGET_NODES) {
                throw new ClimateScraperException("Only found " + stations.size() + " geocoded nodes; need " + TARGET_NODES);
            }
            List<CityClimateNode> nodes = fetchWeather(stations.subList(0, TARGET_NODES));
            if (nodes.size() < TARGET_NODES) {
                throw new ClimateScraperException("Only parsed " + nodes.size() + " weather nodes; need " + TARGET_NODES);
            }
            return new ClimateScraperResult(nodes, stations.size());
        } catch (IOException e) {
            throw new ClimateScraperException("Network unavailable: " + e.getMessage(), e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ClimateScraperException("Request interrupted", e);
        }
    }

    private List<StationSeed> fetchStationSeeds() throws IOException, InterruptedException {
        Map<String, StationSeed> unique = new LinkedHashMap<>();

        for (StationSeed station : curatedObservationSeeds()) {
            unique.putIfAbsent(station.id, station);
        }

        for (String seed : SEARCH_SEEDS) {
            String url = "https://geocoding-api.open-meteo.com/v1/search?name="
                    + URLEncoder.encode(seed, StandardCharsets.UTF_8)
                    + "&count=" + SEARCH_COUNT + "&language=en&format=json";
            String json = get(url);
            for (String object : extractObjectsFromArray(json, "results")) {
                StationSeed station = parseStation(object);
                if (station != null && station.population > 0) {
                    unique.putIfAbsent(station.id, station);
                }
                if (unique.size() >= TARGET_NODES + 20) {
                    return new ArrayList<>(unique.values());
                }
            }
        }

        return new ArrayList<>(unique.values());
    }

    private List<CityClimateNode> fetchWeather(List<StationSeed> stations) throws IOException, InterruptedException {
        StringBuilder latitudes = new StringBuilder();
        StringBuilder longitudes = new StringBuilder();

        for (int i = 0; i < stations.size(); i++) {
            if (i > 0) {
                latitudes.append(',');
                longitudes.append(',');
            }
            latitudes.append(String.format(Locale.US, "%.5f", stations.get(i).latitude));
            longitudes.append(String.format(Locale.US, "%.5f", stations.get(i).longitude));
        }

        String url = "https://api.open-meteo.com/v1/forecast?latitude=" + latitudes
                + "&longitude=" + longitudes
                + "&current=temperature_2m,relative_humidity_2m,precipitation,weather_code,wind_speed_10m"
                + "&timezone=auto";

        String json = get(url);
        List<String> weatherObjects = splitTopLevelObjects(json);
        List<CityClimateNode> nodes = new ArrayList<>();

        for (int i = 0; i < Math.min(stations.size(), weatherObjects.size()); i++) {
            StationSeed seed = stations.get(i);
            String current = extractObject(weatherObjects.get(i), "current");
            if (current == null) {
                continue;
            }

            String time = extractString(current, "time");
            double temperature = extractDouble(current, "temperature_2m", 0.0);
            int humidity = (int) extractDouble(current, "relative_humidity_2m", 0.0);
            double precipitation = extractDouble(current, "precipitation", 0.0);
            int weatherCode = (int) extractDouble(current, "weather_code", 0.0);
            double windSpeed = extractDouble(current, "wind_speed_10m", 0.0);

            nodes.add(createNode(seed, time, temperature, humidity, precipitation, weatherCode, windSpeed));
        }

        return nodes;
    }

    private CityClimateNode createNode(StationSeed seed, String time, double temperature, int humidity,
                                       double precipitation, int weatherCode, double windSpeed) {
        if (seed.elevation > 850.0) {
            return new HighAltitudeClimateNode(seed.id, seed.name, seed.country, seed.region, seed.admin1, seed.latitude,
                    seed.longitude, seed.elevation, time, temperature, humidity, precipitation, weatherCode, windSpeed);
        }
        if (isCoastal(seed)) {
            return new CoastalClimateNode(seed.id, seed.name, seed.country, seed.region, seed.admin1, seed.latitude,
                    seed.longitude, seed.elevation, time, temperature, humidity, precipitation, weatherCode, windSpeed);
        }
        return new CityClimateNode(seed.id, seed.name, seed.country, seed.region, seed.admin1, seed.latitude,
                seed.longitude, seed.elevation, time, temperature, humidity, precipitation, weatherCode, windSpeed);
    }

    private boolean isCoastal(StationSeed seed) {
        String text = (seed.name + " " + seed.admin1 + " " + seed.country).toLowerCase();
        return seed.elevation < 80.0 && (text.contains("port") || text.contains("san ")
                || text.contains("bay") || text.contains("beach") || text.contains("island")
                || text.contains("new ") || text.contains("santa"));
    }

    private List<StationSeed> curatedObservationSeeds() {
        List<StationSeed> seeds = new ArrayList<>();

        addSeed(seeds, "cn-beijing", "Beijing", "China", "Beijing", 39.9042, 116.4074, 44, 21_500_000);
        addSeed(seeds, "cn-shanghai", "Shanghai", "China", "Shanghai", 31.2304, 121.4737, 4, 24_800_000);
        addSeed(seeds, "cn-guangzhou", "Guangzhou", "China", "Guangdong", 23.1291, 113.2644, 21, 18_700_000);
        addSeed(seeds, "cn-shenzhen", "Shenzhen", "China", "Guangdong", 22.5431, 114.0579, 40, 17_600_000);
        addSeed(seeds, "cn-chengdu", "Chengdu", "China", "Sichuan", 30.5728, 104.0668, 500, 21_200_000);
        addSeed(seeds, "cn-chongqing", "Chongqing", "China", "Chongqing", 29.5630, 106.5516, 244, 32_000_000);
        addSeed(seeds, "cn-kunming", "Kunming", "China", "Yunnan", 25.0389, 102.7183, 1892, 8_500_000);
        addSeed(seeds, "cn-lijiang", "Lijiang", "China", "Yunnan", 26.8565, 100.2271, 2400, 1_200_000);
        addSeed(seeds, "cn-lhasa", "Lhasa", "China", "Tibet", 29.6520, 91.1721, 3650, 870_000);
        addSeed(seeds, "cn-xining", "Xining", "China", "Qinghai", 36.6171, 101.7782, 2275, 2_400_000);
        addSeed(seeds, "cn-urumqi", "Urumqi", "China", "Xinjiang", 43.8256, 87.6168, 800, 4_000_000);
        addSeed(seeds, "cn-dunhuang", "Dunhuang", "China", "Gansu", 40.1421, 94.6619, 1139, 190_000);
        addSeed(seeds, "cn-hohhot", "Hohhot", "China", "Inner Mongolia", 40.8424, 111.7490, 1065, 3_500_000);
        addSeed(seeds, "cn-hangzhou", "Hangzhou", "China", "Zhejiang", 30.2741, 120.1551, 19, 12_300_000);
        addSeed(seeds, "cn-nanjing", "Nanjing", "China", "Jiangsu", 32.0603, 118.7969, 20, 9_400_000);
        addSeed(seeds, "cn-xian", "Xi'an", "China", "Shaanxi", 34.3416, 108.9398, 405, 13_000_000);
        addSeed(seeds, "cn-sanya", "Sanya", "China", "Hainan", 18.2528, 109.5120, 7, 1_000_000);
        addSeed(seeds, "cn-tianjin", "Tianjin", "China", "Tianjin", 39.3434, 117.3616, 6, 13_600_000);
        addSeed(seeds, "cn-wuhan", "Wuhan", "China", "Hubei", 30.5928, 114.3055, 37, 13_700_000);
        addSeed(seeds, "cn-harbin", "Harbin", "China", "Heilongjiang", 45.8038, 126.5349, 150, 10_000_000);
        addSeed(seeds, "cn-changsha", "Changsha", "China", "Hunan", 28.2282, 112.9388, 63, 10_400_000);
        addSeed(seeds, "cn-fuzhou", "Fuzhou", "China", "Fujian", 26.0745, 119.2965, 14, 8_400_000);
        addSeed(seeds, "cn-xiamen", "Xiamen", "China", "Fujian", 24.4798, 118.0894, 5, 5_300_000);
        addSeed(seeds, "cn-guiyang", "Guiyang", "China", "Guizhou", 26.6470, 106.6302, 1100, 6_100_000);
        addSeed(seeds, "cn-nanning", "Nanning", "China", "Guangxi", 22.8170, 108.3669, 72, 8_800_000);
        addSeed(seeds, "cn-zhengzhou", "Zhengzhou", "China", "Henan", 34.7466, 113.6254, 108, 12_800_000);
        addSeed(seeds, "cn-jinan", "Jinan", "China", "Shandong", 36.6512, 117.1201, 23, 9_200_000);
        addSeed(seeds, "cn-qingdao", "Qingdao", "China", "Shandong", 36.0671, 120.3826, 25, 10_300_000);
        addSeed(seeds, "cn-hefei", "Hefei", "China", "Anhui", 31.8206, 117.2272, 29, 9_600_000);
        addSeed(seeds, "cn-shijiazhuang", "Shijiazhuang", "China", "Hebei", 38.0428, 114.5149, 83, 11_200_000);

        addSeed(seeds, "us-los-angeles", "Los Angeles", "United States", "California", 34.0522, -118.2437, 89, 3_900_000);
        addSeed(seeds, "us-san-francisco", "San Francisco", "United States", "California", 37.7749, -122.4194, 16, 815_000);
        addSeed(seeds, "us-san-diego", "San Diego", "United States", "California", 32.7157, -117.1611, 19, 1_400_000);
        addSeed(seeds, "us-flagstaff", "Flagstaff", "United States", "Arizona", 35.1983, -111.6513, 2106, 76_000);
        addSeed(seeds, "us-tucson", "Tucson", "United States", "Arizona", 32.2226, -110.9747, 728, 543_000);
        addSeed(seeds, "us-albuquerque", "Albuquerque", "United States", "New Mexico", 35.0844, -106.6504, 1619, 560_000);
        addSeed(seeds, "us-santa-fe", "Santa Fe", "United States", "New Mexico", 35.6870, -105.9378, 2194, 89_000);
        addSeed(seeds, "us-denver", "Denver", "United States", "Colorado", 39.7392, -104.9903, 1609, 713_000);
        addSeed(seeds, "us-boulder", "Boulder", "United States", "Colorado", 40.0150, -105.2705, 1624, 105_000);
        addSeed(seeds, "us-salt-lake-city", "Salt Lake City", "United States", "Utah", 40.7608, -111.8910, 1288, 200_000);
        addSeed(seeds, "us-moab", "Moab", "United States", "Utah", 38.5733, -109.5498, 1227, 5_300);
        addSeed(seeds, "us-austin", "Austin", "United States", "Texas", 30.2672, -97.7431, 149, 974_000);
        addSeed(seeds, "us-marfa", "Marfa", "United States", "Texas", 30.3095, -104.0206, 1432, 1_800);
        addSeed(seeds, "us-new-york", "New York", "United States", "New York", 40.7128, -74.0060, 10, 8_300_000);
        addSeed(seeds, "us-buffalo", "Buffalo", "United States", "New York", 42.8864, -78.8784, 183, 276_000);
        addSeed(seeds, "us-seattle", "Seattle", "United States", "Washington", 47.6062, -122.3321, 52, 749_000);
        addSeed(seeds, "us-honolulu", "Honolulu", "United States", "Hawaii", 21.3099, -157.8581, 5, 350_000);
        addSeed(seeds, "us-las-vegas", "Las Vegas", "United States", "Nevada", 36.1716, -115.1391, 610, 656_000);
        addSeed(seeds, "us-reno", "Reno", "United States", "Nevada", 39.5296, -119.8138, 1373, 264_000);
        addSeed(seeds, "us-bend", "Bend", "United States", "Oregon", 44.0582, -121.3153, 1104, 103_000);
        addSeed(seeds, "us-portland", "Portland", "United States", "Oregon", 45.5152, -122.6784, 15, 635_000);
        addSeed(seeds, "us-anchorage", "Anchorage", "United States", "Alaska", 61.2176, -149.8997, 31, 288_000);
        addSeed(seeds, "us-fairbanks", "Fairbanks", "United States", "Alaska", 64.8378, -147.7164, 136, 32_000);
        addSeed(seeds, "us-boston", "Boston", "United States", "Massachusetts", 42.3601, -71.0589, 43, 675_000);
        addSeed(seeds, "us-chicago", "Chicago", "United States", "Illinois", 41.8781, -87.6298, 181, 2_700_000);
        addSeed(seeds, "us-miami", "Miami", "United States", "Florida", 25.7617, -80.1918, 2, 442_000);
        addSeed(seeds, "us-atlanta", "Atlanta", "United States", "Georgia", 33.7490, -84.3880, 320, 499_000);
        addSeed(seeds, "us-minneapolis", "Minneapolis", "United States", "Minnesota", 44.9778, -93.2650, 253, 425_000);
        addSeed(seeds, "us-boise", "Boise", "United States", "Idaho", 43.6150, -116.2023, 824, 236_000);
        addSeed(seeds, "us-asheville", "Asheville", "United States", "North Carolina", 35.5951, -82.5515, 650, 95_000);

        return seeds;
    }

    private void addSeed(List<StationSeed> seeds, String id, String name, String country, String admin1,
                         double latitude, double longitude, double elevation, int population) {
        seeds.add(new StationSeed(id, name, country, admin1, classifyRegion(latitude, longitude),
                latitude, longitude, elevation, population));
    }

    private StationSeed parseStation(String object) {
        String id = extractRaw(object, "id");
        String name = extractString(object, "name");
        String country = extractString(object, "country");
        String admin1 = extractString(object, "admin1");
        double latitude = extractDouble(object, "latitude", 999.0);
        double longitude = extractDouble(object, "longitude", 999.0);
        double elevation = extractDouble(object, "elevation", 0.0);
        int population = (int) extractDouble(object, "population", 0.0);

        if (id == null || name == null || country == null || latitude == 999.0 || longitude == 999.0) {
            return null;
        }

        return new StationSeed(id, name, country, admin1, classifyRegion(latitude, longitude),
                latitude, longitude, elevation, population);
    }

    private String classifyRegion(double latitude, double longitude) {
        if (longitude >= -170 && longitude <= -30 && latitude >= 5) {
            return "North America";
        }
        if (longitude >= -90 && longitude <= -30 && latitude < 5) {
            return "South America";
        }
        if (longitude >= -25 && longitude <= 60 && latitude >= 35) {
            return "Europe";
        }
        if (longitude >= -20 && longitude <= 55 && latitude < 35) {
            return "Africa";
        }
        if (longitude >= 55 && longitude <= 180 && latitude > -10) {
            return "Asia";
        }
        if (longitude >= 100 && longitude <= 180 && latitude <= -10) {
            return "Oceania";
        }
        return "Other";
    }

    private String get(String url) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder(URI.create(url))
                .timeout(Duration.ofSeconds(20))
                .header("User-Agent", "ClimateShield/1.0 Educational Project")
                .GET()
                .build();
        HttpResponse<String> response = CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            throw new IOException("HTTP " + response.statusCode());
        }
        return response.body();
    }

    private List<String> extractObjectsFromArray(String json, String key) {
        int keyIndex = json.indexOf("\"" + key + "\"");
        if (keyIndex < 0) {
            return List.of();
        }
        int start = json.indexOf('[', keyIndex);
        int end = findMatching(json, start, '[', ']');
        if (start < 0 || end < 0) {
            return List.of();
        }
        return splitTopLevelObjects(json.substring(start, end + 1));
    }

    private List<String> splitTopLevelObjects(String json) {
        List<String> objects = new ArrayList<>();
        int depth = 0;
        int start = -1;
        boolean inString = false;
        boolean escaped = false;

        for (int i = 0; i < json.length(); i++) {
            char c = json.charAt(i);
            if (inString) {
                if (escaped) {
                    escaped = false;
                } else if (c == '\\') {
                    escaped = true;
                } else if (c == '"') {
                    inString = false;
                }
                continue;
            }
            if (c == '"') {
                inString = true;
            } else if (c == '{') {
                if (depth == 0) {
                    start = i;
                }
                depth++;
            } else if (c == '}') {
                depth--;
                if (depth == 0 && start >= 0) {
                    objects.add(json.substring(start, i + 1));
                    start = -1;
                }
            }
        }
        return objects;
    }

    private String extractObject(String json, String key) {
        int keyIndex = json.indexOf("\"" + key + "\"");
        if (keyIndex < 0) {
            return null;
        }
        int start = json.indexOf('{', keyIndex);
        int end = findMatching(json, start, '{', '}');
        if (start < 0 || end < 0) {
            return null;
        }
        return json.substring(start, end + 1);
    }

    private int findMatching(String text, int start, char open, char close) {
        if (start < 0) {
            return -1;
        }
        int depth = 0;
        boolean inString = false;
        boolean escaped = false;
        for (int i = start; i < text.length(); i++) {
            char c = text.charAt(i);
            if (inString) {
                if (escaped) {
                    escaped = false;
                } else if (c == '\\') {
                    escaped = true;
                } else if (c == '"') {
                    inString = false;
                }
                continue;
            }
            if (c == '"') {
                inString = true;
            } else if (c == open) {
                depth++;
            } else if (c == close) {
                depth--;
                if (depth == 0) {
                    return i;
                }
            }
        }
        return -1;
    }

    private String extractString(String json, String key) {
        Pattern pattern = Pattern.compile("\"" + Pattern.quote(key) + "\"\\s*:\\s*\"(.*?)\"");
        Matcher matcher = pattern.matcher(json);
        return matcher.find() ? unescape(matcher.group(1)) : null;
    }

    private String extractRaw(String json, String key) {
        Pattern pattern = Pattern.compile("\"" + Pattern.quote(key) + "\"\\s*:\\s*([^,}\\]]+)");
        Matcher matcher = pattern.matcher(json);
        return matcher.find() ? matcher.group(1).replace("\"", "").trim() : null;
    }

    private double extractDouble(String json, String key, double fallback) {
        String raw = extractRaw(json, key);
        if (raw == null || raw.equals("null")) {
            return fallback;
        }
        try {
            return Double.parseDouble(raw);
        } catch (NumberFormatException e) {
            return fallback;
        }
    }

    private String unescape(String text) {
        return text.replace("\\\"", "\"").replace("\\/", "/");
    }

    private static class StationSeed {
        private final String id;
        private final String name;
        private final String country;
        private final String admin1;
        private final String region;
        private final double latitude;
        private final double longitude;
        private final double elevation;
        private final int population;

        private StationSeed(String id, String name, String country, String admin1, String region,
                            double latitude, double longitude, double elevation, int population) {
            this.id = id;
            this.name = name;
            this.country = country;
            this.admin1 = admin1 == null ? "" : admin1;
            this.region = region;
            this.latitude = latitude;
            this.longitude = longitude;
            this.elevation = elevation;
            this.population = population;
        }
    }
}
