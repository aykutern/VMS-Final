package com.example.demo.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class CreateAnnouncementRequest {

    @NotNull
    private Integer projectId;

    @NotBlank
    private String title;

    @NotBlank
    private String content;
}
