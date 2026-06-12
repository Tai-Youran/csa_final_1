package climate.model;

public class CoastalClimateNode extends CityClimateNode {

    public CoastalClimateNode(String id, String city, String country, String region,
                              double latitude, double longitude, double elevation,
                              String localTime, double temperatureC, int humidity,
                              double precipitationMm, int weatherCode, double windSpeedKmh) {
        this(id, city, country, region, "", latitude, longitude, elevation, localTime,
                temperatureC, humidity, precipitationMm, weatherCode, windSpeedKmh);
    }

    public CoastalClimateNode(String id, String city, String country, String region, String adminArea,
                              double latitude, double longitude, double elevation,
                              String localTime, double temperatureC, int humidity,
                              double precipitationMm, int weatherCode, double windSpeedKmh) {
        super(id, city, country, region, adminArea, latitude, longitude, elevation, localTime,
                temperatureC, humidity, precipitationMm, weatherCode, windSpeedKmh);
    }

    @Override
    public double calculateRiskScore() {
        double coastalExposure = getWindSpeedKmh() > 25.0 ? 8.0 : 3.0;
        double floodExposure = getPrecipitationMm() > 2.0 ? 8.0 : 0.0;
        return Math.min(100.0, super.calculateRiskScore() + coastalExposure + floodExposure);
    }

    @Override
    public String getNodeType() {
        return "Coastal";
    }
}
