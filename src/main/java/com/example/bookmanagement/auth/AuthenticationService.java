package com.example.bookmanagement.auth;

import com.example.bookmanagement.dto.LoginRequest;
import com.example.bookmanagement.dto.LoginResponse;
import com.example.bookmanagement.dto.RegisterRequest;
import com.example.bookmanagement.entity.User;
import com.example.bookmanagement.exception.UserArgumentInvalidException;
import com.example.bookmanagement.exception.UserRegistrationException;
import com.example.bookmanagement.repository.UserRepository;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthenticationService {
    private final UserRepository userRepository;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;

    public AuthenticationService(UserRepository userRepository, AuthenticationManager authenticationManager, JwtService jwtService, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.passwordEncoder = passwordEncoder;
    }

    public LoginResponse authentication(LoginRequest loginRequest) {
//        ユーザー名バリデーションチェック
        if (loginRequest.getMail() == null || loginRequest.getMail().isEmpty()) {
            throw new UserArgumentInvalidException("ユーザー名が不正です");
        }
//        パスワードバリデーションチェック
        if (loginRequest.getPlainPassword() == null || loginRequest.getPlainPassword().isEmpty()) {
            throw new UserArgumentInvalidException("パスワードが不正です");
        }

        authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(loginRequest.getMail(), loginRequest.getPlainPassword())
        );
        String token = jwtService.generateToken(loginRequest.getMail()); // トークン生成
        LoginResponse loginResponse = new LoginResponse();
        loginResponse.setToken(token); // レスポンスにトークンをセット
        return loginResponse;
    }

    public LoginResponse register(RegisterRequest registerRequest) {
        if (registerRequest.getMail() == null || registerRequest.getMail().isEmpty()) {
            throw new UserRegistrationException("メールアドレスが不正です");
        }
        if (registerRequest.getPlainPassword() == null || registerRequest.getPlainPassword().isEmpty()) {
            throw new UserRegistrationException("パスワードが不正です");
        }
        if (userRepository.getUserByMail(registerRequest.getMail()) != null) {
            throw new UserRegistrationException("既に登録済みのメールアドレスです");
        }
        String encodedPassword = passwordEncoder.encode(registerRequest.getPlainPassword());
        User user = new User();
        user.setMail(registerRequest.getMail());
        user.setName(registerRequest.getName());
        user.setPassword(encodedPassword);
        userRepository.insertUser(user);
        LoginResponse loginResponse = new LoginResponse();
        loginResponse.setToken(jwtService.generateToken(registerRequest.getMail()));
        return loginResponse;
    }

}
