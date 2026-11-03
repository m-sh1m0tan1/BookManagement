package com.example.bookmanagement.auth;

import com.example.bookmanagement.dto.RegisterRequest;
import com.example.bookmanagement.entity.User;
import com.example.bookmanagement.exception.UserArgumentInvalidException;
import com.example.bookmanagement.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

class AuthenticationServiceRegistrationValidationTests {
    @Test
    void rejectsOverlongPasswordBeforeEncodingOrInsertingUser() {
        StubUserRepository userRepository = new StubUserRepository();
        RecordingPasswordEncoder passwordEncoder = new RecordingPasswordEncoder();
        RegisterRequest request = new RegisterRequest();
        request.setMail("reader@example.test");
        request.setName("Reader");
        request.setPlainPassword("a".repeat(73));

        AuthenticationService service = new AuthenticationService(
            userRepository,
            authentication -> authentication,
            new StubJwtService(),
            passwordEncoder
        );

        assertThrows(UserArgumentInvalidException.class, () -> service.register(request));

        assertFalse(passwordEncoder.isCalled());
        assertEquals(0, userRepository.getInsertCount());
    }

    private static class StubUserRepository extends UserRepository {
        private int insertCount;

        private StubUserRepository() {
            super(null);
        }

        @Override
        public User getUserByMail(String mail) {
            return null;
        }

        @Override
        public int insertUser(User user) {
            insertCount++;
            return 1;
        }

        private int getInsertCount() {
            return insertCount;
        }
    }

    private static class RecordingPasswordEncoder implements PasswordEncoder {
        private boolean called;

        @Override
        public String encode(CharSequence rawPassword) {
            called = true;
            return "encoded";
        }

        @Override
        public boolean matches(CharSequence rawPassword, String encodedPassword) {
            return false;
        }

        private boolean isCalled() {
            return called;
        }
    }

    private static class StubJwtService extends JwtService {
        @Override
        public String generateToken(String mail) {
            return "token";
        }
    }
}
