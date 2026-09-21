package com.itb.inf3an.nutrilife.auth;

import tools.jackson.databind.ObjectMapper;
import com.itb.inf3an.nutrilife.dto.auth.AuthenticationRequest;
import com.itb.inf3an.nutrilife.dto.auth.AuthenticationResponse;
import com.itb.inf3an.nutrilife.dto.auth.RegisterRequest;
import com.itb.inf3an.nutrilife.exceptions.BadRequest;
import com.itb.inf3an.nutrilife.exceptions.Unauthorized;
import com.itb.inf3an.nutrilife.model.entity.Nutricionista;
import com.itb.inf3an.nutrilife.model.entity.Usuario;
import com.itb.inf3an.nutrilife.model.enums.TipoUsuario;
import com.itb.inf3an.nutrilife.model.repository.UsuarioRepository;
import com.itb.inf3an.nutrilife.security.jwt.JwtService;
import com.itb.inf3an.nutrilife.security.token.Token;
import com.itb.inf3an.nutrilife.security.token.TokenRepository;
import com.itb.inf3an.nutrilife.security.token.TokenType;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class AuthenticationService {

    private final UsuarioRepository repository;
    private final TokenRepository tokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    public AuthenticationService(
            UsuarioRepository repository,
            TokenRepository tokenRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService,
            AuthenticationManager authenticationManager) {
        this.repository = repository;
        this.tokenRepository = tokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.authenticationManager = authenticationManager;
    }

    public AuthenticationResponse register(RegisterRequest request) {
        if (request.getEmail() == null || request.getEmail().isBlank()) {
            throw new BadRequest("E-mail é obrigatório");
        }
        if (request.getPassword() == null || request.getPassword().isBlank()) {
            throw new BadRequest("Senha é obrigatória");
        }
        if (request.getTipoUsuario() == null) {
            request.setTipoUsuario(TipoUsuario.CLIENTE);
        }

        var usuarioDb = repository.findByEmail(request.getEmail());
        if (usuarioDb.isPresent()) {
            throw new BadRequest("Já existe este email cadastrado em nossa base de dados");
        }

        String nomeClasse = String.valueOf(request.getTipoUsuario());
        nomeClasse = nomeClasse.toLowerCase();
        char primeiroCharMaiusculo = Character.toUpperCase(nomeClasse.charAt(0));
        nomeClasse = primeiroCharMaiusculo + nomeClasse.substring(1);

        try {
            Class<?> myClass = Class.forName("com.itb.inf3an.nutrilife.model.entity." + nomeClasse);
            Usuario usuario = (Usuario) myClass.getDeclaredConstructor().newInstance();

            usuario.setCodStatus(true);
            usuario.setNomeCompleto(request.getNome());
            usuario.setEmail(request.getEmail());
            usuario.setSenha(passwordEncoder.encode(request.getPassword()));
            usuario.setTipoUsuario(request.getTipoUsuario());
            usuario.setCpf(request.getCpf());
            usuario.setTelefone(request.getTelefone());
            usuario.setDataNascimento(request.getDataNascimento());

            if (usuario instanceof Nutricionista nutri) {
                nutri.setCrn(request.getCrn());
                nutri.setEspecialidade(request.getEspecialidade());
                nutri.setCodStatus(true);
            }

            var savedUser = repository.save(usuario);
            var jwtToken = jwtService.generateToken(savedUser);
            var refreshToken = jwtService.generateRefreshToken(savedUser);

            saveUserToken(savedUser, jwtToken);
            return new AuthenticationResponse(jwtToken, refreshToken);

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Erro ao cadastrar usuário: " + e.getMessage());
        }
    }

    public AuthenticationResponse authenticate(AuthenticationRequest request) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getEmail(),
                            request.getPassword()
                    )
            );
        } catch (Exception e) {
            e.printStackTrace();
            throw new BadRequest("Email ou Senha Incorreto");
        }

        var user = repository.findByEmail(request.getEmail())
                .orElseThrow(() -> new BadRequest("Email ou Senha Incorreto"));

        if (!user.isCodStatus()) {
            throw new Unauthorized("Conta inativa, por favor procurar o administrador da conta");
        }

        var jwtToken = jwtService.generateToken(user);
        var refreshToken = jwtService.generateRefreshToken(user);
        revokeAllUserTokens(user);
        saveUserToken(user, jwtToken);

        return new AuthenticationResponse(jwtToken, refreshToken);
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

    public void refreshToken(
            HttpServletRequest request,
            HttpServletResponse response
    ) throws IOException {
        final String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
        final String refreshToken;
        final String userEmail;
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return;
        }
        refreshToken = authHeader.substring(7);
        userEmail = jwtService.extractUsername(refreshToken);
        if (userEmail != null) {
            var user = this.repository.findByEmail(userEmail)
                    .orElseThrow();
            if (jwtService.isTokenValid(refreshToken, user)) {
                var accessToken = jwtService.generateToken(user);
                revokeAllUserTokens(user);
                saveUserToken(user, accessToken);
                var authResponse = new AuthenticationResponse(accessToken, refreshToken);
                new ObjectMapper().writeValue(response.getOutputStream(), authResponse);
            }
        }
    }
}
