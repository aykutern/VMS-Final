package com.example.demo.controllers;

import com.example.demo.dto.request.CreateUserRequest;
import com.example.demo.dto.request.UpdateUserRequest;
import com.example.demo.dto.response.UserResponse;
import com.example.demo.services.abstracts.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @Operation(summary = "Create user")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UserResponse create(
            @Parameter(description = "Optional user header for auditing", example = "aykut.eren") @RequestHeader(value = "X-User", required = false) String xUser,
            @Valid @RequestBody CreateUserRequest request) {
        return userService.create(request);
    }

    @Operation(summary = "Get user by id")
    @GetMapping("/{id}")
    public UserResponse getById(@PathVariable Integer id) {
        return userService.getById(id);
    }

    @Operation(summary = "List users (optional vendorId filter)")
    @GetMapping
    public List<UserResponse> getAll(@RequestParam(required = false) Integer vendorId) {
        return userService.getAll(vendorId);
    }

    @Operation(summary = "Update user")
    @PutMapping("/{id}")
    public UserResponse update(
            @PathVariable Integer id,
            @Valid @RequestBody UpdateUserRequest request) {
        return userService.update(id, request);
    }

    @Operation(summary = "Delete user")
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Integer id) {
        userService.delete(id);
    }
}
