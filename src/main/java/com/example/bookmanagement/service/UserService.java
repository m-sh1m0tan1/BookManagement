package com.example.bookmanagement.service;

import com.example.bookmanagement.entity.User;
import com.example.bookmanagement.exception.UserArgumentInvalidException;
import com.example.bookmanagement.exception.UserNotFoundException;
import com.example.bookmanagement.form.UserForm;
import com.example.bookmanagement.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {
    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
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
        User user = new User();
        if (userForm.getMail() == null || userForm.getMail().isEmpty() ||
                userForm.getName() == null || userForm.getName().isEmpty() ||
                userForm.getPlainPassword() == null || userForm.getPlainPassword().isEmpty()) {
            throw new UserArgumentInvalidException("入力された値が不正です");
        }
        user.setMail(userForm.getMail());
        user.setName(userForm.getName());
        user.setPassword("" // ここはSecurityConfigでパスワードハッシュ化アルゴリズムを設定してから書く
        );
        if (userRepository.insertUser(user) != 1) {
            throw new RuntimeException("ユーザーの登録に失敗しました");
        }
        return user;
    }

    @Transactional
    public User updateUser(UserForm userForm, int userId) {
        User user = new User();
        if (userForm.getMail() == null || userForm.getMail().isEmpty() ||
                userForm.getName() == null || userForm.getName().isEmpty() ||
                userForm.getPlainPassword() == null || userForm.getPlainPassword().isEmpty()) {
            throw new UserArgumentInvalidException("入力された値が不正です");
        }

        User targetUser = userRepository.getUserByIdForUpdate(userId);
        if (targetUser == null) {
            throw new UserNotFoundException("ユーザーが見つかりませんでした");
        }

        user.setMail(userForm.getMail());
        user.setName(userForm.getName());
        user.setPassword("" // ここはSecurityConfigでパスワードハッシュ化アルゴリズムを設定してから書く
        );
        return userRepository.updateUser(user);
    }

    public User updateIsAdminByUserId(int userId, boolean isAdmin) {
        User targetUser = userRepository.getUserByIdForUpdate(userId);
        if (targetUser == null) {
            throw new UserNotFoundException("ユーザーが見つかりませんでした");
        }
        return userRepository.updateIsAdminByUserId(userId, isAdmin);
    }
}
