package com.example.bookmanagement.service;

import com.example.bookmanagement.entity.User;
import com.example.bookmanagement.exception.UserArgumentInvalidException;
import com.example.bookmanagement.exception.UserNotFoundException;
import com.example.bookmanagement.form.UserForm;
import com.example.bookmanagement.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;


    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public User getUserByMail(String mail) {
        User user = userRepository.getUserByMail(mail);
        if (user == null) {
            throw new UserNotFoundException("指定されたメールアドレスのユーザーが見つかりませんでした");
        }
        return user;
    }

    public User getUserById(int id) {
        User user = userRepository.getUserById(id);
        if (user == null) {
            throw new UserNotFoundException("指定されたIDのユーザーが見つかりませんでした");
        }
        return user;
    }

    public User insertUser(UserForm userForm)
    {
        if (userForm == null) {
            throw new UserArgumentInvalidException("入力された値が不正です");
        }
        User user = new User();
        if (userForm.getMail() == null || userForm.getMail().isBlank() ||
                userForm.getName() == null || userForm.getName().isBlank() ||
                userForm.getPlainPassword() == null || userForm.getPlainPassword().isBlank()) {
            throw new UserArgumentInvalidException("入力された値が不正です");
        }
        user.setMail(userForm.getMail());
        user.setName(userForm.getName());
        user.setPassword(passwordEncoder.encode(userForm.getPlainPassword()));
        if (userRepository.insertUser(user) != 1) {
            throw new RuntimeException("ユーザーの登録に失敗しました");
        }
        return user;
    }

    @Transactional
    public User updateUser(UserForm userForm, int userId) {
        if (userForm == null) {
            throw new UserArgumentInvalidException("入力された値が不正です");
        }
        if (userForm.getMail() == null || userForm.getMail().isBlank() ||
                userForm.getName() == null || userForm.getName().isBlank() ||
                userForm.getPlainPassword() == null || userForm.getPlainPassword().isBlank()) {
            throw new UserArgumentInvalidException("入力された値が不正です");
        }

        User targetUser = userRepository.getUserByIdForUpdate(userId);
        if (targetUser == null) {
            throw new UserNotFoundException("ユーザーが見つかりませんでした");
        }

        targetUser.setId(userId);
        targetUser.setMail(userForm.getMail());
        targetUser.setName(userForm.getName());
        targetUser.setPassword(passwordEncoder.encode(userForm.getPlainPassword()));
        return userRepository.updateUser(targetUser);
    }

    @Transactional
    public User updateIsAdminByUserId(int userId, boolean isAdmin) {
        User targetUser = userRepository.getUserByIdForUpdate(userId);
        if (targetUser == null) {
            throw new UserNotFoundException("ユーザーが見つかりませんでした");
        }
        return userRepository.updateIsAdminByUserId(userId, isAdmin);
    }
}
