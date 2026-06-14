package scraper;

import model.TechNewsArticle;

import java.util.List;

public interface WebScraper {

    List<TechNewsArticle> scrape() throws ScraperException;
}
