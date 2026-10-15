package com.example.bookmanagement.repository;

import com.example.bookmanagement.entity.User;
import com.example.bookmanagement.mapper.UserMapper;
import org.springframework.stereotype.Repository;

@Repository
public class UserRepository {
    private final UserMapper userMapper;

    public UserRepository(UserMapper userMapper) {
        this.userMapper = userMapper;
    }

    public User getUserByMail(String mail) {
        return userMapper.getUserByMail(mail);
    }

    public User getUserById(int id) {
        return userMapper.getUserById(id);
    }

    public User getUserByIdForUpdate(int id) {
        return userMapper.getUserByIdForUpdate(id);
    }

    public int insertUser(User user) {
        return userMapper.insertUser(user);
    }

    public User updateUser(User user) {
        if (userMapper.updateUser(user) == 1) {
            return user;
        }
        return null;
    }

    public User updateIsAdminByUserId(int id, boolean isAdmin) {
        if (userMapper.updateIsAdminByUserId(id, isAdmin) == 1) {
            return getUserById(id);
        }
        return null;
    }
}
