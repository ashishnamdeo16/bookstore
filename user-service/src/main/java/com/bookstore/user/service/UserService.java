package com.bookstore.user.service;

import com.bookstore.user.dto.CreateUserProfileRequest;
import com.bookstore.user.dto.UserRequestDto;
import com.bookstore.user.dto.UserResponseDto;

import java.util.List;
import java.util.UUID;

public interface UserService{

    UserResponseDto getUserById(UUID id);

    UserResponseDto createUser(CreateUserProfileRequest request);

    List<UserResponseDto> getAllUser();

    List<UserResponseDto> searchUserByFirstName(String keyword);

    void deleteUserById(UUID id);

    UserResponseDto updateUser(UserRequestDto request,UUID id);

}
