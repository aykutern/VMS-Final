package com.example.demo.dto.response;

import com.example.demo.enums.PersonnelTitle;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
public class ProductManagerResponse {

    private Integer id;
    private String name;
    private PersonnelTitle title;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String createdBy;
    private String updatedBy;
    private int isActive;
}
