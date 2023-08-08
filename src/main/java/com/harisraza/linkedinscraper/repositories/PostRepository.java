package com.harisraza.linkedinscraper.repositories;

import com.harisraza.linkedinscraper.models.Post;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface PostRepository extends MongoRepository<Post, String> {
}
