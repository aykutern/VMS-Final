package com.example.demo.dto.request;

import com.example.demo.enums.PersonnelTitle;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class UpdateProductManagerRequest {

    @NotBlank
    private String name;

    @NotNull
    private PersonnelTitle title;
}
