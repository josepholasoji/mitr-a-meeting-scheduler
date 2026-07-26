package com.doodle.challenge.service;

import com.doodle.challenge.dto.CreateUserCommand;
import com.doodle.challenge.dto.UserResponse;
import com.doodle.challenge.entity.User;
import com.doodle.challenge.exception.UserNotFoundException;
import com.doodle.challenge.mapper.UserMapper;
import com.doodle.challenge.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    private final UserMapper userMapper = new UserMapper();

    private UserService service() {
        return new UserService(userRepository, passwordEncoder, userMapper);
    }

    @Test
    void createUserHashesPasswordAndSaves() {
        when(passwordEncoder.encode("plain-password")).thenReturn("hashed-password");
        CreateUserCommand command = new CreateUserCommand("Ada Lovelace", "ada@example.com", "plain-password");

        UserResponse response = service().createUser(command);

        assertThat(response.name()).isEqualTo("Ada Lovelace");
        assertThat(response.email()).isEqualTo("ada@example.com");
        verify(userRepository).save(any(User.class));
    }

    @Test
    void getUserReturnsMappedResponse() {
        User user = User.register("Ada", "ada@example.com", "hash");
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));

        UserResponse response = service().getUser(user.getId());

        assertThat(response.id()).isEqualTo(user.getId());
    }

    @Test
    void getUserThrowsWhenMissing() {
        UUID missingId = UUID.randomUUID();
        when(userRepository.findById(missingId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service().getUser(missingId)).isInstanceOf(UserNotFoundException.class);
    }
}
