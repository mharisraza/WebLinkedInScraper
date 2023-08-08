package com.harisraza.linkedinscraper.repositories;

import com.harisraza.linkedinscraper.models.Search;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.time.LocalDateTime;

public interface SearchRepository extends MongoRepository<Search, String> {
    Search findBySearchedAt(LocalDateTime searchedAt);
}
