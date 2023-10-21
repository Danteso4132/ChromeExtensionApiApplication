package com.danteso.chromeextensionapiapplication.security;

import com.danteso.chromeextensionapiapplication.security.controller.LoginController;
import com.danteso.chromeextensionapiapplication.security.repo.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.Iterator;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final Logger LOG = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

//        Enumeration<String> headerNames = request.getHeaderNames();
//        Iterator<String> stringIterator = headerNames.asIterator();
//        while (stringIterator.hasNext()){
//            String s = stringIterator.next();
//            LOG.debug("Header {} = {}", s, request.getHeader(s));
//        }
        if (request.getRequestURL().toString().contains("/register") || request.getRequestURL().toString().contains("/login")) {
            filterChain.doFilter(request, response);
            return;
        }

        String authHeader = request.getHeader("Authorization");
        LOG.debug("authHeader = {}", authHeader);
        final String userEmail;
        LOG.debug("request = {}", request);
        String jwtFromCookies = extractJwtFromCookies(request.getCookies());
        String jwtFromRequest = extractJwtFromRequest(request);
        String jwt = "";
        if (!jwtFromRequest.equals("")) {
            jwt = jwtFromRequest;
        } else if (!jwtFromCookies.equals("")) {
            jwt = jwtFromCookies;
        } else {
            filterChain.doFilter(request, response);
            return;
        }

        LOG.debug("jwt = {}", jwt);
        userEmail = jwtService.extractUsername(jwt);
        if (userEmail != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            UserDetails userDetails = this.userDetailsService.loadUserByUsername(userEmail);
            if (jwtService.isTokenValid(jwt, userDetails)) {
                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                        userDetails,
                        null,
                        userDetails.getAuthorities()
                );
                LOG.debug("Created authToken = {}", authToken);
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authToken);
                LOG.debug("set authToken to context");
            }
        }
        filterChain.doFilter(request, response);
    }

    private String extractJwtFromCookies(Cookie[] cookies) {
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                LOG.debug("cookie = {}", cookie.getName());
                if ("token".equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }
        return "";
    }

    private String extractJwtFromRequest(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return "";
        } else {
            String bearerJwt = authHeader.substring(7);
            LOG.debug("bearerJwt = {}", bearerJwt);
            return bearerJwt;
        }
    }
}
