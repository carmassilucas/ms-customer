package com.ecosystem.ms_customer.resource.dto;

public record AuthResponse(String token, Long expiresIn) {
}
