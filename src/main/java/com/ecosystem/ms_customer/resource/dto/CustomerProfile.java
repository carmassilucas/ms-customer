package com.ecosystem.ms_customer.resource.dto;

import com.ecosystem.ms_customer.entity.Customer;

public record CustomerProfile(String email,
                              String password,
                              String name,
                              String profilePicture,
                              String description) {

    public static CustomerProfile fromCustomer(Customer customer) {
        return new CustomerProfile(
                customer.getEmail(),
                customer.getPassword(),
                customer.getName(),
                customer.getProfilePicture(),
                customer.getDescription()
        );
    }
}
