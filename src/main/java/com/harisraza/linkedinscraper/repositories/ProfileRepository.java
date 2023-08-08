package com.harisraza.linkedinscraper.repositories;

import com.harisraza.linkedinscraper.models.Profile;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface ProfileRepository extends MongoRepository<Profile, String> {}
