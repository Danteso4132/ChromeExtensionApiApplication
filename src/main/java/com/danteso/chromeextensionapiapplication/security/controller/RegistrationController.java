package com.danteso.chromeextensionapiapplication.security.controller;

import com.danteso.chromeextensionapiapplication.security.RegistrationForm;
import com.danteso.chromeextensionapiapplication.security.repo.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/api/register")
public class RegistrationController {

    private UserRepository userRepo;
    private PasswordEncoder passwordEncoder;

    public RegistrationController(UserRepository userRepo, PasswordEncoder passwordEncoder){
        this.userRepo = userRepo;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping
    public String registerForm(){
        return "registration";
    }

    @PostMapping
    public String processRegistration(RegistrationForm registrationForm){
        userRepo.save(registrationForm.toUser(passwordEncoder));
        return "redirect:/api/login";
    }
}
