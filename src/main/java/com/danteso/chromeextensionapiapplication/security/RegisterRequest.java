package com.danteso.chromeextensionapiapplication.security;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class RegisterRequest {

    private String firstname;
    private String lastname;
    private String email;
    private String password;
}
