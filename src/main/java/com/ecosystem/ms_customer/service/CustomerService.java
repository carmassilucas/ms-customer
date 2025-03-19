package com.ecosystem.ms_customer.service;

import com.ecosystem.ms_customer.entity.Customer;
import com.ecosystem.ms_customer.exception.CustomerAlreadyExistsException;
import com.ecosystem.ms_customer.exception.MinorException;
import com.ecosystem.ms_customer.repository.CustomerRepository;
import com.ecosystem.ms_customer.resource.dto.CreateCustomer;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
@AllArgsConstructor
public class CustomerService {

    private final CustomerRepository repository;

    public void create(CreateCustomer body) {
        if (LocalDate.now().minusYears(18).isBefore(body.birthDate())) {
            throw new MinorException();
        }

        this.repository.findByEmail(body.email()).ifPresent(customer -> {
            throw new CustomerAlreadyExistsException();
        });

        var customer = Customer.builder()
                .name(body.name())
                .email(body.email())
                .password(body.password())
                .description(body.description())
                .birthDate(body.birthDate())
                .build();

        this.repository.save(customer);
    }

}
