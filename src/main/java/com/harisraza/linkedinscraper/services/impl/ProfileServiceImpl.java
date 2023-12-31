package com.harisraza.linkedinscraper.services.impl;

import com.harisraza.linkedinscraper.config.ScraperConfig;
import com.harisraza.linkedinscraper.models.Profile;
import com.harisraza.linkedinscraper.models.Search;
import com.harisraza.linkedinscraper.repositories.ProfileRepository;
import com.harisraza.linkedinscraper.services.ProfileService;
import com.harisraza.linkedinscraper.services.SearchService;
import com.harisraza.linkedinscraper.utils.ProfileScraperFilters;
import com.harisraza.linkedinscraper.utils.ProfileScraperParameters;
import com.harisraza.linkedinscraper.utils.WebDriverHelper;
import org.bson.types.ObjectId;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ProfileServiceImpl implements ProfileService {

    private final ProfileRepository profileRepository;
    private final SearchService searchService;

    private WebDriverHelper driverHelper;

    private String currentStatus = "";
    private final String DEFAULT_STATUS = "";
    private boolean isScraperRunning = false;
    private boolean scrapedSuccess = false;

    @Autowired
    public ProfileServiceImpl(ProfileRepository profileRepository, SearchService searchService) {
        this.profileRepository = profileRepository;
        this.searchService = searchService;
    }

    private Set<Profile> scrapProfiles(Set<String> profileLinks)  {
        this.currentStatus = "Extracting required information from each profile. (it might take some time)";
        return profileLinks.stream().map((profileLink) -> scrapeProfile(profileLink)).collect(Collectors.toSet());
    }

    private Profile scrapeProfile(String profileLink) {
        Profile profile = new Profile(driverHelper, profileLink);
        profile.fetchInformation();
        return profile;
    }


    private String getFilters(ProfileScraperFilters profileFilters) {
        if (profileFilters == null) return "";

        StringBuilder connectionFilter = new StringBuilder();


        Boolean[] connectionChecked = {
                profileFilters.getIsFirstConnectionChecked(),
                profileFilters.getIsSecondConnectionChecked(),
                profileFilters.getIsThirdConnectionChecked()
        };

        if (Arrays.stream(connectionChecked).anyMatch(Objects::nonNull)) {
            connectionFilter.append("&network=%5B");
            boolean isFirst = true;

            String[] connectionTypes = {"F", "S", "O"};

            for (int i = 0; i < connectionChecked.length; i++) {
                if (connectionChecked[i] != null && connectionChecked[i]) {
                    if (!isFirst) {
                        connectionFilter.append("%2C");
                    }
                    connectionFilter.append("%22").append(connectionTypes[i]).append("%22");
                    isFirst = false;
                }
            }

            connectionFilter.append("%5D");
        }

        String industriesFilter = "";
        String locationsFilter = "";

        if(profileFilters.getSelectedLocations().size() > 0) {
            locationsFilter = profileFilters.getSelectedLocations().stream()
                    .map(location -> "%22" + location + "%22")
                    .collect(Collectors.joining("%2C", "&location=", ""));
        }

        if (profileFilters.getSelectedIndustries().size() > 0) {
            industriesFilter = profileFilters.getSelectedIndustries().stream()
                    .map(industry -> "%22" + industry + "%22")
                    .collect(Collectors.joining("%2C", "&industry=", ""));
        }

        return connectionFilter + locationsFilter + industriesFilter;
    }

    private Set<String> retrieveProfileLinks(ProfileScraperParameters profileParameters)  {
        this.currentStatus = String.format("Retrieving %s profiles (it might take some time).", profileParameters.getTotalProfilesToFetch());
        Set<String> profileLinks = new HashSet<>();

        boolean isNextPageAvailable = true;
        int pageToGoNext = 1;
        int profilesRetrieved = 0;
        long totalProfilesToRetrieve = profileParameters.getTotalProfilesToFetch();
        String filters = getFilters(profileParameters.getFilters());
        String keywords = profileParameters.getKeywords();


        while ((isNextPageAvailable && profilesRetrieved < totalProfilesToRetrieve) || (isNextPageAvailable && totalProfilesToRetrieve == -1)) {
            driverHelper.goToPage(String.format("https://www.linkedin.com/search/results/people/?page=%d%s&keywords=%s", pageToGoNext, filters, keywords));

            List<WebElement> profilesElements = driverHelper.getElementsIfExists(By.xpath("//a[contains(@class, 'app-aware-link') and contains(@href, '/in/')]"));

            if(profilesElements != null && profilesElements.size() > 0) {
                for (WebElement profileElement : profilesElements) {
                    String profileLink = profileElement.getAttribute("href");
                    int endIndex = profileLink.indexOf("?miniProfile");

                    if (endIndex != -1 && profileLinks.add(profileLink.substring(0, endIndex))) {
                        profilesRetrieved++;
                        if (profilesRetrieved >= totalProfilesToRetrieve && totalProfilesToRetrieve != -1) {
                            break;
                        }
                    }
                }
            }

            WebElement noResultPageElement = driverHelper.getElementIfExist(By.xpath("//div[@class='search-reusable-search-no-results artdeco-card mb2']"));
            if(noResultPageElement != null) isNextPageAvailable = false;
            pageToGoNext++;

        }

        return profileLinks;
    }

    @Override
    public void startScraper(ProfileScraperParameters profileParameters) {
        try {
            ScraperConfig config = new ScraperConfig();
            WebDriver driver = config.setupWebDriver(profileParameters.getHeadlessMode());
            this.driverHelper = new WebDriverHelper(driver);

            this.isScraperRunning = true;
            this.currentStatus = "Scraper started and logging into linkedin account.";

            boolean isLoggedIn = driverHelper.loginToLinkedIn(profileParameters.getEmail(), profileParameters.getPassword(), profileParameters.getHeadlessMode());
            if(!isLoggedIn) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Wrong LinkedIn login credentials.");
            }

            this.currentStatus = "Logged in to linkedin successfully.";
            Set<String> profileLinks = retrieveProfileLinks(profileParameters);
            Set<Profile> profiles = scrapProfiles(profileLinks);

            // save it to database
            saveToDatabase(profiles, profileParameters.getTitle());
            this.currentStatus = "Successfully saved fetch profiles into database... Shutting down scraper...";
            this.scrapedSuccess = true;
            this.isScraperRunning  = false;

        } catch (Exception e) {
            driverHelper.shutDownScraper();
            setDefaultStatus();
        } finally {
            driverHelper.shutDownScraper();
            setDefaultStatus();
        }
    }

    @Override
    public String getStatus() {
        if (!isScraperRunning && !scrapedSuccess)
            return DEFAULT_STATUS;
        return currentStatus;
    }
    @Override
    public boolean isScraperCurrentlyRunning() {
        return this.isScraperRunning;
    }

    @Override
    public boolean isScrapedSuccess() {
        return scrapedSuccess;
    }
    private void setDefaultStatus() {
        this.currentStatus = "";
        this.isScraperRunning = false;
        this.scrapedSuccess = false;
    }

    // below all methods communicate to database

    @Override
    public List<Profile> getProfiles() {
        return profileRepository.findAll();
    }

    @Transactional
    private void saveToDatabase(Set<Profile> profiles, String searchTitle) {
        try {
            this.currentStatus = "Saving fetched profiles to database.";
            Search search = new Search();
            search.setSearchedAt(LocalDateTime.now());
            search.setTitle(searchTitle);
            search.setId(new ObjectId().toString());

            Set<Profile> updatedProfiles = profiles.stream()
                    .peek((profile) -> profile.setSearch(search))
                    .collect(Collectors.toSet());

            profileRepository.saveAll(updatedProfiles);

            search.setProfiles(updatedProfiles.stream().toList());
            searchService.saveSearch(search);
        } catch (Exception e) {
            e.printStackTrace();
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Fetched all profiles but unable to save it to database.");
        }
    }
}
