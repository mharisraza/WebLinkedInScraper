package com.harisraza.linkedinscraper.utils;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class StatusResponse {
    private String status;
    private boolean isScraperRunning;
    private boolean isScrapedSuccess;
}
