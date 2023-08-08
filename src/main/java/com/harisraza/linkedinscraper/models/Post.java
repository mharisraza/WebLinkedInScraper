package com.harisraza.linkedinscraper.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "posts")
@NoArgsConstructor
@Getter
@Setter
public class Post {
    @Id
    private String id;
    private String by;
    private String content;
    private String matchedKeywords;
    private String link;

    @JsonIgnoreProperties({"posts"})
    private Search search;

    public Post(String by, String content, String matchedKeywords, String link) {
        this.by = by;
        this.content = content;
        this.matchedKeywords = matchedKeywords;
        this.link = link;
    }

}
