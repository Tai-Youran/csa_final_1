package climate.scraper;

public class ClimateScraperException extends Exception {

    public ClimateScraperException(String message) {
        super(message);
    }

    public ClimateScraperException(String message, Throwable cause) {
        super(message, cause);
    }
}
