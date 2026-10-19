package com.deploy.devops.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@Profile("dev") // Spring solo levanta este filtro si el perfil activo es "dev"
public class DevDelayFilter extends OncePerRequestFilter {

    @Value("${APP_DELAY_MS:5000}")
    private long delayMs;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        // Solo aplicamos retardo a las rutas de la API
        if (request.getRequestURI().startsWith("/api/") && delayMs > 0) {
            try {
                Thread.sleep(delayMs);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new ServletException("Se interrumpió la espera en DevDelayFilter", e);
            }
        }

        // Continúa con la ejecución normal hacia el controlador
        filterChain.doFilter(request, response);
    }
}