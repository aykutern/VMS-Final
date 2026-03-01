package com.example.demo.services.concretes;

import com.example.demo.dto.request.CreateUserRequest;
import com.example.demo.dto.request.UpdateUserRequest;
import com.example.demo.dto.response.UserResponse;
import com.example.demo.entities.concretes.Vendor;
import com.example.demo.entities.concretes.Users;
import com.example.demo.repositories.UserRepository;
import com.example.demo.repositories.VendorRepository;
import com.example.demo.services.abstracts.UserService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final VendorRepository vendorRepository;

    private static final int ACTIVE = 1;
    private static final int DELETED = 0;

    @Override
    public UserResponse create(CreateUserRequest request) {
        Vendor vendor = vendorRepository.findById(request.getVendorId())
                .orElseThrow(() -> new EntityNotFoundException("Vendor not found: " + request.getVendorId()));

        Users user = new Users();
        user.setUsername(request.getUsername());
        user.setPassword(request.getPassword());
        user.setEmail(request.getEmail());
        user.setVendor(vendor);
        user.setPersonnelName(request.getPersonnelName());
        user.setPersonnelSurname(request.getPersonnelSurname());
        user.setUserType(request.getUserType());
        user.setIsActive(ACTIVE);

        return toResponse(userRepository.save(user));
    }

    @Override
    public UserResponse getById(Integer id) {
        Users user = userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("User not found: " + id));

        if (user.getIsActive() != ACTIVE) {
            throw new EntityNotFoundException("User not found: " + id);
        }

        return toResponse(user);
    }

    @Override
    public List<UserResponse> getAll(Integer vendorId) {
        List<Users> userList;

        if (vendorId != null) {
            userList = userRepository.findByVendor_IdAndIsActive(vendorId, ACTIVE);
        } else {
            userList = userRepository.findByIsActive(ACTIVE);
        }

        return userList.stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    public UserResponse update(Integer id, UpdateUserRequest request) {
        Users user = userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("User not found: " + id));

        if (user.getIsActive() != ACTIVE) {
            throw new EntityNotFoundException("User not found: " + id);
        }

        Vendor vendor = vendorRepository.findById(request.getVendorId())
                .orElseThrow(() -> new EntityNotFoundException("Vendor not found: " + request.getVendorId()));

        user.setEmail(request.getEmail());
        user.setVendor(vendor);
        user.setPersonnelName(request.getPersonnelName());
        user.setPersonnelSurname(request.getPersonnelSurname());
        user.setUserType(request.getUserType());

        return toResponse(userRepository.save(user));
    }

    @Override
    public void delete(Integer id) {
        Users user = userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("User not found: " + id));

        if (user.getIsActive() == ACTIVE) {
            user.setIsActive(DELETED);
            userRepository.save(user);
        }
    }

    private UserResponse toResponse(Users user) {
        UserResponse response = new UserResponse();
        response.setId(user.getId());
        response.setUsername(user.getUsername());
        response.setEmail(user.getEmail());
        if (user.getVendor() != null) {
            response.setVendorId(user.getVendor().getId());
            response.setVendorName(user.getVendor().getVendorName());
        }
        response.setPersonnelName(user.getPersonnelName());
        response.setPersonnelSurname(user.getPersonnelSurname());
        response.setUserType(user.getUserType());
        response.setCreatedAt(user.getCreatedAt());
        response.setUpdatedAt(user.getUpdatedAt());
        response.setCreatedBy(user.getCreatedBy());
        response.setUpdatedBy(user.getUpdatedBy());
        response.setIsActive(user.getIsActive());
        return response;
    }
}
