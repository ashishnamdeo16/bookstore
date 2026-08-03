package com.bookstore.user.service;
import com.bookstore.user.dto.CreateUserProfileRequest;
import com.bookstore.user.dto.UserRequestDto;
import com.bookstore.user.dto.UserResponseDto;
import com.bookstore.user.entity.User;
import com.bookstore.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * User service implementation
 *
 * @author Ashish Namdeo
 */

@Service
@RequiredArgsConstructor
@EnableMethodSecurity
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public UserResponseDto getUserById(UUID id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + id));
        return toResponse(user);
    }

    @Override
    public UserResponseDto createUser(
            CreateUserProfileRequest request
    ) {

        User user = User.builder()
                .userId(request.getUserId())
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .phoneNumber(request.getPhoneNumber())
                .dateOfBirth(request.getDateOfBirth())
                .address(request.getAddress())
                .build();

        User savedUser = userRepository.save(user);

        return toResponse(savedUser);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserResponseDto> getAllUser() {
        return userRepository.findAll().stream().map(this::toResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserResponseDto> searchUserByFirstName(String keyword) {
        return userRepository.findByFirstNameContainingIgnoreCase(keyword).stream().map(this::toResponse).toList();
    }

    @Override
    @Transactional
    public void deleteUserById(UUID id) {
         userRepository.deleteById(id);
    }

//    @Override
//    @Transactional(readOnly = true)
//    public List<UserResponseDto> getInactiveUsers() {
//        return userRepository.findByActiveFalse().stream().map(this::toResponse).toList();
//    }

    @Override
    public UserResponseDto updateUser(UserRequestDto request, UUID id) {

        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));

        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setPhoneNumber(request.getPhoneNumber());
        user.setDateOfBirth(request.getDateOfBirth());
        user.setAddress(request.getAddress());
        User updatedUser = userRepository.save(user);

        return toResponse(updatedUser);
    }

    public UserResponseDto toResponse(User user) {
        return UserResponseDto.builder()
                .userId(user.getUserId())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .dateOfBirth(user.getDateOfBirth())
                .phoneNumber(user.getPhoneNumber())
                .address(user.getAddress())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }
}
