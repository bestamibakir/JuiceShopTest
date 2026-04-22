package com.bestamibakir.data;

import com.bestamibakir.User;
import net.datafaker.Faker;

public class UserDataBuilder {
    public static User generateRandomUser() {
        Faker faker = new Faker();
        // JuiceShop şifre politikasına uyması için kompleks bir şifre üretiyoruz
        String password = faker.internet().password(8, 15, true, true, true) + "1aA!";
        
        return User.builder()
                .email(faker.internet().emailAddress())
                .password(password)
                .securityAnswer(faker.animal().name())
                .build();
    }
}
