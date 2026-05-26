package com.example.blog.auth;

import com.example.blog.auth.dto.AuthResponse;
import com.example.blog.auth.dto.AuthUserResponse;
import com.example.blog.auth.dto.LoginRequest;
import com.example.blog.model.User;
import com.example.blog.model.UserStatus;
import com.example.blog.repository.UserRepository;
import com.example.blog.security.AuthenticatedUser;
import com.example.blog.security.JwtTokenService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenService jwtTokenService;
    private final UserRepository userRepository;

    public AuthService(
            AuthenticationManager authenticationManager,
            JwtTokenService jwtTokenService,
            UserRepository userRepository
    ) {
        this.authenticationManager = authenticationManager;
        this.jwtTokenService = jwtTokenService;
        this.userRepository = userRepository;
    }

    public AuthResponse login(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.username(), request.password())
        );

        AuthenticatedUser authenticatedUser = (AuthenticatedUser) authentication.getPrincipal();
        if (authenticatedUser.getStatus() != UserStatus.ENABLED) {
            throw new DisabledException("User is disabled");
        }

        User user = userRepository.findByUsername(authenticatedUser.getUsername())
                .orElseThrow(() -> new BadCredentialsException("Invalid username or password"));

        String token = jwtTokenService.generateToken(authenticatedUser);
        return new AuthResponse(
                token,
                new AuthUserResponse(user.getId(), user.getUsername(), user.getNickname(), user.getRole().name())
        );
    }
}
