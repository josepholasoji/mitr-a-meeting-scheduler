package com.doodle.challenge.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final BasicAuthenticationProvider basicAuthenticationProvider;
    private final RestAuthenticationEntryPoint authenticationEntryPoint;
    private final UserIdMdcFilter userIdMdcFilter;

    public SecurityConfig(
            BasicAuthenticationProvider basicAuthenticationProvider,
            RestAuthenticationEntryPoint authenticationEntryPoint,
            UserIdMdcFilter userIdMdcFilter) {
        this.basicAuthenticationProvider = basicAuthenticationProvider;
        this.authenticationEntryPoint = authenticationEntryPoint;
        this.userIdMdcFilter = userIdMdcFilter;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authenticationProvider(basicAuthenticationProvider)
                .authorizeHttpRequests(auth -> auth
                        // registration itself must be reachable without credentials
                        .requestMatchers(HttpMethod.POST, "/users").permitAll()
                        .requestMatchers("/swagger-ui/**", "/swagger-ui.html", "/v3/api-docs/**").permitAll()
                        // Prometheus has no way to supply per-request credentials
                        .requestMatchers("/actuator/health/**", "/actuator/health", "/actuator/prometheus").permitAll()
                        .anyRequest().authenticated())
                .httpBasic(basic -> basic.authenticationEntryPoint(authenticationEntryPoint))
                .exceptionHandling(exceptions -> exceptions.authenticationEntryPoint(authenticationEntryPoint))
                .addFilterAfter(userIdMdcFilter, BasicAuthenticationFilter.class);
        return http.build();
    }
}
