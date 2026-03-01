package com.example.demo.dto.response;

import com.example.demo.enums.UserType;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
public class UserResponse {

    private Integer id;
    private String username;
    private String email;
    private Integer vendorId;
    private String vendorName;
    private String personnelName;
    private String personnelSurname;
    private UserType userType;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String createdBy;
    private String updatedBy;
    private int isActive;
}
