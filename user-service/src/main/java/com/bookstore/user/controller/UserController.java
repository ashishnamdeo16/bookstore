package com.bookstore.user.controller;

import com.bookstore.user.dto.CreateUserProfileRequest;
import com.bookstore.user.dto.UserRequestDto;
import com.bookstore.user.dto.UserResponseDto;
import com.bookstore.user.exception.AuthorizationDeniedException;
import com.bookstore.user.service.UserServiceImpl;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/user")
public class UserController {

    private final UserServiceImpl userService;
    private final String internalApiKey;

    public UserController(
            UserServiceImpl userService,
            @Value("${app.internal-api-key}") String internalApiKey
    ) {
        this.userService = userService;
        this.internalApiKey = internalApiKey;
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserResponseDto> getUserById(@PathVariable UUID id) {
        return ResponseEntity.ok(userService.getUserById(id));
    }

    /**
     * Called by auth-service during registration. Protected by a shared internal API key
     * instead of a user JWT (the user does not exist yet at this point).
     */
    @PostMapping("/create")
    public UserResponseDto createUser(
            @RequestHeader(value = "X-Internal-Api-Key", required = false) String apiKey,
            @Valid @RequestBody CreateUserProfileRequest request
    ) {
        if (apiKey == null || !apiKey.equals(internalApiKey)) {
            throw new AuthorizationDeniedException("Internal API key required");
        }
        return userService.createUser(request);
    }

    @GetMapping("/")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<UserResponseDto>> getUser() {
        return ResponseEntity.ok(userService.getAllUser());
    }

    @PutMapping("/update/{id}")
    @PreAuthorize("hasRole('ADMIN') or #id.toString() == authentication.name")
    public ResponseEntity<UserResponseDto> updateById(
            @Valid @RequestBody UserRequestDto request,
            @PathVariable UUID id
    ) {
        return ResponseEntity.ok(userService.updateUser(request, id));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> deleteById(@PathVariable UUID id) {
        userService.deleteUserById(id);
        return ResponseEntity.ok("User with id : " + id + " Deleted successfully");
    }

    @GetMapping("/search")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<UserResponseDto>> searchUsers(@RequestParam String keyword) {
        return ResponseEntity.ok(userService.searchUserByFirstName(keyword));
    }
}
