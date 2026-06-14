package climate.model;

public class HighAltitudeClimateNode extends CityClimateNode {

    public HighAltitudeClimateNode(String id, String city, String country, String region,
                                   double latitude, double longitude, double elevation,
                                   String localTime, double temperatureC, int humidity,
                                   double precipitationMm, int weatherCode, double windSpeedKmh) {
        this(id, city, country, region, "", latitude, longitude, elevation, localTime,
                temperatureC, humidity, precipitationMm, weatherCode, windSpeedKmh);
    }

    public HighAltitudeClimateNode(String id, String city, String country, String region, String adminArea,
                                   double latitude, double longitude, double elevation,
                                   String localTime, double temperatureC, int humidity,
                                   double precipitationMm, int weatherCode, double windSpeedKmh) {
        super(id, city, country, region, adminArea, latitude, longitude, elevation, localTime,
                temperatureC, humidity, precipitationMm, weatherCode, windSpeedKmh);
    }

    @Override
    public double calculateRiskScore() {
        double altitudeExposure = getElevation() > 1200.0 ? 10.0 : 5.0;
        double freezeWindExposure = getTemperatureC() < 5.0 && getWindSpeedKmh() > 20.0 ? 10.0 : 0.0;
        return Math.min(100.0, super.calculateRiskScore() + altitudeExposure + freezeWindExposure);
    }

    @Override
    public String getNodeType() {
        return "Highland";
    }
}
