package com.itb.inf3an.nutrilife.auth;

import com.itb.inf3an.nutrilife.dto.auth.AuthenticationRequest;
import com.itb.inf3an.nutrilife.dto.auth.AuthenticationResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/** Login: autentica, gera o JWT e devolve {access_token, usuario}. O logout é tratado pelo Spring Security (/auth/logout). */
@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthenticationService service;

    public AuthController(AuthenticationService service) {
        this.service = service;
    }

    @PostMapping("/login")
    public ResponseEntity<AuthenticationResponse> login(@RequestBody AuthenticationRequest request) {
        return ResponseEntity.ok(service.authenticate(request));
    }
}
