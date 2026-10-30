package com.example.bookmanagement.auth;

import com.example.bookmanagement.entity.User;
import com.example.bookmanagement.repository.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailsService implements UserDetailsService {
    private final UserRepository userRepository;

    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String mail) throws UsernameNotFoundException {
        User user = userRepository.getUserByMail(mail);
        if (user == null) {
            throw new UsernameNotFoundException("指定されたメールアドレスのユーザーが見つかりませんでした");
        }

        return new CustomUserDetails(user);

    }
}
