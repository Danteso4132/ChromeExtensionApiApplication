package com.danteso.chromeextensionapiapplication.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.csrf.CsrfFilter;
import org.springframework.security.web.csrf.CsrfTokenRepository;
import org.springframework.security.web.csrf.HttpSessionCsrfTokenRepository;

@Configuration
public class SecurityConfig {
    @Bean
    public PasswordEncoder passwordEncoder(){
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
            return http
                    .authorizeRequests()
                    .requestMatchers(
                            "/api/showAll*", "/api/showScores*",
                            "/api/random*", "/api/saveForTermName*",
                            "/api/save*", "/api/showDescription*",
                            "/api/verifyAnswer*").hasRole("USER")
                    .anyRequest().permitAll()

                    .and()
                    .formLogin()
                    .loginPage("/api/login")

                    .and()
                    .logout()
                    .logoutSuccessUrl("/")

                    // Make H2-Console non-secured; for debug purposes
                    //.and()
                    //.csrf()
//                    .ignoringRequestMatchers("/h2-console/**")

                    // Allow pages to be loaded in frames from the same origin; needed for H2-Console
                    .and()
                    .headers()
                    .frameOptions()
                    .sameOrigin()

                    .and()
                    .build();
        }

    @Bean
    public CsrfTokenRepository csrfTokenRepository(){
        HttpSessionCsrfTokenRepository repository = new HttpSessionCsrfTokenRepository();

        // This is the second part you were missing
        repository.setHeaderName("X-XSRF-TOKEN");
        return repository;
    }

}
