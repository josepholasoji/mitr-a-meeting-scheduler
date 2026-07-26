package com.doodle.challenge.service;

import com.doodle.challenge.dto.CreateUserCommand;
import com.doodle.challenge.dto.UserResponse;
import com.doodle.challenge.entity.User;
import com.doodle.challenge.exception.UserNotFoundException;
import com.doodle.challenge.mapper.UserMapper;
import com.doodle.challenge.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder, UserMapper userMapper) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.userMapper = userMapper;
    }

    @Transactional
    public UserResponse createUser(CreateUserCommand command) {
        User user = User.register(command.name(), command.email(), passwordEncoder.encode(command.rawPassword()));
        userRepository.save(user);
        return userMapper.toResponse(user);
    }

    public UserResponse getUser(UUID userId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new UserNotFoundException(userId));
        return userMapper.toResponse(user);
    }
}
