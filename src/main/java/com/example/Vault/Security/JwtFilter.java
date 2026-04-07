package com.example.Vault.Security;

import com.example.Vault.model.User;
import com.example.Vault.repository.UserRepository;
import com.example.Vault.util.JwtUtil;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;

import java.io.IOException;


public class JwtFilter implements Filter {

    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;

    public JwtFilter(JwtUtil jwtUtil, UserRepository userRepository) {
        this.jwtUtil = jwtUtil;
        this.userRepository = userRepository;
    }

    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException
    {
        HttpServletRequest httpRequest= (HttpServletRequest) request;

        String header = httpRequest.getHeader("Authorization");

        if(header!=null && header.startsWith("Bearer "))
        {
            String token = header.substring(7);

            String email = jwtUtil.extractEmail(token);

            User user = userRepository.findByEmail(email).orElse(null);
            httpRequest.setAttribute("user",user);
        }
        chain.doFilter(request, response);

    }

}
