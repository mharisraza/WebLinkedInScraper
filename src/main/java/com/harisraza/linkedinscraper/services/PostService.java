package com.harisraza.linkedinscraper.services;

import com.harisraza.linkedinscraper.utils.PostScraperParameters;

public interface PostService {
    void startScraper(PostScraperParameters postsParameters);
    String getStatus();
    boolean isScraperIsCurrentlyRunning();
    boolean isScrapedSuccess();
}
