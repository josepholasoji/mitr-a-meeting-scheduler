package com.metr.challenge.mapper;

import com.metr.challenge.dto.UserResponse;
import com.metr.challenge.entity.User;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {

    public UserResponse toResponse(User user) {
        return new UserResponse(user.getId(), user.getName(), user.getEmail(), user.getCreatedAt());
    }
}
