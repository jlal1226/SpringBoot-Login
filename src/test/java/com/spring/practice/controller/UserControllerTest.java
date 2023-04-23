package com.spring.practice.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.spring.practice.domain.dto.UserJoinRequest;
import com.spring.practice.domain.dto.UserLoginRequest;
import com.spring.practice.exception.AppException;
import com.spring.practice.exception.ErrorCode;
import com.spring.practice.service.UserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest
class UserControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper; // java 객체를 json으로 만들어줌

    @MockBean
    UserService userService;


    @Test
    @DisplayName("회원가입 성공")
    @WithMockUser
    void joinSuccess() throws Exception {
        String userName = "jungrak";
        String password = "asdf1234";
        mockMvc.perform(post("/api/v1/users/join")
                        .with(csrf()) // security에서 test를 csrf로 인식하기 때문에 꼭 넣어줘야함
                        .contentType(MediaType.APPLICATION_JSON) // json으로 요청
                        .content(objectMapper.writeValueAsBytes(new UserJoinRequest(userName, password)))
                ).andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("회원가입 실패 - userName 중복")
    @WithMockUser

    void joinFail() throws Exception {
        String userName = "jungrak";
        String password = "asdf1234";

        //mocking하기
        when(userService.join(any(), any()))
                .thenThrow(new RuntimeException("해당 userId가 중복됩니다"));

        mockMvc.perform(post("/api/v1/users/join")
                        .with(csrf()) // security에서 test를 csrf로 인식하기 때문에 꼭 넣어줘야함
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsBytes(new UserJoinRequest(userName, password)))
                ).andDo(print())
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("로그인 성공")
    @WithMockUser
    void loginSuccess() throws Exception {
        String userName = "jungrak";
        String password = "asdf1234";

        when(userService.login(any(), any()))
                .thenReturn("token");

        mockMvc.perform(post("/api/v1/users/login")
                        .with(csrf()) // security에서 test를 csrf로 인식하기 때문에 꼭 넣어줘야함
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsBytes(new UserLoginRequest(userName, password)))
                ).andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("로그인 실패 - userName 없음")
    @WithMockUser
    void loginFail1() throws Exception {
        String userName = "jungrak";
        String password = "asdf1234";

        when(userService.login(any(), any()))
                .thenThrow(new AppException(ErrorCode.USERNAME_NOTFOUND, ""));

        mockMvc.perform(post("/api/v1/users/login")
                        .with(csrf()) // security에서 test를 csrf로 인식하기 때문에 꼭 넣어줘야함
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsBytes(new UserLoginRequest(userName, password)))
                ).andDo(print())
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("로그인 실패 - password 오류")
    @WithMockUser
    void loginFail2() throws Exception {
        String userName = "jungrak";
        String password = "asdf1234";

        when(userService.login(any(), any()))
                .thenThrow(new AppException(ErrorCode.INVALID_PASSWORD, ""));

        mockMvc.perform(post("/api/v1/users/login")
                        .with(csrf()) // security에서 test를 csrf로 인식하기 때문에 꼭 넣어줘야함
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsBytes(new UserLoginRequest(userName, password)))
                ).andDo(print())
                .andExpect(status().isUnauthorized());
    }
}