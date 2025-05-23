package com.enigma.automated_resume_screening.web.controllers.auth;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class AuthenticationController {

    private final AuthenticateService authenticateService;
    @PostMapping("/register")
    public ResponseEntity<AuthenticationResponse> register(
            @RequestBody @Valid RegisterRequest request
    ){
        return  ResponseEntity.ok(authenticateService.register(request));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthenticationResponse> login(
            @RequestBody @Valid AuthenticationRequest request
    ){
        return  ResponseEntity.ok(authenticateService.authenticate(request));
    }

    @PostMapping("/refresh-token")
    public void refreshToken(
            HttpServletRequest request,
            HttpServletResponse response
    ) throws IOException {
        authenticateService.refreshToken(request, response);
    }



}
