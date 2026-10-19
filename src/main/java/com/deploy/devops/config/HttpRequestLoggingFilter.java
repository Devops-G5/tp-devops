package com.deploy.devops.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.event.Level;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class HttpRequestLoggingFilter extends OncePerRequestFilter {

    private static final Logger LOGGER =
            LoggerFactory.getLogger(HttpRequestLoggingFilter.class);

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return !request.getServletPath().startsWith("/api/");
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        long start = System.nanoTime();
        boolean completed = false;

        try {
            filterChain.doFilter(request, response);
            completed = true;
        } finally {
            int status = completed ? response.getStatus() : 500;
            double durationMs = (System.nanoTime() - start) / 1_000_000.0;
            Level level = status >= 500 ? Level.ERROR
                    : status >= 400 ? Level.WARN : Level.INFO;

            LOGGER.atLevel(level)
                    .addKeyValue("method", request.getMethod())
                    .addKeyValue("path", request.getRequestURI())
                    .addKeyValue("status_code", status)
                    .addKeyValue("duration_ms", durationMs)
                    .log("HTTP request completed");
        }
    }
}
