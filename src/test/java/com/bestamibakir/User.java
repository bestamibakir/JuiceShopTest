package com.bestamibakir;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class User {
    private String email;
    private String password;
    private String securityQuestion;
    private String securityAnswer;
}
