package com.example.bookmanagement.service;

import com.example.bookmanagement.entity.User;
import com.example.bookmanagement.exception.UserArgumentInvalidException;
import com.example.bookmanagement.form.UserForm;
import com.example.bookmanagement.repository.UserRepository;
import com.example.bookmanagement.security.SecurityConfig;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class UserServicePasswordTests {
    private AnnotationConfigApplicationContext context;
    private RecordingUserRepository userRepository;
    private UserService userService;
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
        userRepository = new RecordingUserRepository();
        context = new AnnotationConfigApplicationContext();
        context.registerBean(UserRepository.class, () -> userRepository);
        context.register(SecurityConfig.class, UserService.class);
        context.refresh();
        userService = context.getBean(UserService.class);
        passwordEncoder = context.getBean(PasswordEncoder.class);
    }

    @AfterEach
    void tearDown() {
        context.close();
    }

    @Test
    void insertUserSavesPasswordHashMatchingSubmittedPassword() {
        UserForm form = createForm("registration-test-input-42");
        userService.insertUser(form);

        assertTrue(passwordEncoder.matches(form.getPlainPassword(), userRepository.savedUser.getPassword()));
    }

    @Test
    void updateUserPreservesIdAndAdminStatusWhileHashingPassword() {
        UserForm form = createForm("update-test-input-43");
        User existingUser = new User();
        existingUser.setId(42);
        existingUser.setAdmin(true);
        userRepository.existingUser = existingUser;

        User updatedUser = userService.updateUser(form, 42);

        assertTrue(passwordEncoder.matches(form.getPlainPassword(), userRepository.savedUser.getPassword()));
        assertEquals(42, updatedUser.getId());
        assertTrue(updatedUser.isAdmin());
    }

    @Test
    void insertUserRejectsNullForm() {
        assertThrows(UserArgumentInvalidException.class, () -> userService.insertUser(null));
    }

    @Test
    void updateUserRejectsNullForm() {
        assertThrows(UserArgumentInvalidException.class, () -> userService.updateUser(null, 42));
    }

    @Test
    void insertUserRejectsWhitespaceOnlyMailOrName() {
        assertThrows(UserArgumentInvalidException.class,
                () -> userService.insertUser(createForm("   ", "sample", "registration-test-input-44")));
        assertThrows(UserArgumentInvalidException.class,
                () -> userService.insertUser(createForm("sample@example.invalid", "   ", "registration-test-input-45")));
    }

    @Test
    void updateUserRejectsWhitespaceOnlyMailOrName() {
        User existingUser = new User();
        existingUser.setId(42);
        userRepository.existingUser = existingUser;

        assertThrows(UserArgumentInvalidException.class,
                () -> userService.updateUser(createForm("   ", "sample", "update-test-input-44"), 42));
        assertThrows(UserArgumentInvalidException.class,
                () -> userService.updateUser(createForm("sample@example.invalid", "   ", "update-test-input-45"), 42));
    }

    @Test
    void insertUserRejectsWhitespaceOnlyPassword() {
        assertThrows(UserArgumentInvalidException.class, () -> userService.insertUser(createForm("   ")));
    }

    @Test
    void updateUserRejectsWhitespaceOnlyPassword() {
        User existingUser = new User();
        existingUser.setId(42);
        userRepository.existingUser = existingUser;

        assertThrows(UserArgumentInvalidException.class, () -> userService.updateUser(createForm("   "), 42));
    }

    private UserForm createForm(String plainPassword) {
        return createForm("sample@example.invalid", "sample", plainPassword);
    }

    private UserForm createForm(String mail, String name, String plainPassword) {
        UserForm form = new UserForm();
        form.setMail(mail);
        form.setName(name);
        form.setPlainPassword(plainPassword);
        return form;
    }

    private static class RecordingUserRepository extends UserRepository {
        private User savedUser;
        private User existingUser;

        RecordingUserRepository() {
            super(null);
        }

        @Override
        public int insertUser(User user) {
            savedUser = user;
            return 1;
        }

        @Override
        public User getUserByIdForUpdate(int id) {
            return existingUser != null && existingUser.getId() == id ? existingUser : null;
        }

        @Override
        public User updateUser(User user) {
            savedUser = user;
            return user;
        }
    }
}
