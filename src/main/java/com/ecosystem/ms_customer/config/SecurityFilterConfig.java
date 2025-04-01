package com.ecosystem.ms_customer.config;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class SecurityFilterConfig extends OncePerRequestFilter {

    private final String issuer;
    private final String secret;

    public SecurityFilterConfig(@Value("${authentication.jwt.issuer}") String issuer,
                                @Value("${authentication.algorithm.secret}") String secret) {
        this.issuer = issuer;
        this.secret = secret;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String header = request.getHeader("Authorization");
        if (header == null) {
            filterChain.doFilter(request, response);
            return;
        }

        var verifier = JWT.require(Algorithm.HMAC256(this.secret)).withIssuer(this.issuer).build();

        var token = verifier.verify(header.replace("Bearer ", ""));
        if (token == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        request.setAttribute("customerEmail", token.getSubject());

        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(
                token.getSubject(), null, null
        ));

        filterChain.doFilter(request, response);
    }
}
