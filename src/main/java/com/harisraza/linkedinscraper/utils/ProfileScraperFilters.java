package com.harisraza.linkedinscraper.utils;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class ProfileScraperFilters {

    private Boolean isFirstConnectionChecked;
    private Boolean isSecondConnectionChecked;
    private Boolean isThirdConnectionChecked;

    private List<String> selectedLocations;
    private List<String> selectedIndustries;
}
