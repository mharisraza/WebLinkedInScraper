package com.harisraza.linkedinscraper.services.impl;

import com.harisraza.linkedinscraper.config.ScraperConfig;
import com.harisraza.linkedinscraper.models.Post;
import com.harisraza.linkedinscraper.models.Search;
import com.harisraza.linkedinscraper.repositories.PostRepository;
import com.harisraza.linkedinscraper.services.PostService;
import com.harisraza.linkedinscraper.services.SearchService;
import com.harisraza.linkedinscraper.utils.PostScraperParameters;
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
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class PostServiceImpl implements PostService {

    private final PostRepository postRepository;
    private final SearchService searchService;
    private WebDriverHelper driverHelper;

    private String currentStatus = "";
    private final String DEFAULT_STATUS = "";
    private boolean isScraperRunning = false;
    private boolean scrapedSuccess = false;

    @Autowired
    public PostServiceImpl(PostRepository postRepository, SearchService searchService) {
        this.postRepository = postRepository;
        this.searchService = searchService;
    }

    private String getFilters(String datePosted, String sortBy) {
        return (datePosted != null ? "&datePosted=%22" + datePosted + "%22" : "") +
                (sortBy != null ? "&sortedBy=%22" + sortBy + "%22" : "");
    }

    private Set<Post> fetchPosts(PostScraperParameters postParameters) throws InterruptedException {
        this.currentStatus = String.format("Retrieving %s posts. (it might take some time)", postParameters.getTotalPostsToFetch());

        final Set<Post> posts = new HashSet<>();

        int pageToGoNext = 1;
        int postsRetrieved = 0;

        long totalPostsToRetrieve = postParameters.getTotalPostsToFetch();
        String filters = getFilters(postParameters.getDatePosted(), postParameters.getSortBy());
        String keywords = postParameters.getKeywords();
        if (keywords == null) keywords = "";

        while (postsRetrieved < totalPostsToRetrieve || totalPostsToRetrieve == -1) {
            driverHelper.goToPage(String.format("https://www.linkedin.com/search/results/content/?page=%d&keywords=%s%s", pageToGoNext, keywords, filters));

            WebElement noPageElement = driverHelper.getElementIfExist(By.xpath("//div[@class='search-reusable-search-no-results artdeco-card mb2']"));
            if (noPageElement != null) {
                break;
            }

            final String finalKeywords = keywords;
            List<WebElement> postsElements = driverHelper.getElementsIfExists(By.xpath("//div[contains(@class, 'feed-shared-update-v2 feed-shared-update-v2--minimal-padding')]"));
            this.currentStatus = "Extracting required information from each post.. (it might take some times)";

            if(postsElements != null && postsElements.size() > 0) {
                for (WebElement postElement : postsElements) {
                    Post post = extractPostInformation(postElement, finalKeywords);
                    posts.add(post);
                    postsRetrieved++;
                    if(postsRetrieved == totalPostsToRetrieve && totalPostsToRetrieve != -1) {
                        break;
                    }
                }
            }

            pageToGoNext++;
        }
        return posts;
    }

    private Post extractPostInformation(WebElement postElement, String matchedKeywords) {
        String content = driverHelper.getRelatedElementIfExist(postElement, By.xpath(".//div[@class='update-components-text relative feed-shared-update-v2__commentary ' and @dir='ltr']/span/span")).getText();
        String link = String.format("https://www.linkedin.com/feed/update/%s", postElement.getAttribute("data-urn"));
        String by = driverHelper.getRelatedElementIfExist(postElement, By.xpath(".//div[contains(@class, 'update-components-actor__meta relative')]/span/span/span[2]")).getText();
        return new Post(by, content, matchedKeywords, link);
    }


    @Override
    public void startScraper(PostScraperParameters postsParameters) {
        try {

            ScraperConfig config = new ScraperConfig();
            WebDriver driver = config.setupWebDriver(postsParameters.getHeadlessMode());
            this.driverHelper = new WebDriverHelper(driver);

            this.isScraperRunning = true;
            this.currentStatus = "Scraper started, logging into linkedin account.";

            boolean isLoggedIn = driverHelper.loginToLinkedIn(postsParameters.getEmail(), postsParameters.getPassword(), postsParameters.getHeadlessMode());

            if(!isLoggedIn) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Wrong LinkedIn login credentials.");
            }

            this.currentStatus = "Logged in to linkedin successfully...";
            Set<Post> posts = fetchPosts(postsParameters);

            // save fetched posts to database
            saveToDatabase(posts, postsParameters.getTitle());
            this.scrapedSuccess = true;
            this.currentStatus = "Successfully saved fetched posts into database. Shutting down scraper";
            this.isScraperRunning = false;


        } catch (Exception e) {
            e.printStackTrace();
            driverHelper.driver().quit();
            setDefaultStatus();
        } finally {
            driverHelper.driver().quit();
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
    public boolean isScraperIsCurrentlyRunning() {
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

    // below all methods communicate to database.

    @Transactional
    private void saveToDatabase (Set<Post> posts, String searchTitle) {
        try {
            Search search = new Search();
            search.setSearchedAt(LocalDateTime.now());
            search.setTitle(searchTitle);
            search.setId(new ObjectId().toString());

            Set<Post> updatedPosts = posts.stream().peek((post) -> post.setSearch(search)).collect(Collectors.toSet());
            postRepository.saveAll(updatedPosts);

            search.setPosts(updatedPosts.stream().toList());
            searchService.saveSearch(search);

        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Fetch posts successfully but unable to save it to the database");
        }
    }
}
