package com.itb.inf3an.nutrilife.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    // erros de validação @Valid
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<?> handleValidation(MethodArgumentNotValidException e) {
        String mensagem = e.getBindingResult().getFieldErrors().stream()
            .map(f -> f.getField() + ": " + f.getDefaultMessage())
            .collect(Collectors.joining(", "));
        log.warn("Erro de validação: {}", mensagem);
        return ResponseEntity.badRequest().body(Map.of("message", mensagem));
    }

    // erro de negócio esperado (ex: anamnese não encontrada)
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<?> handleRuntime(RuntimeException e) {
        log.warn("Erro de negócio: {}", e.getMessage());
        return ResponseEntity
            .status(HttpStatus.UNPROCESSABLE_ENTITY)
            .body(Map.of("message", e.getMessage() != null ? e.getMessage() : "Erro de negócio"));
    }

    // erro inesperado do servidor
    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> handleGeneric(Exception e) {
        log.error("Erro interno: {}", e.getMessage(), e);
        return ResponseEntity
            .status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(Map.of("message", "Erro interno do servidor. Tente novamente."));
    }
}
