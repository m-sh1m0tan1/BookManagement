package com.example.bookmanagement.controller;

import com.example.bookmanagement.auth.AuthenticationService;
import com.example.bookmanagement.dto.LoginRequest;
import com.example.bookmanagement.dto.LoginResponse;
import com.example.bookmanagement.dto.RegisterRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthenticationService authenticationService;

    public AuthController(AuthenticationService authenticationService) {
        this.authenticationService = authenticationService;
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest loginRequest) { // ログインJSONを受けてJWTを返すメソッド
        LoginResponse loginResponse = authenticationService.authentication(loginRequest);
        return ResponseEntity.ok(loginResponse); // 200とJWTを返す
    }

    @PostMapping("/register")
    public ResponseEntity<LoginResponse> register(@RequestBody RegisterRequest registerRequest) {
        LoginResponse loginResponse = authenticationService.register(registerRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(loginResponse);
    }
}
