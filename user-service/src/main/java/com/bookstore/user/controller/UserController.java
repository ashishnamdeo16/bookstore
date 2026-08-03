package com.bookstore.user.controller;
import com.bookstore.user.dto.CreateUserProfileRequest;
import com.bookstore.user.dto.UserRequestDto;
import com.bookstore.user.dto.UserResponseDto;
import com.bookstore.user.service.UserServiceImpl;
import jakarta.validation.Valid;
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

    public UserController(UserServiceImpl userService){
        this.userService = userService;
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserResponseDto> getUserById(@PathVariable UUID id){
        return new ResponseEntity<>(userService.getUserById(id), HttpStatus.OK);
    }

    @PostMapping("/create")
    public UserResponseDto createUser(
            @RequestBody CreateUserProfileRequest request
    ) {
        return userService.createUser(request);
    }

    @GetMapping("/")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<UserResponseDto>> getUser(){
        return new ResponseEntity<>(userService.getAllUser(), HttpStatus.OK);
    }

    @PutMapping("/update/{id}")
    @PreAuthorize("hasRole('ADMIN') or #id.toString() == authentication.name")
    public ResponseEntity<UserResponseDto> updateById(@Valid @RequestBody UserRequestDto request, @PathVariable UUID id){
        return new ResponseEntity<>(userService.updateUser(request,id), HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> deleteById(@PathVariable UUID id){
       userService.deleteUserById(id);
       return new ResponseEntity<>("User with id : " + id + " Deleted successfully",HttpStatus.OK);
    }

//    @GetMapping("/inactive")
//    @PreAuthorize("hasRole('ADMIN')")
//    public ResponseEntity<List<UserResponseDto>> getInactiveUsers(){
//        return new ResponseEntity<>(userService.getInactiveUsers(), HttpStatus.OK);
//    }

    @GetMapping("/search")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<UserResponseDto>> searchUsers(@RequestParam String keyword){
        return new ResponseEntity<>(userService.searchUserByFirstName(keyword), HttpStatus.OK);
    }
}
