package com.example.bookmanagement.auth;

import com.example.bookmanagement.dto.LoginRequest;
import com.example.bookmanagement.dto.LoginResponse;
import com.example.bookmanagement.dto.RegisterRequest;
import com.example.bookmanagement.entity.User;
import com.example.bookmanagement.exception.MailDuplicateException;
import com.example.bookmanagement.exception.UserArgumentInvalidException;
import com.example.bookmanagement.repository.UserRepository;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.regex.Pattern;

@Service
public class AuthenticationService {
    private final UserRepository userRepository;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;
    private static final Pattern MAIL_PATTERN = Pattern.compile("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$");


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
            throw new UserArgumentInvalidException("メールアドレスが不正です");
        }
        if (registerRequest.getPlainPassword() == null || registerRequest.getPlainPassword().isEmpty()) {
            throw new UserArgumentInvalidException("パスワードが不正です");
        }
        if (userRepository.getUserByMail(registerRequest.getMail()) != null) {
            throw new MailDuplicateException("既に登録済みのメールアドレスです");
        }
        User user = new User();
        user.setMail(registerRequest.getMail());
        user.setName(registerRequest.getName());
        user.setPassword(registerRequest.getPlainPassword());
        if (isInvalidUserArguments(user)) {
            throw new UserArgumentInvalidException("ユーザーアカウントの引数が不正です");
        }
        String encodedPassword = passwordEncoder.encode(registerRequest.getPlainPassword());
        user.setPassword(encodedPassword);
        if (userRepository.insertUser(user) != 1) {
            throw new MailDuplicateException("メールアドレスがすでに登録されています");
        }
        LoginResponse loginResponse = new LoginResponse();
        loginResponse.setToken(jwtService.generateToken(registerRequest.getMail()));
        return loginResponse;
    }

    private boolean isInvalidUserArguments(User user) {
        if (user == null ||
            user.getMail() == null ||
            user.getMail().isBlank() ||
            user.getName() == null ||
            user.getName().isBlank() ||
            user.getPassword() == null ||
            user.getPassword().isBlank()
        ) {
            return true;
        }

        if (user.getMail().length() > 255 ||
            !MAIL_PATTERN.matcher(user.getMail()).matches() ||
            user.getName().length() > 32 ||
            user.getPassword().length() > 72 ||
            user.getPassword().codePoints()
                .anyMatch(c -> c < 0x20 ||
                        c > 0x7e)) {
            return true;
        }
        return false;
    }
}
