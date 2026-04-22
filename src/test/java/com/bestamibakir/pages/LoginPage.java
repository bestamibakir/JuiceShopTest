package com.bestamibakir.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;

public class LoginPage extends BasePage {

    public LoginPage(WebDriver driver) {
        super(driver);
    }

    public RegistrationPage openRegistrationPage() {
        wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//a[@href='#/register']"))).click();
        return new RegistrationPage(driver);
    }

    public void login(String email, String password) {
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("email"))).sendKeys(email);
        driver.findElement(By.id("password")).sendKeys(password);
        wait.until(ExpectedConditions.elementToBeClickable(By.id("loginButton"))).click();
    }
}
