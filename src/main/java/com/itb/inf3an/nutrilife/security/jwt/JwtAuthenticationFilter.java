package com.itb.inf3an.nutrilife.security.jwt;

import com.itb.inf3an.nutrilife.model.entity.Usuario;
import com.itb.inf3an.nutrilife.security.token.TokenRepository;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

	private final JwtService jwtService;
	private final UserDetailsService userDetailsService;
	private final TokenRepository tokenRepository;

	public JwtAuthenticationFilter(JwtService jwtService, UserDetailsService userDetailsService, TokenRepository tokenRepository) {
		this.jwtService = jwtService;
		this.userDetailsService = userDetailsService;
		this.tokenRepository = tokenRepository;
	}

	@Override
	protected void doFilterInternal(
			@NonNull HttpServletRequest request,
			@NonNull HttpServletResponse response,
			@NonNull FilterChain filterChain
	) throws ServletException, IOException {
		final String authHeader = request.getHeader("Authorization");
		if (authHeader == null || !authHeader.startsWith("Bearer ")) {
			filterChain.doFilter(request, response);
			return;
		}
		final String token = authHeader.substring(7).trim();

		// Token expirado, malformado, com assinatura errada ou de usuário removido: segue sem autenticar.
		// Rotas públicas continuam funcionando e as protegidas respondem 401 pelo AuthenticationEntryPoint.
		try {
			final String userEmail = jwtService.extractUsername(token);
			if (userEmail != null && SecurityContextHolder.getContext().getAuthentication() == null) {
				Usuario userDetails = (Usuario) this.userDetailsService.loadUserByUsername(userEmail);

				var tokenAtivo = tokenRepository.findByToken(token)
						.map(t -> !t.isExpired() && !t.isRevoked()
								&& t.getUsuario() != null
								&& t.getUsuario().getEmail().equals(userDetails.getEmail()))
						.orElse(false);

				if (tokenAtivo && jwtService.isTokenValid(token, userDetails)) {
					UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
							userDetails,
							null,
							userDetails.getAuthorities()
					);
					authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
					SecurityContextHolder.getContext().setAuthentication(authToken);
				}
			}
		} catch (JwtException | IllegalArgumentException | UsernameNotFoundException e) {
			logger.debug("JWT rejeitado: " + e.getMessage());
		}
		filterChain.doFilter(request, response);
	}
}
