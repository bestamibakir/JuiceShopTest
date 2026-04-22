package com.bestamibakir;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.time.Duration;

public class BaseTest {

    // ThreadLocal ile her thread (test koşumu) için izole bir WebDriver instance'ı oluşturuyoruz.
    // Bu sayede paralel test koşumlarında driver'ların birbirine karışmasını (race condition) önlüyoruz.
    protected static ThreadLocal<WebDriver> driver = new ThreadLocal<>();

    @BeforeMethod
    public void setup() throws MalformedURLException {
        ChromeOptions options = new ChromeOptions();
        
        // Selenium Grid'in Docker network üzerindeki adresi. 
        // "selenium-hub" yerine docker-compose dosyanızdaki grid servisinin adını kullanmalısınız.
        // Aynı ağda oldukları için container isimleriyle (hostname) haberleşebilirler.
        URL gridUrl = URI.create("http://selenium-hub:4444/wd/hub").toURL();
        
        // RemoteWebDriver oluşturup ThreadLocal içine set ediyoruz.
        driver.set(new RemoteWebDriver(gridUrl, options));
        
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
