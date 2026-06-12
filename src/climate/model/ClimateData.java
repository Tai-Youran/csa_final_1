package climate.model;

public abstract class ClimateData {

    private final String id;
    private final String city;
    private final String country;
    private final String region;
    private final double latitude;
    private final double longitude;
    private final double elevation;
    private final long fetchedAt;

    protected ClimateData(String id, String city, String country, String region,
                          double latitude, double longitude, double elevation) {
        this.id = id == null || id.isBlank() ? "unknown" : id;
        this.city = city == null || city.isBlank() ? "Unknown" : city;
        this.country = country == null || country.isBlank() ? "Unknown" : country;
        this.region = region == null || region.isBlank() ? "Other" : region;
        this.latitude = latitude;
        this.longitude = longitude;
        this.elevation = elevation;
        this.fetchedAt = System.currentTimeMillis();
    }

    public String getId() {
        return id;
    }

    public String getCity() {
        return city;
    }

    public String getCountry() {
        return country;
    }

    public String getRegion() {
        return region;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public double getElevation() {
        return elevation;
    }

    public long getFetchedAt() {
        return fetchedAt;
    }

    public abstract double calculateRiskScore();

    public abstract String getNodeType();

    public String getRiskBand() {
        double risk = calculateRiskScore();
        if (risk >= 80.0) {
            return "Critical";
        }
        if (risk >= 60.0) {
            return "High";
        }
        if (risk >= 35.0) {
            return "Guarded";
        }
        return "Stable";
    }
}
