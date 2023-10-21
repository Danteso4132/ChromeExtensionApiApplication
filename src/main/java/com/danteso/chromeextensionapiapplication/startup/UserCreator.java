package com.danteso.chromeextensionapiapplication.startup;

import com.danteso.chromeextensionapiapplication.security.AuthenticationService;
import com.danteso.chromeextensionapiapplication.security.entity.User;
import com.danteso.chromeextensionapiapplication.security.repo.UserRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserCreator {
    private final Logger LOG = LoggerFactory.getLogger(UserCreator.class);

    private final UserRepository userRepository;
    private final AuthenticationService authenticationService;

    //TODO add user/password to env variables
    @EventListener(ApplicationReadyEvent.class)
    public void createDefaultUser(){
        LOG.debug("Creating default user");
        if (userRepository.findByUsername("root") == null){
            User user = User.builder().username("root").password("root").build();
            authenticationService.registerUser(user);
            LOG.debug("Created default user");
        }
        else{
            LOG.debug("User already exists");
        }
    }
}
