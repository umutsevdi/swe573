package com.hive.app.account.model.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Data
public class RegisterRequest {
    @Size(min = 4, max = 30,
            message = "Username must be between 4 and 30 characters")
    private String username;
    @Size(min = 8, max = 30,
            message = "Username must be between 8 and 30 characters")
    private String password;
    @Email
    private String email;
    @NotBlank
    private String fullName;
    @NotBlank
    private String biography;
    @JsonProperty(defaultValue = "[]")
    private List<String> interests;
}
