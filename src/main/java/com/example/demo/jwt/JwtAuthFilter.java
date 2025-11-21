package com.example.demo.jwt;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

    @Value("${jwt.access.name}")
    private String accessCookieName;
    
    private final JwtTokenProvider tokenProvider;
    private final UserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String token = "";
        Cookie[] cookies = request.getCookies();
        
        // ЕДИНСТВЕННОЕ ИЗМЕНЕНИЕ - добавил проверку на null
        if (cookies != null) {
            for (Cookie cookie: cookies){
                if (accessCookieName.equals(cookie.getName())){
                    token = cookie.getValue();
                    log.debug("Found JWT token in cookies");
                }
            }
        } else {
            log.debug("No cookies found in request");
        }

        if(token.equals("")) {
            log.debug("No JWT token found, skipping authentication");
            filterChain.doFilter(request, response);
            return;
        }

        if(!tokenProvider.validateToken(token)){
            log.warn("Invalid JWT token");
            filterChain.doFilter(request, response);
            return;
        }

        String username = tokenProvider.getUsername(token);
        if(username == null){
            log.warn("Username not found in JWT token");
            filterChain.doFilter(request, response);
            return;
        }

        log.debug("Valid JWT token for user: {}", username);

        UserDetails user = userDetailsService.loadUserByUsername(username);
        UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());
        authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
        SecurityContextHolder.getContext().setAuthentication(authToken);
        
        log.info("User authenticated: {}", username);

        filterChain.doFilter(request, response);
    }
}