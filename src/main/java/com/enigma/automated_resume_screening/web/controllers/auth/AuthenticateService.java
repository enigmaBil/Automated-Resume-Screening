package com.enigma.automated_resume_screening.web.controllers.auth;

import com.enigma.automated_resume_screening.config.JwtService;
import com.enigma.automated_resume_screening.dao.entities.Rh;
import com.enigma.automated_resume_screening.dao.entities.Token;
import com.enigma.automated_resume_screening.dao.enums.TokenType;
import com.enigma.automated_resume_screening.dao.repositories.RhRepository;
import com.enigma.automated_resume_screening.dao.repositories.TokenRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Transactional
@Service
@RequiredArgsConstructor
public class AuthenticateService {

    private final RhRepository rhRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final TokenRepository tokenRepository;


    public AuthenticationResponse register(RegisterRequest request) {

    Rh rh = Rh.builder()
            .name(request.getName())
            .email(request.getEmail())
            .password(passwordEncoder.encode(request.getPassword()))
            .build();

        var savedRh = rhRepository.save(rh);
        var jwtToken = jwtService.generateToken(rh);
        var refreshToken = jwtService.generateRefreshToken(savedRh);
        saveRhToken(savedRh, jwtToken);
        return AuthenticationResponse.builder().accessToken(jwtToken).refreshToken(refreshToken).build();
    }

    public AuthenticationResponse authenticate(AuthenticationRequest request) {

        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));
        var  rh = rhRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new UsernameNotFoundException("Utilisateur introuvable : " + request.getEmail()));
        var jwtToken = jwtService.generateToken(rh);
        var refreshToken = jwtService.generateRefreshToken(rh);
        revokeAllRhTokens(rh);
        saveRhToken(rh, jwtToken);
        return AuthenticationResponse.builder().accessToken(jwtToken).refreshToken(refreshToken).build();
    }

    private void revokeAllRhTokens(Rh rh){
        var validRhTokens = tokenRepository.findAllValidTokenByRh(rh.getId());
        if (validRhTokens.isEmpty()) return;
        validRhTokens.forEach(token -> {
            token.setExpired(true);
            token.setRevoked(true);
        });
        tokenRepository.saveAll(validRhTokens);
    }

    private void saveRhToken(Rh rh, String jwtToken) {
        var token = Token.builder()
                .rh(rh)
                .token(jwtToken)
                .tokenType(TokenType.BEARER)
                .expired(false)
                .revoked(false)
                .build();
        tokenRepository.save(token);
    }


    public AuthenticationResponse refreshToken(HttpServletRequest request, HttpServletResponse response) throws IOException {
        final String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            response.setStatus(HttpStatus.BAD_REQUEST.value());
            response.getWriter().write("Refresh token manquant ou invalide");
            return null;
        }

        final String refreshToken = authHeader.substring(7);
        final String userEmail = jwtService.extractUsername(refreshToken);

        if (userEmail == null) {
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            response.getWriter().write("Utilisateur non identifié");
            return null;
        }

        var userDetails = rhRepository.findByEmail(userEmail)
                .orElseThrow(() -> new UsernameNotFoundException("Utilisateur introuvable : " + userEmail));

        if (!jwtService.isTokenValid(refreshToken, userDetails)) {
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            response.getWriter().write("Refresh token invalide ou expiré");
            return null;
        }

        var accessToken = jwtService.generateToken(userDetails);
        revokeAllRhTokens(userDetails);
        saveRhToken(userDetails, accessToken);

        var authResponse = AuthenticationResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();

        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        new ObjectMapper().writeValue(response.getOutputStream(), authResponse);
        return authResponse;
    }

}

