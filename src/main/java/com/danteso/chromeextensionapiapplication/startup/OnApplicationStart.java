package com.danteso.chromeextensionapiapplication.startup;

import com.danteso.chromeextensionapiapplication.entity.Description;
import com.danteso.chromeextensionapiapplication.entity.Score;
import com.danteso.chromeextensionapiapplication.entity.Term;
import com.danteso.chromeextensionapiapplication.repo.TermRepository;
import com.danteso.chromeextensionapiapplication.security.AuthenticationService;
import com.danteso.chromeextensionapiapplication.security.entity.User;
import com.danteso.chromeextensionapiapplication.security.repo.UserRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
@RequiredArgsConstructor
public class OnApplicationStart {
    private final Logger LOG = LoggerFactory.getLogger(OnApplicationStart.class);

    private final TermRepository termRepository;
    private final UserRepository userRepository;
    private final AuthenticationService authenticationService;


    @EventListener(ApplicationReadyEvent.class)
    private void onStart(){
        this.createDefaultUser();
        this.createDefaultTerms();
    }


    //TODO add user/password to env variables
    private void createDefaultUser(){
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


    private void createDefaultTerms() {
        User rootUser = userRepository.findByUsername("root");
        Term term = Term.builder().name("field").descriptions(Set.of(
                        new Description("an area of activity or interest:")
                        , new Description("a subject or area that you work in or study:")
                        , new Description("A field can also be a large area covered by something or having something under its surface:")
                        , new Description("to be something you do not know much about:")
                        , new Description("an area of land containing a particular natural substance")
                        , new Description("to answer questions, esp. difficult or unexpected ones:")
                        , new Description("to have or produce a team of people to take part in an activity or event:")
                        , new Description("an area of land with grass or crops growing on it:"))
                )
                .user(rootUser)
                .score(new Score()).build();
        termRepository.save(term);

        term = Term.builder().name("class").descriptions(Set.of(
                        new Description("a group into which goods, services, or people are put according to their standard:")
                        , new Description("a group of people within a society who have the same economic and social position:")
                        , new Description("a group of students who successfully finished their studies in a particular year:")
                        , new Description("the quality of being stylish or fashionable:")
                        , new Description("a group into which goods and services are put based on their characteristics:")
                        , new Description("to be something of such a high quality that nothing can be compared to it:"))
                )
                .user(rootUser)
                .score(new Score()).build();
        termRepository.save(term);
    }
}
