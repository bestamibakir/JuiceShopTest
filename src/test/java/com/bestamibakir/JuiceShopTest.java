package com.bestamibakir;

import com.bestamibakir.data.UserDataBuilder;
import com.bestamibakir.pages.HomePage;
import com.bestamibakir.pages.LoginPage;
import com.bestamibakir.pages.RegistrationPage;
import org.testng.Assert;
import org.testng.annotations.Test;

public class JuiceShopTest extends BaseTest {

    @Test
    public void testJuiceShopHomePage() {
        HomePage homePage = new HomePage(getDriver());
        homePage.openHomePage();
        homePage.dismissPopups();

        Assert.assertEquals(homePage.getPageTitle(), "OWASP Juice Shop", "Sayfa başlığı eşleşmedi!");
        Assert.assertTrue(homePage.isSearchIconDisplayed(), "Arama butonu ekranda görüntülenemedi!");
    }

    @Test
    public void testUserRegistrationAndLogin() {
        HomePage homePage = new HomePage(getDriver());
        homePage.openHomePage();
        homePage.dismissPopups();

        // 1. DataFaker ve Builder kullanarak rastgele test verisi üret
        User testUser = UserDataBuilder.generateRandomUser();
        System.out.println("Oluşturulan Test Kullanıcısı: " + testUser.getEmail());

        // 2. Kayıt Sayfasına Git
        LoginPage loginPage = homePage.openLoginPage();
        RegistrationPage registrationPage = loginPage.openRegistrationPage();

        // 3. Kayıt Formunu Doldur ve Gönder
        registrationPage.registerNewUser(testUser);

        // 4. Kayıt sonrası otomatik yönlendirilen Login ekranından giriş yap
        loginPage.login(testUser.getEmail(), testUser.getPassword());

        // 5. Başarılı Girişi Doğrula
        Assert.assertTrue(homePage.isBasketIconDisplayed(), "Kullanıcı girişi başarısız oldu!");
    }
}
