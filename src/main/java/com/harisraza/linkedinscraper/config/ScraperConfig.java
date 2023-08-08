package com.harisraza.linkedinscraper.config;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.PageLoadStrategy;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

public class ScraperConfig {

    public WebDriver setupWebDriver(boolean headlessMode) {

        WebDriverManager.chromedriver().setup();
        ChromeOptions options = new ChromeOptions();
        options.setPageLoadStrategy(PageLoadStrategy.EAGER); // implies that the WebDriver will wait for the entire page to load before moving on to the next step in the code.

        if(headlessMode) options.addArguments("--headless=new");

        WebDriver webDriver = new ChromeDriver(options);
        return webDriver;
    }
}
