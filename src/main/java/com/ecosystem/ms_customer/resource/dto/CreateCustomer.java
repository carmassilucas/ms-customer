package com.ecosystem.ms_customer.resource.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.hibernate.validator.constraints.Length;

import java.time.LocalDate;

public record CreateCustomer(
        @NotBlank @Email String email,
        @NotBlank @Length(min = 8, max = 32) String password,
        @NotBlank String name,
        String profilePicture,
        String description,
        @NotNull LocalDate birthDate
) { }
