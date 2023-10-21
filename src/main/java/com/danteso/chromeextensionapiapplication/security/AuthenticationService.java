package com.danteso.chromeextensionapiapplication.security;

import com.danteso.chromeextensionapiapplication.security.controller.RegistrationController;
import com.danteso.chromeextensionapiapplication.security.entity.User;
import com.danteso.chromeextensionapiapplication.security.repo.JwtService;
import com.danteso.chromeextensionapiapplication.security.repo.UserRepository;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;


@Service
@RequiredArgsConstructor
public class AuthenticationService {

    private final Logger LOG = LoggerFactory.getLogger(AuthenticationService.class);

    private final UserRepository userRepo;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;


    public AuthenticationResponse registerUser(User user){
        AuthenticationResponse register = register(new RegisterRequest(user.getUsername(), "", "", user.getPassword()));
        return register;
    }

    public AuthenticationResponse register(RegisterRequest request){
//        User user = userRepo.save(registrationForm.toUser(passwordEncoder));
        User user = User.builder()
                .username(request.getFirstname())
                .password(passwordEncoder.encode(request.getPassword()))
                .build();
        userRepo.save(user);
        String jwtToken = jwtService.generateToken(user);
        return AuthenticationResponse.builder().token(jwtToken).build();
    }

    public AuthenticationResponse authenticate(AuthenticationRequest request, HttpServletResponse response){
        LOG.debug("Auth for {}", request.getUsername());
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword()));
        LOG.debug("Authenticated user");
        User userFromRepo = userRepo.findByUsername(request.getUsername());
        LOG.debug("Obtained user from repo");
        String jwtToken = jwtService.generateToken(userFromRepo);
        LOG.debug("token = {}", jwtToken);
        return AuthenticationResponse.builder().token(jwtToken).build();
    }

}
