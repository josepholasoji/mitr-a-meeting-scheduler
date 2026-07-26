package com.doodle.challenge.controller;

import com.doodle.challenge.WebSliceTestConfig;
import com.doodle.challenge.dto.CreateUserRequest;
import com.doodle.challenge.dto.UserResponse;
import com.doodle.challenge.exception.GlobalExceptionHandler;
import com.doodle.challenge.exception.UserNotFoundException;
import com.doodle.challenge.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
@ContextConfiguration(classes = WebSliceTestConfig.class)
@Import({UserController.class, GlobalExceptionHandler.class})
@AutoConfigureMockMvc(addFilters = false)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private UserService userService;

    @Test
    void createUserSucceeds() throws Exception {
        CreateUserRequest request = new CreateUserRequest("Ada Lovelace", "ada@example.com", "correct-horse-battery");
        UUID userId = UUID.randomUUID();
        when(userService.createUser(any())).thenReturn(new UserResponse(userId, "Ada Lovelace", "ada@example.com", Instant.now()));

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(userId.toString()))
                .andExpect(jsonPath("$.email").value("ada@example.com"));
    }

    @Test
    void createUserRejectsInvalidEmail() throws Exception {
        CreateUserRequest request = new CreateUserRequest("Ada Lovelace", "not-an-email", "correct-horse-battery");

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createUserRejectsShortPassword() throws Exception {
        CreateUserRequest request = new CreateUserRequest("Ada Lovelace", "ada@example.com", "short");

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getUserReturnsNotFoundWhenMissing() throws Exception {
        UUID userId = UUID.randomUUID();
        when(userService.getUser(userId)).thenThrow(new UserNotFoundException(userId));

        mockMvc.perform(get("/users/{id}", userId)).andExpect(status().isNotFound());
    }
}
