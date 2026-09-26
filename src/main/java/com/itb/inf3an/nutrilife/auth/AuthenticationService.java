package com.itb.inf3an.nutrilife.auth;

import com.itb.inf3an.nutrilife.dto.auth.AuthenticationRequest;
import com.itb.inf3an.nutrilife.dto.auth.AuthenticationResponse;
import com.itb.inf3an.nutrilife.exceptions.BadRequest;
import com.itb.inf3an.nutrilife.exceptions.Unauthorized;
import com.itb.inf3an.nutrilife.model.entity.Admin;
import com.itb.inf3an.nutrilife.model.entity.Nutricionista;
import com.itb.inf3an.nutrilife.model.entity.Usuario;
import com.itb.inf3an.nutrilife.model.repository.UsuarioRepository;
import com.itb.inf3an.nutrilife.model.services.Auditoria;
import com.itb.inf3an.nutrilife.security.jwt.JwtService;
import com.itb.inf3an.nutrilife.security.token.Token;
import com.itb.inf3an.nutrilife.security.token.TokenRepository;
import com.itb.inf3an.nutrilife.security.token.TokenType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthenticationService {

    private static final String CREDENCIAIS_INVALIDAS = "E-mail ou senha inválidos.";

    private final UsuarioRepository repository;
    private final TokenRepository tokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final Auditoria auditoria;

    public AuthenticationService(
            UsuarioRepository repository,
            TokenRepository tokenRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService,
            AuthenticationManager authenticationManager,
            Auditoria auditoria) {
        this.repository = repository;
        this.tokenRepository = tokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.authenticationManager = authenticationManager;
        this.auditoria = auditoria;
    }

    public AuthenticationResponse authenticate(AuthenticationRequest request) {
        if (request.getEmail() == null || request.getEmail().isBlank()
                || request.getPassword() == null || request.getPassword().isBlank()) {
            throw new BadRequest("E-mail e senha são obrigatórios.");
        }

        var user = repository.findByEmail(request.getEmail())
                .orElseThrow(() -> new Unauthorized(CREDENCIAIS_INVALIDAS));

        // Migra senha legada em texto puro para BCrypt antes de autenticar
        if (!user.getSenha().startsWith("$2") && user.getSenha().equals(request.getPassword())) {
            user.setSenha(passwordEncoder.encode(request.getPassword()));
            user = repository.save(user);
        }

        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));
        } catch (AuthenticationException e) {
            auditoria.registrar(user.getNomeCompleto(), "Tentativa de login com senha incorreta", "Falha");
            throw new Unauthorized(CREDENCIAIS_INVALIDAS);
        }

        // Nutricionista com cadastro pendente (cod_status = false) pode entrar; os demais precisam estar ativos
        if (!user.isCodStatus() && !(user instanceof Nutricionista)) {
            throw new Unauthorized("Conta inativa, por favor procurar o administrador da conta");
        }
        if (user instanceof Admin admin && !admin.isAprovado()) {
            throw new Unauthorized("Administrador ainda não aprovado.");
        }

        user.setUltimoAcesso(java.time.LocalDateTime.now());
        user = repository.save(user);
        auditoria.registrar(user.getNomeCompleto(), "Login realizado (" + user.getTipoUsuario() + ")", "Sucesso");

        revokeAllUserTokens(user);
        var jwtToken = jwtService.generateToken(user);
        saveUserToken(user, jwtToken);

        return new AuthenticationResponse(jwtToken, user);
    }

    private void saveUserToken(Usuario usuario, String jwtToken) {
        var token = new Token();
        token.setUsuario(usuario);
        token.setToken(jwtToken);
        token.setTokenType(TokenType.BEARER);
        token.setExpired(false);
        token.setRevoked(false);
        tokenRepository.save(token);
    }

    private void revokeAllUserTokens(Usuario usuario) {
        var validUserTokens = tokenRepository.findAllValidTokenByUser(usuario.getId());
        if (validUserTokens.isEmpty())
            return;
        validUserTokens.forEach(token -> {
            token.setExpired(true);
            token.setRevoked(true);
        });
        tokenRepository.saveAll(validUserTokens);
    }
}
