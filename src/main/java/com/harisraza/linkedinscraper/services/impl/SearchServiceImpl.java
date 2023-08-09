package com.harisraza.linkedinscraper.services.impl;

import com.harisraza.linkedinscraper.models.Post;
import com.harisraza.linkedinscraper.models.Profile;
import com.harisraza.linkedinscraper.models.Search;
import com.harisraza.linkedinscraper.repositories.SearchRepository;
import com.harisraza.linkedinscraper.services.SearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class SearchServiceImpl implements SearchService {

    @Autowired private SearchRepository searchRepository;

    @Override
    public List<Search> getSearches() {
        return searchRepository.findAll();
    }

    @Override
    public List<Profile> getProfilesBySearch(String id) {
        try {
            return searchRepository.findById(id).get().getProfiles();
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Unable to get profiles by search id: " + id);
        }
    }

    @Override
    public List<Post> getPostsBySearch(String id) {
        try {
            return searchRepository.findById(id).get().getPosts();
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Unable to get posts by search id: " + id);
        }
    }

    @Override
    public List<Search> getProfileSearches() {
        try {
            return searchRepository.findAll().stream().filter((search) -> search.getProfiles() != null).collect(Collectors.toList());
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Unable to get profile searches");
        }
    }

    @Override
    public List<Search> getPostsSearches() {
        try {
            return searchRepository.findAll().stream().filter((search) -> search.getPosts() != null).collect(Collectors.toList());
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Unable to get posts searches");
        }
    }

    @Override
    public List<Profile> getProfilesBySearchedAt(LocalDateTime searchedAt) {
        try {
            return searchRepository.findBySearchedAt(searchedAt).getProfiles();
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Unable to get profiles by search.");
        }
    }

    @Override
    public List<Post> getPostsBySearchedAt(LocalDateTime searchedAt) {
        try {
            return searchRepository.findBySearchedAt(searchedAt).getPosts();
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Unable to get posts by search");
        }
    }

    @Override
    public Search saveSearch(Search search) {
        try {
            return searchRepository.save(search);
        } catch (Exception e) {

            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Unable to save search to database");
        }
    }

    @Override
    public Search updateSearch(Search search) {
        try {
             return searchRepository.save(search);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Unable to update search to database");
        }
    }

}
