package com.example.demo.services.concretes;

import com.example.demo.dto.request.LoginRequest;
import com.example.demo.dto.request.RegisterRequest;
import com.example.demo.dto.response.LoginResponse;
import com.example.demo.entities.concretes.Users;
import com.example.demo.entities.concretes.Vendor;
import com.example.demo.repositories.UserRepository;
import com.example.demo.repositories.VendorRepository;
import com.example.demo.services.abstracts.AuthService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final VendorRepository vendorRepository;

    @Override
    public LoginResponse login(LoginRequest request) {
        Users user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new EntityNotFoundException("Invalid username or password"));

        if (!user.getPassword().equals(request.getPassword())) {
            throw new EntityNotFoundException("Invalid username or password");
        }

        if (user.getIsActive() != 1) {
            throw new EntityNotFoundException("User account is deactivated");
        }

        return toLoginResponse(user, "Login successful");
    }

    @Override
    public LoginResponse register(RegisterRequest request) {
        if (userRepository.findByUsername(request.getUsername()).isPresent()) {
            throw new IllegalArgumentException("Username already exists: " + request.getUsername());
        }

        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new IllegalArgumentException("Email already exists: " + request.getEmail());
        }

        Users user = new Users();
        user.setUsername(request.getUsername());
        user.setPassword(request.getPassword());
        user.setEmail(request.getEmail());
        user.setPersonnelName(request.getPersonnelName());
        user.setPersonnelSurname(request.getPersonnelSurname());
        user.setUserType(request.getUserType());
        user.setIsActive(1);

        if (request.getVendorId() != null) {
            Vendor vendor = vendorRepository.findById(request.getVendorId())
                    .orElseThrow(() -> new EntityNotFoundException("Vendor not found: " + request.getVendorId()));
            user.setVendor(vendor);
        }

        Users savedUser = userRepository.save(user);
        return toLoginResponse(savedUser, "Registration successful");
    }

    private LoginResponse toLoginResponse(Users user, String message) {
        LoginResponse response = new LoginResponse();
        response.setId(user.getId());
        response.setUsername(user.getUsername());
        response.setEmail(user.getEmail());
        response.setPersonnelName(user.getPersonnelName());
        response.setPersonnelSurname(user.getPersonnelSurname());
        response.setUserType(user.getUserType());
        if (user.getVendor() != null) {
            response.setVendorId(user.getVendor().getId());
        }
        response.setMessage(message);
        return response;
    }
}
