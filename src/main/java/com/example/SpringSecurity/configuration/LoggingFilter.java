package com.example.SpringSecurity.configuration;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class LoggingFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(LoggingFilter.class);

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        logger.info("Request URI: {}, Method: {}, RemoteAddr: {}",
                request.getRequestURI(), request.getMethod(), request.getRemoteAddr());

        long startTime = System.currentTimeMillis();
        filterChain.doFilter(request, response);
        long duration = System.currentTimeMillis() - startTime;

        logger.info("Response status: {}, Duration: {} ms", response.getStatus(), duration);
    }
}
