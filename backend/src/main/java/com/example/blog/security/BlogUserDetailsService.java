package com.example.blog.security;

import com.example.blog.repository.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class BlogUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    public BlogUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return userRepository.findByUsername(username)
                .map(AuthenticatedUser::from)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
    }
}
