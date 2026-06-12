package climate.model;

public class CityClimateNode extends ClimateData {

    private final String localTime;
    private final double temperatureC;
    private final int humidity;
    private final double precipitationMm;
    private final int weatherCode;
    private final double windSpeedKmh;

    public CityClimateNode(String id, String city, String country, String region,
                           double latitude, double longitude, double elevation,
                           String localTime, double temperatureC, int humidity,
                           double precipitationMm, int weatherCode, double windSpeedKmh) {
        super(id, city, country, region, latitude, longitude, elevation);
        this.localTime = localTime == null ? "" : localTime;
        this.temperatureC = temperatureC;
        this.humidity = Math.max(0, Math.min(100, humidity));
        this.precipitationMm = Math.max(0.0, precipitationMm);
        this.weatherCode = weatherCode;
        this.windSpeedKmh = Math.max(0.0, windSpeedKmh);
    }

    public String getLocalTime() {
        return localTime;
    }

    public double getTemperatureC() {
        return temperatureC;
    }

    public int getHumidity() {
        return humidity;
    }

    public double getPrecipitationMm() {
        return precipitationMm;
    }

    public int getWeatherCode() {
        return weatherCode;
    }

    public double getWindSpeedKmh() {
        return windSpeedKmh;
    }

    @Override
    public double calculateRiskScore() {
        double heatRisk = temperatureC > 32.0 ? (temperatureC - 32.0) * 3.0 : 0.0;
        double coldRisk = temperatureC < 0.0 ? Math.abs(temperatureC) * 2.0 : 0.0;
        double rainRisk = precipitationMm * 18.0;
        double windRisk = windSpeedKmh * 1.35;
        double humidityRisk = humidity > 85 ? (humidity - 85) * 0.8 : humidity < 20 ? (20 - humidity) * 0.6 : 0.0;
        double weatherRisk = severeWeatherBonus();
        return Math.min(100.0, heatRisk + coldRisk + rainRisk + windRisk + humidityRisk + weatherRisk);
    }

    @Override
    public String getNodeType() {
        return "Urban";
    }

    protected double severeWeatherBonus() {
        if (weatherCode >= 95) {
            return 30.0;
        }
        if (weatherCode >= 80) {
            return 18.0;
        }
        if (weatherCode >= 60) {
            return 10.0;
        }
        if (weatherCode >= 45) {
            return 5.0;
        }
        return 0.0;
    }
}
