package com.sindikbackend.auth.security.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class JwtAccessDeniedHandler implements AccessDeniedHandler {
    private static final Logger log = LoggerFactory.getLogger(JwtAccessDeniedHandler.class);

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response,
                       AccessDeniedException accessDeniedException) throws IOException, ServletException {

        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);

        String path = request.getServletPath();
        boolean unitMutation = path.startsWith("/units/")
                && !"GET".equalsIgnoreCase(request.getMethod());
        boolean missingBuildingAssignment = unitMutation
                && "You are not assigned to manage this building".equals(accessDeniedException.getMessage());
        log.warn("Access denied for {} {} (reason: {})", request.getMethod(), path,
                missingBuildingAssignment ? "building assignment required" : "role or permission required");

        final Map<String, Object> body = new HashMap<>();
        body.put("status", HttpServletResponse.SC_FORBIDDEN);
        body.put("error", "Forbidden");
        body.put("message", missingBuildingAssignment
                ? "Le syndic connecté n'est pas affecté au bâtiment de cette unité."
                : unitMutation
                    ? "Cette action est réservée à un compte SYNDIC."
                    : "You don't have permission to access this resource");
        body.put("path", path);

        final ObjectMapper mapper = new ObjectMapper();
        mapper.writeValue(response.getOutputStream(), body);
    }
}
