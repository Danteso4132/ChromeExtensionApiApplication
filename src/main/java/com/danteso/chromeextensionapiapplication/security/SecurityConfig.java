package com.danteso.chromeextensionapiapplication.security;

import com.danteso.chromeextensionapiapplication.security.repo.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationConverter;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.csrf.CsrfTokenRepository;
import org.springframework.security.web.csrf.HttpSessionCsrfTokenRepository;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

@Configuration
@RequiredArgsConstructor
@EnableWebSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthFilter;
    private final AuthenticationProvider authenticationProvider;



//    @Bean
//    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
//            return http
//                    .authorizeRequests()
//                    .requestMatchers(
//                            "/api/showAll*", "/api/showScores*",
//                            "/api/random*", "/api/saveForTermName*",
//                            "/api/save*", "/api/showDescription*",
//                            "/api/verifyAnswer*").hasRole("USER")
//                    .anyRequest().permitAll()
//
//                    .and()
//                    .formLogin()
//                    .loginPage("/api/login").defaultSuccessUrl("/api/showAll")
//
//                    .and()
//                    .logout()
//                    .logoutRequestMatcher(new AntPathRequestMatcher("/api/logout"))
//                    //.logoutSuccessUrl("/api/login")
//
//                    // Make H2-Console non-secured; for debug purposes
//                    //.and()
//                    //.csrf()
////                    .ignoringRequestMatchers("/h2-console/**")
//
//                    // Allow pages to be loaded in frames from the same origin; needed for H2-Console
//                    .and()
//                    .headers()
//                    .frameOptions()
//                    .sameOrigin()
//
//                    .and()
//                    .build();
//        }
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception{
        http
                .csrf().disable()
                .authorizeHttpRequests()
                .requestMatchers("/api/register/**", "/api/login/**", "/api/login/request")
                .permitAll()
                .anyRequest()
                .authenticated()
                .and()
                .sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and()
                .authenticationProvider(authenticationProvider)
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CsrfTokenRepository csrfTokenRepository(){
        HttpSessionCsrfTokenRepository repository = new HttpSessionCsrfTokenRepository();

        // This is the second part you were missing
        repository.setHeaderName("X-XSRF-TOKEN");
        return repository;
    }



//    @Autowired
//    public void configureGlobal(AuthenticationManagerBuilder auth) throws Exception {
//        BCryptPasswordEncoder enc = new BCryptPasswordEncoder();
//            auth.
//                    inMemoryAuthentication()
//                    .withUser("admin")
//                    .password(enc.encode("admin")).roles("USER", "ADMIN")
//                    .and()
//                    .withUser("user")
//                    .password(enc.encode("user")).roles("USER");
//    }



}
