package com.bestamibakir.pages;

import com.bestamibakir.User;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;

public class RegistrationPage extends BasePage {

    public RegistrationPage(WebDriver driver) {
        super(driver);
    }

    public void registerNewUser(User user) {
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("emailControl"))).sendKeys(user.getEmail());
        driver.findElement(By.id("passwordControl")).sendKeys(user.getPassword());
        driver.findElement(By.id("repeatPasswordControl")).sendKeys(user.getPassword());

        WebElement securityQuestionDropdown = wait.until(ExpectedConditions.presenceOfElementLocated(By.name("securityQuestion")));
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", securityQuestionDropdown);
        wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//mat-option[contains(., 'favorite pet')]"))).click();
        driver.findElement(By.id("securityAnswerControl")).sendKeys(user.getSecurityAnswer());

        wait.until(ExpectedConditions.elementToBeClickable(By.id("registerButton"))).click();
    }
}
