package scraper;

public class InsufficientDataException extends ScraperException {

    public InsufficientDataException(int found, int required) {
        super("Insufficient data: found " + found + " nodes, required " + required + "+");
    }
}
