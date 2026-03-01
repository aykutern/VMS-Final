package com.example.demo.services.abstracts;

import com.example.demo.dto.request.CreateUserRequest;
import com.example.demo.dto.request.UpdateUserRequest;
import com.example.demo.dto.response.UserResponse;

import java.util.List;

public interface UserService {
    UserResponse create(CreateUserRequest request);

    UserResponse getById(Integer id);

    List<UserResponse> getAll(Integer vendorId);

    UserResponse update(Integer id, UpdateUserRequest request);

    void delete(Integer id);
}
