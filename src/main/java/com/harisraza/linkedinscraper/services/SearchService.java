package com.harisraza.linkedinscraper.services;

import com.harisraza.linkedinscraper.models.Post;
import com.harisraza.linkedinscraper.models.Profile;
import com.harisraza.linkedinscraper.models.Search;

import java.time.LocalDateTime;
import java.util.List;

public interface SearchService {
    List<Search> getSearches();

    List<Profile> getProfilesBySearch(String id);
    List<Post> getPostsBySearch(String id);
    List<Search> getProfileSearches();
    List<Search> getPostsSearches();
    List<Profile> getProfilesBySearchedAt(LocalDateTime searchedAt);
    List<Post> getPostsBySearchedAt(LocalDateTime searchedAt);

    Search saveSearch(Search search);
    Search updateSearch(Search search);

}
