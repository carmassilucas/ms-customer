package com.ecosystem.ms_customer.service;

import com.ecosystem.ms_customer.entity.Customer;
import com.ecosystem.ms_customer.repository.CustomerRepository;
import com.ecosystem.ms_customer.resource.dto.CreateCustomer;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

@ExtendWith(MockitoExtension.class)
public class CustomerServiceTest {

    @InjectMocks
    private CustomerService service;

    @Mock
    private CustomerRepository repository;

    @Test
    @DisplayName("Should be possible create customer.")
    void should_be_possible_create_customer() {
        var body = new CreateCustomer(
                "name",
                "email@email.com",
                "secretpassword",
                "description",
                LocalDate.now().minusYears(18)
        );

        Mockito.when(this.repository.findByEmail(Mockito.any())).thenReturn(Optional.empty());

        Assertions.assertDoesNotThrow(() -> this.service.create(body));
    }

    @Test
    @DisplayName("Should not be possible create customer with already registered email.")
    void should_not_be_possible_create_customer_with_already_registered_email() {
        var body = new CreateCustomer(
                "name",
                "email@email.com",
                "secretpassword",
                "description",
                LocalDate.now().minusYears(18)
        );

        Mockito.when(this.repository.findByEmail(Mockito.any())).thenReturn(Optional.of(new Customer()));

        Assertions.assertThrows(RuntimeException.class, () -> this.service.create(body));
    }

    @Test
    @DisplayName("Should not be possible create minor customer.")
    void should_not_be_possible_create_minor_customer() {
        var body = new CreateCustomer(
                "name",
                "email@email.com",
                "secretpassword",
                "description",
                LocalDate.now().minusYears(16)
        );

        Assertions.assertThrows(RuntimeException.class, () -> this.service.create(body));
    }
}
