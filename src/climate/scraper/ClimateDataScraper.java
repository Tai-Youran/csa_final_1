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

    private static final int TARGET_NODES = 60;
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
            return new HighAltitudeClimateNode(seed.id, seed.name, seed.country, seed.region, seed.latitude,
                    seed.longitude, seed.elevation, time, temperature, humidity, precipitation, weatherCode, windSpeed);
        }
        if (isCoastal(seed)) {
            return new CoastalClimateNode(seed.id, seed.name, seed.country, seed.region, seed.latitude,
                    seed.longitude, seed.elevation, time, temperature, humidity, precipitation, weatherCode, windSpeed);
        }
        return new CityClimateNode(seed.id, seed.name, seed.country, seed.region, seed.latitude,
                seed.longitude, seed.elevation, time, temperature, humidity, precipitation, weatherCode, windSpeed);
    }

    private boolean isCoastal(StationSeed seed) {
        String text = (seed.name + " " + seed.admin1 + " " + seed.country).toLowerCase();
        return seed.elevation < 80.0 && (text.contains("port") || text.contains("san ")
                || text.contains("bay") || text.contains("beach") || text.contains("island")
                || text.contains("new ") || text.contains("santa"));
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
