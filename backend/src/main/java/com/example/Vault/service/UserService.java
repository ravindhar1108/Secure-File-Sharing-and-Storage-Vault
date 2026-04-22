package com.example.Vault.service;

import com.example.Vault.model.User;
import com.example.Vault.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;
    private BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    public String createUser(String email, String password)
    {
        if(userRepository.existsUserByEmail(email))
        {
            throw new RuntimeException("User already exists");
        }
        User user = new User();
        user.setEmail(email);
        user.setPassword(encoder.encode(password));
        userRepository.save(user);
        return "User registered Successfully!";
    }

    public User login(String email, String password)
    {
        User user = userRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("User not Found."));

        if(!encoder.matches(password,user.getPassword()))
        {
            throw new RuntimeException("Invalid Password");
        }
        return user;
    }

}
