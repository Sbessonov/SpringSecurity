package com.example.SpringSecurity.configuration;

import com.example.SpringSecurity.entity.UserEntity;
import com.example.SpringSecurity.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
@RequiredArgsConstructor
public class UserDetailedService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String name) throws UsernameNotFoundException {
        UserEntity entity = userRepository.findByEmail(name).orElseThrow(
                () -> new UsernameNotFoundException("User: " + name +  " not found")
        );
        return new User(entity.getName(), entity.getPassword(), Collections.emptyList());
    }
}
