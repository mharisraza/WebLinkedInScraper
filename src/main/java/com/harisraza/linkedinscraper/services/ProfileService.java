package com.harisraza.linkedinscraper.services;

import com.harisraza.linkedinscraper.helper.ProfileScraperParameters;
import com.harisraza.linkedinscraper.models.Profile;

import java.util.List;

public interface ProfileService {

    List<Profile> getProfiles();
    void startScraper(ProfileScraperParameters profileParameters);

    String getStatus();
    boolean isScraperCurrentlyRunning();
    boolean isScrapedSuccess();

}
