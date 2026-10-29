package com.example.bookmanagement.service;

import com.example.bookmanagement.entity.User;
import com.example.bookmanagement.exception.MailDuplicateException;
import com.example.bookmanagement.exception.UserArgumentInvalidException;
import com.example.bookmanagement.exception.UserNotFoundException;
import com.example.bookmanagement.form.UserForm;
import com.example.bookmanagement.repository.UserRepository;
import org.springframework.dao.DuplicateKeyException;
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
        if (isInvalidUserForm(userForm)) {
            throw new UserArgumentInvalidException("入力された値が不正です");
        }
        User user = new User();
        user.setMail(userForm.getMail());
        user.setName(userForm.getName());
        user.setPassword(passwordEncoder.encode(userForm.getPlainPassword()));
        if (userRepository.insertUser(user) == 0) {
            throw new MailDuplicateException("メールアドレスが既に登録されています");
        }
        return user;
    }

    @Transactional
    public User updateUser(UserForm userForm, int userId) {
        if (isInvalidUserForm(userForm)) {
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
        try{
            User updatedUser = userRepository.updateUser(targetUser);
            if (updatedUser == null) {
                throw new RuntimeException("ユーザの更新に失敗しました");
            }
            return updatedUser;
        } catch (DuplicateKeyException e) {
            throw new MailDuplicateException("メールアドレスが重複しています");
        }
    }

    @Transactional
    public User updateIsAdminByUserId(int userId, boolean isAdmin) {
        User targetUser = userRepository.getUserByIdForUpdate(userId);
        if (targetUser == null) {
            throw new UserNotFoundException("ユーザーが見つかりませんでした");
        }
        return userRepository.updateIsAdminByUserId(userId, isAdmin);
    }

    private boolean isInvalidUserForm(UserForm userForm) {
        if (userForm == null ||
                userForm.getMail() == null || userForm.getMail().isBlank() ||
                userForm.getName() == null || userForm.getName().isBlank() ||
                userForm.getPlainPassword() == null || userForm.getPlainPassword().isBlank()) {
            return true;
        }
        if (userForm.getMail().length() > 255 ||
            userForm.getName().length() > 32 ||
            userForm.getPlainPassword().length() > 72 ||
            userForm.getPlainPassword().codePoints().anyMatch(c -> c < 0x20 || c > 0x7e)) {
            return true;
        }
        return false;
    }
}
