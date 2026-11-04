package com.example.bookmanagement.auth;

import com.example.bookmanagement.dto.LoginResponse;
import com.example.bookmanagement.dto.RegisterRequest;
import com.example.bookmanagement.entity.User;
import com.example.bookmanagement.exception.MailDuplicateException;
import com.example.bookmanagement.mapper.UserMapper;
import com.example.bookmanagement.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
class AuthenticationServiceRegistrationConcurrencyTests {
    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private String mail;

    @AfterEach
    void cleanUp() {
        if (mail != null) {
            jdbcTemplate.update("delete from users where mail = ?", mail);
        }
    }

    @Test
    void concurrentRegistrationForSameMailReturnsTokenOnlyToInsertedUser() throws Exception {
        mail = "registration-concurrency-" + UUID.randomUUID() + "@example.test";
        AuthenticationService service = new AuthenticationService(
                new LookupBarrierUserRepository(userMapper), authenticationManager, jwtService, passwordEncoder);
        String firstPassword = "first-password-" + UUID.randomUUID();
        String secondPassword = "second-password-" + UUID.randomUUID();

        ExecutorService executor = Executors.newFixedThreadPool(2);
        try {
            Future<RegistrationResult> first = executor.submit(() -> register(service, mail, firstPassword));
            Future<RegistrationResult> second = executor.submit(() -> register(service, mail, secondPassword));
            List<RegistrationResult> results = List.of(
                    first.get(15, TimeUnit.SECONDS), second.get(15, TimeUnit.SECONDS));

            assertEquals(1L, results.stream().filter(result -> result.response() != null).count());
            assertEquals(1L, results.stream().filter(result -> result.exception() != null).count());

            User registeredUser = userRepository.getUserByMail(mail);
            assertNotNull(registeredUser);
            for (RegistrationResult result : results) {
                if (result.response() != null) {
                    assertNotNull(result.response().getToken());
                    assertEquals(mail, jwtService.getMailFromToken(result.response().getToken()));
                    assertTrue(passwordEncoder.matches(result.password(), registeredUser.getPassword()));
                } else {
                    assertInstanceOf(MailDuplicateException.class, result.exception());
                    assertFalse(passwordEncoder.matches(result.password(), registeredUser.getPassword()));
                }
            }
        } finally {
            executor.shutdownNow();
        }
    }

    private RegistrationResult register(AuthenticationService service, String mail, String password) {
        RegisterRequest request = new RegisterRequest();
        request.setMail(mail);
        request.setName("同時登録テスト");
        request.setPlainPassword(password);
        try {
            return new RegistrationResult(password, service.register(request), null);
        } catch (MailDuplicateException e) {
            return new RegistrationResult(password, null, e);
        }
    }

    private record RegistrationResult(String password, LoginResponse response, RuntimeException exception) {
    }

    private static class LookupBarrierUserRepository extends UserRepository {
        private final CountDownLatch bothLookupsCompleted = new CountDownLatch(2);

        private LookupBarrierUserRepository(UserMapper userMapper) {
            super(userMapper);
        }

        @Override
        public User getUserByMail(String mail) {
            User user = super.getUserByMail(mail);
            bothLookupsCompleted.countDown();
            try {
                if (!bothLookupsCompleted.await(10, TimeUnit.SECONDS)) {
                    throw new IllegalStateException("同時登録の事前確認がタイムアウトしました");
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new IllegalStateException("同時登録の事前確認が中断されました", e);
            }
            return user;
        }

    }
}
