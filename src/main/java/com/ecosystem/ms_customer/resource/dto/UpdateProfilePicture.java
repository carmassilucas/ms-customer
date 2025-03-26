package com.ecosystem.ms_customer.resource.dto;

import jakarta.validation.constraints.NotNull;
import org.springframework.web.multipart.MultipartFile;

public record UpdateProfilePicture(@NotNull MultipartFile profilePicture) {
}
