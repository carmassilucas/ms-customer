package com.ecosystem.ms_customer.resource.dto;

import com.ecosystem.ms_customer.entity.Customer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public record CustomerProfile(String email,
                              String name,
                              String profilePicture,
                              String description) {

    private static final Logger log = LoggerFactory.getLogger(CustomerProfile.class);

    public static CustomerProfile fromCustomer(Customer customer) {
        log.debug("Criando perfil do usuário por meio de suas informações");
        return new CustomerProfile(
                customer.getEmail(),
                customer.getName(),
                customer.getProfilePicture(),
                customer.getDescription()
        );
    }
}
