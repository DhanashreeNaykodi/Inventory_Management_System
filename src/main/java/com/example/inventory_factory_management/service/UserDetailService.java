package com.example.inventory_factory_management.service;

import com.example.inventory_factory_management.entity.User;
import com.example.inventory_factory_management.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class UserDetailService implements UserDetailsService {

    @Autowired
    UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws RuntimeException {
        User user1 = userRepository.findByEmail(email).orElseThrow(()->new RuntimeException("User not found"));

        return new org.springframework.security.core.userdetails.User(
                user1.getEmail(),
                user1.getPassword(),
                List.of(new SimpleGrantedAuthority("ROLE_" + user1.getRole()))
        );
    }
}