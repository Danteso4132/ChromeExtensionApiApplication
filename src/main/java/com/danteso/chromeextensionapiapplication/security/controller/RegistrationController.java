package com.danteso.chromeextensionapiapplication.security.controller;

import com.danteso.chromeextensionapiapplication.security.AuthenticationService;
import com.danteso.chromeextensionapiapplication.security.RegisterRequest;
import com.danteso.chromeextensionapiapplication.security.RegistrationForm;
import com.danteso.chromeextensionapiapplication.security.repo.UserRepository;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.net.http.HttpRequest;
import java.util.Map;


@Controller
@RequestMapping("/api/register")
@RequiredArgsConstructor
public class RegistrationController {

    private final Logger LOG = LoggerFactory.getLogger(RegistrationController.class);

    private final AuthenticationService authenticationService;


    @GetMapping
    public String registerForm(){
        LOG.debug("Navigating to register");
        return "registration";
    }

    @PostMapping
    public String processRegistration(RegistrationForm registrationForm){
        authenticationService.register(new RegisterRequest(registrationForm.getUsername(), "", "", registrationForm.getPassword()));
        LOG.debug("Registered user {}", registrationForm.getUsername());
        return "redirect:/api/login";
    }
}
