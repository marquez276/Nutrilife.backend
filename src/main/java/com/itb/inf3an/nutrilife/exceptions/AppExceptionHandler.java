package com.itb.inf3an.nutrilife.exceptions;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.beans.TypeMismatchException;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class AppExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(AppExceptionHandler.class);
    private static final ZoneId ZONE_BRASIL = ZoneId.of("America/Sao_Paulo");
    private static final Map<Class<? extends Exception>, HttpStatus> EXCEPTION_STATUS_MAP = new HashMap<>();

    static {
        EXCEPTION_STATUS_MAP.put(BadRequest.class, HttpStatus.BAD_REQUEST);
        EXCEPTION_STATUS_MAP.put(NotFound.class, HttpStatus.NOT_FOUND);
        EXCEPTION_STATUS_MAP.put(Unauthorized.class, HttpStatus.UNAUTHORIZED);
        EXCEPTION_STATUS_MAP.put(Forbidden.class, HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<?> handleValidationExceptions(MethodArgumentNotValidException ex) {
        HttpStatus status = HttpStatus.BAD_REQUEST;
        String[] messages = ex.getBindingResult().getFieldErrors().stream()
                .map(f -> f.getField() + ": " + f.getDefaultMessage())
                .toArray(String[]::new);

        ErrorMessage errorMessage = new ErrorMessage(LocalDateTime.now(ZONE_BRASIL), messages, status);
        return new ResponseEntity<>(errorMessage, new HttpHeaders(), status);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> handleAllExceptions(Exception exception) {
        HttpStatus status = statusOf(exception);

        String message;
        if (exception instanceof Forbidden || exception instanceof AccessDeniedException) {
            message = "Você não tem permissão para acessar este recurso.";
        } else if (status == HttpStatus.INTERNAL_SERVER_ERROR) {
            log.error("Erro inesperado", exception);
            message = "Erro interno do servidor.";
        } else if (status == HttpStatus.BAD_REQUEST && !(exception instanceof BadRequest)) {
            message = "Requisição inválida.";
        } else if (status == HttpStatus.CONFLICT) {
            message = "Operação conflita com dados já existentes.";
        } else {
            message = exception.getLocalizedMessage() != null ? exception.getLocalizedMessage() : exception.toString();
        }

        ErrorMessage errorMessage = new ErrorMessage(LocalDateTime.now(ZONE_BRASIL), new String[]{message}, status);
        return new ResponseEntity<>(errorMessage, new HttpHeaders(), status);
    }

    private static HttpStatus statusOf(Exception e) {
        if (e instanceof AccessDeniedException) return HttpStatus.FORBIDDEN;
        if (e instanceof AuthenticationException) return HttpStatus.UNAUTHORIZED;
        if (e instanceof HttpMessageNotReadableException || e instanceof TypeMismatchException) return HttpStatus.BAD_REQUEST;
        if (e instanceof DataIntegrityViolationException) return HttpStatus.CONFLICT;
        if (e instanceof ErrorResponse er) return HttpStatus.valueOf(er.getStatusCode().value());
        return EXCEPTION_STATUS_MAP.getOrDefault(e.getClass(), HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
