package com.harisraza.linkedinscraper.services.impl;

import com.harisraza.linkedinscraper.helper.ProfileScraperFilters;
import com.harisraza.linkedinscraper.helper.WebDriverHelper;
import com.harisraza.linkedinscraper.config.ScraperConfig;
import com.harisraza.linkedinscraper.helper.ProfileScraperParameters;
import com.harisraza.linkedinscraper.models.Profile;
import com.harisraza.linkedinscraper.models.Search;
import com.harisraza.linkedinscraper.repositories.ProfileRepository;
import com.harisraza.linkedinscraper.services.ProfileService;
import com.harisraza.linkedinscraper.services.SearchService;
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

    @Autowired private ProfileRepository profileRepository;
    @Autowired private SearchService searchService;

    private WebDriverHelper driverHelper;

    private String currentStatus = "";
    private final String DEFAULT_STATUS = "";
    private boolean isScraperRunning = false;
    private boolean scrapedSuccess = false;

    private Set<Profile> scrapProfiles(Set<String> profileLinks) throws InterruptedException {
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

            if (Arrays.stream(connectionChecked).anyMatch(c -> c != null)) {
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

    private Set<String> retrieveProfileLinks(ProfileScraperParameters profileParameters) throws InterruptedException {
        this.currentStatus = String.format("Retrieving %s profiles (it might take some time).", profileParameters.getTotalProfilesToFetch());
        Set<String> profileLinks = new HashSet<>();

        boolean isNextPageAvailable = true;
        int pageToGoNext = 1;
        int profilesRetrieved = 0;
        long totalProfilesToRetrieve = profileParameters.getTotalProfilesToFetch();
        String filters = getFilters(profileParameters.getFilters());
        String keywords = profileParameters.getKeywords();


        while ((isNextPageAvailable && profilesRetrieved < totalProfilesToRetrieve) || (isNextPageAvailable && totalProfilesToRetrieve == -1)) {
            // going to search results
            driverHelper.getDriver().get(String.format("https://www.linkedin.com/search/results/people/?page=%d%s&keywords=%s", pageToGoNext, filters, keywords));
            Thread.sleep(1000);
            pageToGoNext++;

            List<WebElement> profilesElements = driverHelper.getDriver().findElements(By.xpath("//a[contains(@class, 'app-aware-link') and contains(@href, '/in/')]"));

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

            WebElement noResultPageElement = driverHelper.getElementIfExist(By.xpath("//div[@class='search-reusable-search-no-results artdeco-card mb2']"));
            if(noResultPageElement != null) isNextPageAvailable = false;
        }

        return profileLinks;
    }

    private boolean loginToLinkedIn(String emailAddress, String password) throws InterruptedException {
        // going to LinkedIn login page.
        driverHelper.getDriver().get("https://www.linkedin.com/login");
        Thread.sleep(1000);

        // entering details and then clicking on login button to logged in.
        driverHelper.getDriver().findElement(By.xpath("/html/body/div/main/div[2]/div[1]/form/div[1]/input")).sendKeys(emailAddress);
        driverHelper.getDriver().findElement(By.xpath("/html/body/div/main/div[2]/div[1]/form/div[2]/input")).sendKeys(password);
        driverHelper.getDriver().findElement(By.xpath("/html/body/div/main/div[2]/div[1]/form/div[3]/button")).click();
        Thread.sleep(2500);

        // if verification captcha required make it sleep for 15 seconds to verify manually.
        if(driverHelper.getDriver().getCurrentUrl().contains("checkpoint")) Thread.sleep(15000);

        WebElement wrongCredentialsElement = driverHelper.getElementIfExist(By.id("error-for-password"));
        return wrongCredentialsElement == null;
    }

    @Override
    public void startScraper(ProfileScraperParameters profileParameters) {
        try {
            ScraperConfig config = new ScraperConfig();
            WebDriver driver = config.setupWebDriver(profileParameters.isHeadlessMode());
            this.driverHelper = new WebDriverHelper(driver);

            this.isScraperRunning = true;
            this.currentStatus = "Scraper started and logging into linkedin account.";

            boolean isLoggedIn = loginToLinkedIn(profileParameters.getEmail(), profileParameters.getPassword());
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
            driverHelper.getDriver().quit();
            setDefaultStatus();
        } finally {
            driverHelper.getDriver().quit();
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
