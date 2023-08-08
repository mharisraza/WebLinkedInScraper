package com.harisraza.linkedinscraper.services;

import com.harisraza.linkedinscraper.helper.PostScraperParameters;

public interface PostService {
    void startScraper(PostScraperParameters postsParameters);
    String getStatus();
    boolean isScraperIsCurrentlyRunning();
    boolean isScrapedSuccess();
}
