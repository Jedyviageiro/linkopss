package com.linkops.security;

import com.linkops.common.response.ApiError;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;

@Component
public class SecurityErrorHandler implements AuthenticationEntryPoint, AccessDeniedHandler {

    private final ObjectMapper objectMapper;

    public SecurityErrorHandler(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public void commence(
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException exception
    ) throws IOException {
        writeError(
                response,
                ApiError.of(
                        HttpServletResponse.SC_UNAUTHORIZED,
                        "Não autenticado",
                        "É necessário autenticar-se para aceder a este recurso.",
                        request.getRequestURI()
                )
        );
    }

    @Override
    public void handle(
            HttpServletRequest request,
            HttpServletResponse response,
            org.springframework.security.access.AccessDeniedException exception
    ) throws IOException {
        writeError(
                response,
                ApiError.of(
                        HttpServletResponse.SC_FORBIDDEN,
                        "Acesso negado",
                        "Não tem permissão para realizar esta operação.",
                        request.getRequestURI()
                )
        );
    }

    private void writeError(HttpServletResponse response, ApiError error) throws IOException {
        response.setStatus(error.status());
        response.setCharacterEncoding("UTF-8");
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        objectMapper.writeValue(response.getOutputStream(), error);
    }
}
