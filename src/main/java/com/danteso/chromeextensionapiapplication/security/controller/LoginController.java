package com.danteso.chromeextensionapiapplication.security.controller;

import com.danteso.chromeextensionapiapplication.security.AuthenticationRequest;
import com.danteso.chromeextensionapiapplication.security.AuthenticationResponse;
import com.danteso.chromeextensionapiapplication.security.AuthenticationService;
import com.danteso.chromeextensionapiapplication.security.RegistrationForm;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/api/login")
@RequiredArgsConstructor
public class LoginController {
    private final Logger LOG = LoggerFactory.getLogger(LoginController.class);

    private final AuthenticationService authenticationService;


    @GetMapping
    public String loginPage(){
        return "login";
    }

    @PostMapping
    public String afterLoginRedirect(RegistrationForm registrationForm, HttpServletResponse response){
        LOG.debug("Trying to login user {}", registrationForm.getUsername());
        AuthenticationResponse authenticate = authenticationService.authenticate(new AuthenticationRequest(registrationForm.getUsername(), registrationForm.getPassword()), response);
        LOG.debug("Adding token {}", authenticate.getToken());
        response.addCookie(new Cookie("token", authenticate.getToken()));
        return "redirect:/api/showAll";
    }

    @PostMapping("/request")
    public String loginWithRequest(@RequestBody AuthenticationRequest authenticationRequest, HttpServletResponse response){
        LOG.debug("authRequest = {}", authenticationRequest);
        AuthenticationResponse authenticate = authenticationService.authenticate(authenticationRequest, response);
        LOG.debug("Adding token {}", authenticate.getToken());
        response.addCookie(new Cookie("token", authenticate.getToken()));
        response.addHeader("Authorization", authenticate.getToken());
        LOG.debug("response.headerNames = {}", response.getHeaderNames());
        return null;
    }
}
