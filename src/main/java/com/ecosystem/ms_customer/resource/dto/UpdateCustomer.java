package com.ecosystem.ms_customer.resource.dto;

import java.time.LocalDate;

public record UpdateCustomer(String name, String description, LocalDate birthDate) {
}
