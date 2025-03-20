package com.ecosystem.ms_customer.resource.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record UpdateCustomer(@NotBlank String name,
                             @NotBlank String description,
                             @NotNull LocalDate birthDate) {
}
