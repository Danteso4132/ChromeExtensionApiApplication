package com.danteso.chromeextensionapiapplication.security;

import com.danteso.chromeextensionapiapplication.security.entity.User;
import org.springframework.security.crypto.password.PasswordEncoder;
import lombok.Data;

@Data
public class RegistrationForm {

    private String username;
    private String password;

    public User toUser(PasswordEncoder passwordEncoder) {
        return new User(
                username, passwordEncoder.encode(password));
    }

}
