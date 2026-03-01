package com.example.demo.dto.response;

import com.example.demo.enums.UserType;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class LoginResponse {

    private Integer id;
    private String username;
    private String email;
    private String personnelName;
    private String personnelSurname;
    private UserType userType;
    private Integer vendorId;
    private String message;
}
