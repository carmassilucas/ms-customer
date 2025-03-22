package com.ecosystem.ms_customer.resource.dto;

import com.ecosystem.ms_customer.entity.Customer;

public record CustomerProfile(String email,
                              String name,
                              String profilePicture,
                              String description) {

    public static CustomerProfile fromCustomer(Customer customer) {
        return new CustomerProfile(
                customer.getEmail(),
                customer.getName(),
                customer.getProfilePicture(),
                customer.getDescription()
        );
    }
}
