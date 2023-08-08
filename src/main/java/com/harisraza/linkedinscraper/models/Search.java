package com.harisraza.linkedinscraper.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.mongodb.lang.Nullable;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@Document(collection = "searches")
@NoArgsConstructor
public class Search {

    @Id
    private String id;
    private LocalDateTime searchedAt;
    private String title;

    @DBRef
    @Nullable
    @JsonIgnoreProperties({"search"})
    private List<Profile> profiles;

    @DBRef
    @Nullable
    @JsonIgnoreProperties({"search"})
    private List<Post> posts;

}
