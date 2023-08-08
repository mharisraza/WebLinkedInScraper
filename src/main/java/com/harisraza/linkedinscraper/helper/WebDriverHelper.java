package com.harisraza.linkedinscraper.helper;

import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.util.List;

public class WebDriverHelper {

    private WebDriver driver;

    public WebDriverHelper(WebDriver driver) {
        this.driver = driver;
    }

    private boolean isElementPresent(By locator) {
        return driver.findElements(locator).size() > 0;
    }

    public WebElement getElementIfExist(By locator) {
        if (isElementPresent(locator)) return driver.findElement(locator);
        return null;
    }

    public List<WebElement> getElementsIfExists(By locator) {
        if(isElementPresent(locator)) return driver.findElements(locator);
        return null;
    }

    public WebElement getChildElementIfExist(WebElement parentElement, By childLocator) {
        try {
            return parentElement.findElement(childLocator);
        } catch (NoSuchElementException e) {
            return null;
        }
    }

    public WebDriver getDriver() {
        return driver;
    }

}
