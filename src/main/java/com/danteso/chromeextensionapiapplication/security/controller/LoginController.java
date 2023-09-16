//package com.danteso.chromeextensionapiapplication.security.controller;
//
//import com.danteso.chromeextensionapiapplication.security.RegistrationForm;
//import com.danteso.chromeextensionapiapplication.security.entity.User;
//import com.danteso.chromeextensionapiapplication.security.repo.UserRepository;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.security.authentication.AuthenticationManager;
//import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
//import org.springframework.security.core.Authentication;
//import org.springframework.security.core.context.SecurityContextHolder;
//import org.springframework.stereotype.Controller;
//import org.springframework.web.bind.annotation.*;
//
//@Controller
//@RequestMapping("/api/login")
//public class LoginController {
//
//    @Autowired
//    private AuthenticationManager authenticationManager;
//
//    @Autowired
//    private UserRepository userRepo;
//
//
//    @GetMapping
//    @ResponseBody
//    public String loginPage(){
//        return "login";
//    }
//
//    @PostMapping
//    @ResponseBody
//    public String afterLoginRedirect(RegistrationForm registrationForm){
//        User user = userRepo.findByUsername(registrationForm.getUsername());
//        System.out.println("User: " + user);
//        if (user == null){
//            return "redirect:/api/login";
//        }
//        UsernamePasswordAuthenticationToken loginToken = new UsernamePasswordAuthenticationToken(registrationForm.getUsername(), registrationForm.getPassword());
//        Authentication authenticate = authenticationManager.authenticate(loginToken);
//        SecurityContextHolder.getContext().setAuthentication(authenticate);
//        return "redirect:/api/showAll";
//    }
//}
