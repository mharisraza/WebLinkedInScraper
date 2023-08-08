package com.harisraza.linkedinscraper.controllers;

import com.harisraza.linkedinscraper.helper.StatusResponse;
import com.harisraza.linkedinscraper.models.Post;
import com.harisraza.linkedinscraper.models.Search;
import com.harisraza.linkedinscraper.services.PostService;
import com.harisraza.linkedinscraper.services.ProfileService;
import com.harisraza.linkedinscraper.helper.PostScraperParameters;
import com.harisraza.linkedinscraper.helper.ProfileScraperParameters;
import com.harisraza.linkedinscraper.models.Profile;
import com.harisraza.linkedinscraper.services.SearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
public class AppController {

    @Autowired private PostService postService;
    @Autowired private ProfileService profileService;
    @Autowired private SearchService searchService;

    private final String PROFILE_SCRAPER = "profile-scraper";
    private final String POST_SCRAPER = "post-scraper";

    @PostMapping(value = "/profile-scraper", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> profileScraper(@RequestBody ProfileScraperParameters profileParameters) {
        System.out.println("First connection: " + profileParameters.getFilters().getIsFirstConnectionChecked());
        profileService.startScraper(profileParameters);
        return ResponseEntity.ok("Successfully fetched and save profiles to database.");
    }

    @GetMapping(value = "/profiles-searches", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getProfileSearches() {
        List<Search> searches = searchService.getProfileSearches();
        return ResponseEntity.ok(searches);
    }

    @GetMapping(value = "/posts-searches", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getPostsSearches() {
        List<Search> searches = searchService.getPostsSearches();
        return ResponseEntity.ok(searches);
    }

    @GetMapping(value = "/view-profiles/{searchId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getProfilesBySearch(@PathVariable("searchId") String id) {
       List<Profile> profiles = searchService.getProfilesBySearch(id);
       return ResponseEntity.ok(profiles);
    }

    @GetMapping(value = "/view-posts/{searchId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getPostsBySearch(@PathVariable("searchId") String id) {
        List<Post> posts = searchService.getPostsBySearch(id);
        return ResponseEntity.ok(posts);
    }

    @PostMapping(value = "/post-scraper", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> postScraper(@RequestBody PostScraperParameters postParameters) {
        postService.startScraper(postParameters);
        return ResponseEntity.ok("Successfully fetched and save posts to database");
    }

    @GetMapping("/status/{scraper-type}")
    public ResponseEntity<?> getStatus(@PathVariable("scraper-type") String scraperType) throws InterruptedException {
        StatusResponse statusResponse = new StatusResponse();
        if(scraperType.equals(PROFILE_SCRAPER)) {
            statusResponse.setStatus(profileService.getStatus());
            statusResponse.setScraperRunning(profileService.isScraperCurrentlyRunning());
            statusResponse.setScrapedSuccess(profileService.isScrapedSuccess());
        } else {
            statusResponse.setStatus(postService.getStatus());
            statusResponse.setScraperRunning(postService.isScraperIsCurrentlyRunning());
            statusResponse.setScrapedSuccess(postService.isScrapedSuccess());
        }
        return ResponseEntity.ok(statusResponse);
    }


}
