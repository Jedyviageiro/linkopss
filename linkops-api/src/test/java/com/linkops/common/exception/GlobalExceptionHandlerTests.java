package com.linkops.common.exception;

import com.linkops.common.response.ApiError;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.lang.reflect.Method;

import static org.assertj.core.api.Assertions.assertThat;

class GlobalExceptionHandlerTests {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void shouldReturnStandardNotFoundErrorInPortuguese() {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v1/users/123");

        ResponseEntity<ApiError> response = handler.handleResourceNotFound(
                new ResourceNotFoundException("Utilizador não encontrado."),
                request
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().error()).isEqualTo("Recurso não encontrado");
        assertThat(response.getBody().message()).isEqualTo("Utilizador não encontrado.");
        assertThat(response.getBody().path()).isEqualTo("/api/v1/users/123");
        assertThat(response.getBody().validationErrors()).isEmpty();
    }

    @Test
    void shouldReturnValidationErrorsByField() throws Exception {
        Object target = new Object();
        BeanPropertyBindingResult bindingResult = new BeanPropertyBindingResult(target, "request");
        bindingResult.addError(new FieldError("request", "email", "Informe um endereço de e-mail válido."));

        Method method = GlobalExceptionHandlerTests.class.getDeclaredMethod("validationTarget", Object.class);
        MethodArgumentNotValidException exception = new MethodArgumentNotValidException(
                new org.springframework.core.MethodParameter(method, 0),
                bindingResult
        );
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/v1/users");

        ResponseEntity<ApiError> response = handler.handleMethodArgumentNotValid(exception, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().error()).isEqualTo("Erro de validação");
        assertThat(response.getBody().validationErrors())
                .containsEntry("email", "Informe um endereço de e-mail válido.");
    }

    @SuppressWarnings("unused")
    private void validationTarget(Object request) {
    }
}
