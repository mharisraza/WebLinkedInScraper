package com.harisraza.linkedinscraper.utils;

import com.harisraza.linkedinscraper.exceptions.ExpectedPageNotLoadedException;
import com.harisraza.linkedinscraper.exceptions.HumanVerificationRequiredException;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Wait;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.List;

public record WebDriverHelper(WebDriver driver) {

    private boolean isElementPresent(By locator) {
        return driver.findElements(locator).size() > 0;
    }

    public WebElement getElementIfExist(By locator) {
        if (isElementPresent(locator)) return driver.findElement(locator);
        return null;
    }

    public List<WebElement> getElementsIfExists(By locator) {
        if (isElementPresent(locator)) return driver.findElements(locator);
        return null;
    }

    public WebElement getRelatedElementIfExist(WebElement element, By relatedLocator) {
        try {
            return element.findElement(relatedLocator);
        } catch (NoSuchElementException e) {
            return null;
        }
    }

    public boolean loginToLinkedIn(String emailAddress, String password, boolean isHeadlessMode)  {
        // going to LinkedIn login page.
        driver.get("https://www.linkedin.com/login");

        // entering details and then clicking on login button to logged in.
        driver.findElement(By.xpath("/html/body/div/main/div[2]/div[1]/form/div[1]/input")).sendKeys(emailAddress);
        driver.findElement(By.xpath("/html/body/div/main/div[2]/div[1]/form/div[2]/input")).sendKeys(password);
        driver.findElement(By.xpath("/html/body/div/main/div[2]/div[1]/form/div[3]/button")).click();

        if (driver.getCurrentUrl().contains("checkpoint") && isHeadlessMode) {
            throw new HumanVerificationRequiredException("Manual human verification required, please run scraper without headless mode and verify the captcha to continue.");
        } else if (driver.getCurrentUrl().contains("checkpoint")) {
            try {
                waitUntilExpectedPageLoaded("feed", null);
            } catch (ExpectedPageNotLoadedException ex) {
                driver.quit();
            }
        }

        WebElement wrongCredentialsElement = getElementIfExist(By.id("error-for-password"));
        return wrongCredentialsElement == null;
    }

    public void waitUntilExpectedPageLoaded(String expectedUrl, By elementLocator) {
        Wait<WebDriver> wait = new WebDriverWait(driver, Duration.ofSeconds(30));
        try {
            wait.until(ExpectedConditions.urlContains(expectedUrl));
            if (elementLocator != null) {
                wait.until(ExpectedConditions.presenceOfElementLocated(elementLocator));
            }
        } catch (Exception e) {
            throw new ExpectedPageNotLoadedException(String.format("Expected page that should contain '%s' not loaded in 30 seconds", expectedUrl));
        }
    }

    public void goToPage(String url) {
        if (url.isBlank()) throw new RuntimeException("Can't go to the page, provided url is empty or null");
        driver.get(url);
    }

    public String getCurrentPageUrl() {
        return driver.getCurrentUrl();
    }

    public void shutDownScraper() {
        driver.quit();
    }

}