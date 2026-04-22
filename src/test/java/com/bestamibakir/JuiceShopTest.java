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

    private User generateRandomUser() {
        net.datafaker.Faker faker = new net.datafaker.Faker();
        // JuiceShop şifre politikasına uyması için kompleks bir şifre üretiyoruz
        String password = faker.internet().password(8, 15, true, true, true) + "1aA!"; 
        
        return User.builder()
                .email(faker.internet().emailAddress())
                .password(password)
                .securityAnswer(faker.animal().name())
                .build();
    }

    @Test
    public void testUserRegistrationAndLogin() {
        getDriver().get("http://juice-shop:3000");
        WebDriverWait wait = new WebDriverWait(getDriver(), Duration.ofSeconds(10));

        // Pop-up'ları kapat
        wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//button//span[text()='Dismiss']"))).click();
        wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Me want it!"))).click();

        // 1. DataFaker ve Builder (Lombok) kullanarak rastgele test verisi üret (Gerçek zamanlı data generation)
        User testUser = generateRandomUser();
        System.out.println("Oluşturulan Test Kullanıcısı: " + testUser.getEmail());

        // 2. Kayıt Sayfasına Git
        wait.until(ExpectedConditions.elementToBeClickable(By.id("navbarAccount"))).click();
        wait.until(ExpectedConditions.elementToBeClickable(By.id("navbarLoginButton"))).click();
        wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//a[@href='#/register']"))).click();

        // 3. Kayıt Formunu Doldur
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("emailControl"))).sendKeys(testUser.getEmail());
        getDriver().findElement(By.id("passwordControl")).sendKeys(testUser.getPassword());
        getDriver().findElement(By.id("repeatPasswordControl")).sendKeys(testUser.getPassword());

        // Güvenlik Sorusu Seçimi
        getDriver().findElement(By.name("securityQuestion")).click();
        wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//mat-option//span[contains(text(), 'favorite pet')]"))).click();
        getDriver().findElement(By.id("securityAnswerControl")).sendKeys(testUser.getSecurityAnswer());

        // Kayıt Ol Butonuna Tıkla
        wait.until(ExpectedConditions.elementToBeClickable(By.id("registerButton"))).click();

        // 4. Başarılı Kayıt Mesajını Doğrula ve Login Ol
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//span[contains(text(), 'Registration completed successfully')]")));
        
        // Login ekranına yönlendirildikten sonra giriş yap
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("email"))).sendKeys(testUser.getEmail());
        getDriver().findElement(By.id("password")).sendKeys(testUser.getPassword());
        wait.until(ExpectedConditions.elementToBeClickable(By.id("loginButton"))).click();

        // 5. Başarılı Girişi Doğrula (Sepet ikonunun görünür olması)
        WebElement basketIcon = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//button[@aria-label='Show the shopping cart']")));
        Assert.assertTrue(basketIcon.isDisplayed(), "Kullanıcı girişi başarısız oldu!");
    }
}
