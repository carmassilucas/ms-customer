package com.ecosystem.ms_customer.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.intercept.AuthorizationFilter;

@Configuration
public class SecurityConfig {

    private final SecurityFilterConfig filter;

    public SecurityConfig(SecurityFilterConfig filter) {
        this.filter = filter;
    }

    @Bean
    public SecurityFilterChain web(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(authorize ->
                        authorize.requestMatchers(HttpMethod.POST, "/v1/customers").permitAll()
                                .requestMatchers(HttpMethod.POST, "/v1/customers/auth").permitAll()
                                .anyRequest().authenticated()
                )
                .addFilterBefore(this.filter, AuthorizationFilter.class);

        return http.build();
    }
}
