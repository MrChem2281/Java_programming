package com.example.demo.jwt;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.security.web.webauthn.authentication.WebAuthnAuthentication;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    @Value("(jwt.access.name)")
    private String accessCookieName;
    private JwtTokenProvider tokenProvider;
    private UserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        // TODO Auto-generated method stub
        // throw new UnsupportedOperationException("Unimplemented method 'doFilterInternal'");
                String token = "";
                Cookie[] cookies = request.getCookies();
                for (Cookie cookie: cookies){
                    if (accessCookieName.equals(cookie.getName())){
                        token = cookie.getValue();
                    }
                }
                if(token.equals("") || !tokenProvider.validateToken(token)){
                    filterChain.doFilter(request, response);
                    return;
                }

                String username = tokenProvider.getUsername(token);


                if(username == null){
                    filterChain.doFilter(request, response);
                    return;
                }



                UserDetails user = userDetailsService.loadUserByUsername(username);
                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authToken);

                filterChain.doFilter(request, response);
            }

}


