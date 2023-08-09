package com.harisraza.linkedinscraper.services;

import com.harisraza.linkedinscraper.models.Profile;
import com.harisraza.linkedinscraper.utils.ProfileScraperParameters;

import java.util.List;

public interface ProfileService {

    List<Profile> getProfiles();
    void startScraper(ProfileScraperParameters profileParameters);

    String getStatus();
    boolean isScraperCurrentlyRunning();
    boolean isScrapedSuccess();

}
