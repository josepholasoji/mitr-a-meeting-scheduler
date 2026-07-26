package com.doodle.challenge.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

// cleared in finally so it never leaks onto a later request served by the same (virtual) thread
@Component
public class UserIdMdcFilter extends OncePerRequestFilter {

    private static final String MDC_USER_ID_KEY = "userId";

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof UUID userId) {
            MDC.put(MDC_USER_ID_KEY, userId.toString());
        }
        try {
            filterChain.doFilter(request, response);
        } finally {
            MDC.remove(MDC_USER_ID_KEY);
        }
    }
}
