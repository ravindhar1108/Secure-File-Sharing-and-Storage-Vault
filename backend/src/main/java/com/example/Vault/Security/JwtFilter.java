package com.example.Vault.Security;

import com.example.Vault.model.User;
import com.example.Vault.repository.UserRepository;
import com.example.Vault.util.JwtUtil;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayList;

@Component
public class JwtFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;

    public JwtFilter(JwtUtil jwtUtil, UserRepository userRepository) {
        this.jwtUtil = jwtUtil;
        this.userRepository = userRepository;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest httpRequest, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        String header = httpRequest.getHeader("Authorization");
        String tokenParam = httpRequest.getParameter("token");
        String token = null;

        if (header != null && header.startsWith("Bearer ")) {
            token = header.substring(7);
        } else if (tokenParam != null && !tokenParam.isEmpty()) {
            token = tokenParam;
        }

        if (token != null) {
            try {
                String email = jwtUtil.extractEmail(token);
                User user = userRepository.findByEmail(email).orElse(null);
                
                if (user != null) {
                    UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(user, null, new ArrayList<>());
                    SecurityContextHolder.getContext().setAuthentication(auth);
                }
            } catch (JwtException | IllegalArgumentException e) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.getWriter().write("Invalid or Expired JWT Token");
                return;
            }
        }
        filterChain.doFilter(httpRequest, response);
    }
}
