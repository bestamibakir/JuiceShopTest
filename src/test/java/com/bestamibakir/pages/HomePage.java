package com.bestamibakir.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;

public class HomePage extends BasePage {

    public HomePage(WebDriver driver) {
        super(driver);
    }

    public void openHomePage() {
        // Use docker-compose service name "juice-shop"
        driver.get("http://juice-shop:3000");
    }

    public void dismissPopups() {
        wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//button//span[text()='Dismiss']"))).click();
        wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Me want it!"))).click();
    }

    public String getPageTitle() {
        return driver.getTitle();
    }

    public LoginPage openLoginPage() {
        wait.until(ExpectedConditions.elementToBeClickable(By.id("navbarAccount"))).click();
        wait.until(ExpectedConditions.elementToBeClickable(By.id("navbarLoginButton"))).click();
        return new LoginPage(driver);
    }

    public boolean isSearchIconDisplayed() {
        return wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("searchQuery"))).isDisplayed();
    }

    public boolean isBasketIconDisplayed() {
        return wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//button[@aria-label='Show the shopping cart']"))).isDisplayed();
    }
}
