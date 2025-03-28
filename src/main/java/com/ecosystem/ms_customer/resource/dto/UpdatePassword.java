package com.ecosystem.ms_customer.resource.dto;

import jakarta.validation.constraints.NotBlank;

public record UpdatePassword(@NotBlank String currentPassword, @NotBlank String newPassword) {
}
