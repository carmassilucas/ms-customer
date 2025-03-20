package com.ecosystem.ms_customer.resource.dto;

import com.ecosystem.ms_customer.entity.Customer;

public record CustomerProfileResponse(String email,
                                      String password,
                                      String name,
                                      String profilePicture,
                                      String description) {

    public static CustomerProfileResponse fromCustomer(Customer customer) {
        return new CustomerProfileResponse(
                customer.getEmail(),
                customer.getPassword(),
                customer.getName(),
                customer.getProfilePicture(),
                customer.getDescription()
        );
    }
}
