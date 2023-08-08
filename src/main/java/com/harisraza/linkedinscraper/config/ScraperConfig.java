package com.harisraza.linkedinscraper.config;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

public class ScraperConfig {

    public WebDriver setupWebDriver(boolean headlessMode) {

        WebDriverManager.chromedriver().setup();
        WebDriver webDriver = new ChromeDriver();

        if(headlessMode) {
            ChromeOptions options = new ChromeOptions();
            options.addArguments("--headless");
            webDriver = new ChromeDriver(options);
        }

        return webDriver;
    }
}
