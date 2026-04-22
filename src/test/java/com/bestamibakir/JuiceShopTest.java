package com.bestamibakir;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.time.Duration;

public class JuiceShopTest extends BaseTest {

    @Test
    public void testJuiceShopHomePage() {
        // 1. Docker network içindeki Juice Shop uygulamasına git
        // "juice-shop" yerine docker-compose dosyanızdaki uygulamanın servis adını yazmalısınız.
        // Jenkins, Selenium Grid ve Juice Shop aynı Docker ağında olduğunda bu şekilde doğrudan adıyla erişilebilir.
        getDriver().get("http://juice-shop:3000");

        WebDriverWait wait = new WebDriverWait(getDriver(), Duration.ofSeconds(10));

        // 2. Sayfa başlığını doğrula
        String expectedTitle = "OWASP Juice Shop";
        String actualTitle = getDriver().getTitle();
        Assert.assertEquals(actualTitle, expectedTitle, "Sayfa başlığı eşleşmedi!");

        // 3. Ekrana çıkan ilk "Welcome" (Hoşgeldin) Pop-up'ını kapat
        // Mat-dialog elemanının altındaki Dismiss butonunu bulur
        WebElement dismissButton = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//button//span[text()='Dismiss']")));
        dismissButton.click();

        // 4. Çerez (Cookie) onay penceresindeki "Me want it!" butonunu tıkla
        WebElement cookieButton = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Me want it!")));
        cookieButton.click();

        // 5. Sayfanın yüklendiğini ve pop-up'ların engellemediğini teyit etmek için arama ikonunun görünürlüğünü kontrol et
        WebElement searchIcon = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("searchQuery")));
        Assert.assertTrue(searchIcon.isDisplayed(), "Arama butonu ekranda görüntülenemedi!");
    }
}
