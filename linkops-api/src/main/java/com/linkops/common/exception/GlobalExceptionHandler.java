package com.linkops.common.exception;

import com.linkops.common.response.ApiError;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiError> handleResourceNotFound(
            ResourceNotFoundException exception,
            HttpServletRequest request
    ) {
        return buildError(
                HttpStatus.NOT_FOUND,
                "Recurso não encontrado",
                messageOrDefault(exception, "O recurso solicitado não foi encontrado."),
                request
        );
    }

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ApiError> handleBadRequest(
            BadRequestException exception,
            HttpServletRequest request
    ) {
        return buildError(
                HttpStatus.BAD_REQUEST,
                "Requisição inválida",
                messageOrDefault(exception, "Não foi possível processar a requisição."),
                request
        );
    }

    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<ApiError> handleConflict(
            ConflictException exception,
            HttpServletRequest request
    ) {
        return buildError(
                HttpStatus.CONFLICT,
                "Conflito",
                messageOrDefault(exception, "A operação solicitada está em conflito com o estado atual."),
                request
        );
    }

    @ExceptionHandler(ObjectOptimisticLockingFailureException.class)
    public ResponseEntity<ApiError> handleOptimisticLock(
            ObjectOptimisticLockingFailureException exception,
            HttpServletRequest request
    ) {
        return buildError(
                HttpStatus.CONFLICT,
                "Conflito",
                "O pedido foi alterado por outra operação. Atualize os dados e tente novamente.",
                request
        );
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ApiError> handleAuthentication(
            AuthenticationException exception,
            HttpServletRequest request
    ) {
        return buildError(
                HttpStatus.UNAUTHORIZED,
                "Não autenticado",
                "Credenciais ou token inválidos.",
                request
        );
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiError> handleAccessDenied(
            AccessDeniedException exception,
            HttpServletRequest request
    ) {
        return buildError(
                HttpStatus.FORBIDDEN,
                "Acesso negado",
                "Não tem permissão para realizar esta operação.",
                request
        );
    }

    @ExceptionHandler(ServiceUnavailableException.class)
    public ResponseEntity<ApiError> handleServiceUnavailable(
            ServiceUnavailableException exception,
            HttpServletRequest request
    ) {
        return buildError(
                HttpStatus.SERVICE_UNAVAILABLE,
                "Serviço indisponível",
                messageOrDefault(
                        exception,
                        "O serviço está temporariamente indisponível."
                ),
                request
        );
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleMethodArgumentNotValid(
            MethodArgumentNotValidException exception,
            HttpServletRequest request
    ) {
        Map<String, String> errors = new LinkedHashMap<>();

        for (FieldError fieldError : exception.getBindingResult().getFieldErrors()) {
            errors.merge(
                    fieldError.getField(),
                    defaultValidationMessage(fieldError.getDefaultMessage()),
                    (first, second) -> first + "; " + second
            );
        }

        exception.getBindingResult().getGlobalErrors().forEach(error -> errors.merge(
                "global",
                defaultValidationMessage(error.getDefaultMessage()),
                (first, second) -> first + "; " + second
        ));

        return buildValidationError(errors, request);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiError> handleConstraintViolation(
            ConstraintViolationException exception,
            HttpServletRequest request
    ) {
        Map<String, String> errors = new LinkedHashMap<>();

        for (ConstraintViolation<?> violation : exception.getConstraintViolations()) {
            String property = violation.getPropertyPath().toString();
            errors.merge(
                    property,
                    defaultValidationMessage(violation.getMessage()),
                    (first, second) -> first + "; " + second
            );
        }

        return buildValidationError(errors, request);
    }

    @ExceptionHandler(HandlerMethodValidationException.class)
    public ResponseEntity<ApiError> handleHandlerMethodValidation(
            HandlerMethodValidationException exception,
            HttpServletRequest request
    ) {
        Map<String, String> errors = new LinkedHashMap<>();

        exception.getParameterValidationResults().forEach(result ->
                result.getResolvableErrors().forEach(error -> errors.merge(
                        result.getMethodParameter().getParameterName() == null
                                ? "parâmetro"
                                : result.getMethodParameter().getParameterName(),
                        defaultValidationMessage(error.getDefaultMessage()),
                        (first, second) -> first + "; " + second
                ))
        );

        return buildValidationError(errors, request);
    }

    @ExceptionHandler({
            HttpMessageNotReadableException.class,
            MethodArgumentTypeMismatchException.class,
            MissingServletRequestParameterException.class
    })
    public ResponseEntity<ApiError> handleMalformedRequest(
            Exception exception,
            HttpServletRequest request
    ) {
        return buildError(
                HttpStatus.BAD_REQUEST,
                "Requisição inválida",
                "Os dados enviados são inválidos ou estão incompletos.",
                request
        );
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ApiError> handleMethodNotSupported(
            HttpRequestMethodNotSupportedException exception,
            HttpServletRequest request
    ) {
        return buildError(
                HttpStatus.METHOD_NOT_ALLOWED,
                "Método não permitido",
                "O método HTTP utilizado não é permitido para este recurso.",
                request
        );
    }

    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<ApiError> handleMediaTypeNotSupported(
            HttpMediaTypeNotSupportedException exception,
            HttpServletRequest request
    ) {
        return buildError(
                HttpStatus.UNSUPPORTED_MEDIA_TYPE,
                "Formato não suportado",
                "O formato dos dados enviados não é suportado.",
                request
        );
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ApiError> handleMaxUploadSize(
            MaxUploadSizeExceededException exception,
            HttpServletRequest request
    ) {
        return buildError(
                HttpStatus.CONTENT_TOO_LARGE,
                "Ficheiro demasiado grande",
                "A imagem deve ter no máximo 5 MB.",
                request
        );
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ApiError> handleNoResourceFound(
            NoResourceFoundException exception,
            HttpServletRequest request
    ) {
        return buildError(
                HttpStatus.NOT_FOUND,
                "Recurso não encontrado",
                "O recurso solicitado não foi encontrado.",
                request
        );
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleUnexpectedException(
            Exception exception,
            HttpServletRequest request
    ) {
        log.error("Erro inesperado ao processar {} {}", request.getMethod(), request.getRequestURI(), exception);

        return buildError(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Erro interno",
                "Ocorreu um erro interno. Tente novamente mais tarde.",
                request
        );
    }

    private ResponseEntity<ApiError> buildValidationError(
            Map<String, String> errors,
            HttpServletRequest request
    ) {
        HttpStatus status = HttpStatus.BAD_REQUEST;
        ApiError body = new ApiError(
                java.time.Instant.now(),
                status.value(),
                "Erro de validação",
                "Um ou mais campos são inválidos.",
                request.getRequestURI(),
                errors
        );

        return ResponseEntity.status(status).body(body);
    }

    private ResponseEntity<ApiError> buildError(
            HttpStatus status,
            String error,
            String message,
            HttpServletRequest request
    ) {
        return ResponseEntity.status(status).body(
                ApiError.of(status.value(), error, message, request.getRequestURI())
        );
    }

    private String messageOrDefault(RuntimeException exception, String defaultMessage) {
        return exception.getMessage() == null || exception.getMessage().isBlank()
                ? defaultMessage
                : exception.getMessage();
    }

    private String defaultValidationMessage(String message) {
        return message == null || message.isBlank() ? "Valor inválido." : message;
    }
}
