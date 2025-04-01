package com.ecosystem.ms_customer.resource.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record AuthCustomer(@NotBlank @Email String email, @NotBlank String password) {
}
