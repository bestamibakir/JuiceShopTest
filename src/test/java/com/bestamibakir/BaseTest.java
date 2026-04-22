package com.bestamibakir;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Optional;
import org.testng.annotations.Parameters;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.time.Duration;

public class BaseTest {

    // ThreadLocal ile her thread (test koşumu) için izole bir WebDriver instance'ı oluşturuyoruz.
    // Bu sayede paralel test koşumlarında driver'ların birbirine karışmasını (race condition) önlüyoruz.
    protected static ThreadLocal<WebDriver> driver = new ThreadLocal<>();

    @Parameters("browser")
    @BeforeMethod
    public void setup(@Optional("chrome") String browser) throws MalformedURLException {
        URL gridUrl = URI.create("http://selenium-hub:4444/wd/hub").toURL();
        
        if (browser.equalsIgnoreCase("chrome")) {
            ChromeOptions chromeOptions = new ChromeOptions();
            chromeOptions.setCapability("se:name", "Test on Grid - Chrome");
            driver.set(new RemoteWebDriver(gridUrl, chromeOptions));
        } else if (browser.equalsIgnoreCase("firefox")) {
            FirefoxOptions firefoxOptions = new FirefoxOptions();
            firefoxOptions.setCapability("se:name", "Test on Grid - Firefox");
            driver.set(new RemoteWebDriver(gridUrl, firefoxOptions));
        } else {
            throw new IllegalArgumentException("Browser configuration is not defined for: " + browser);
        }
        
        getDriver().manage().window().maximize();
        getDriver().manage().timeouts().implicitlyWait(Duration.ofSeconds(10));
    }

    // ThreadLocal içindeki WebDriver instance'ını almak için yardımcı metod
    public WebDriver getDriver() {
        return driver.get();
    }

    @AfterMethod
    public void tearDown() {
        if (getDriver() != null) {
            getDriver().quit(); // Browser oturumunu kapatır
            driver.remove();    // Test bitince ThreadLocal'ı temizlemek memory leak'leri önler
        }
    }
}
