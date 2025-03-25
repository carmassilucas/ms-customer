package com.ecosystem.ms_customer.service;

import com.ecosystem.ms_customer.entity.Customer;
import com.ecosystem.ms_customer.exception.CustomerAlreadyExistsException;
import com.ecosystem.ms_customer.exception.CustomerNotFoundException;
import com.ecosystem.ms_customer.exception.MinorException;
import com.ecosystem.ms_customer.resource.dto.CreateCustomer;
import com.ecosystem.ms_customer.resource.dto.UpdateCustomer;
import io.awspring.cloud.dynamodb.DynamoDbTemplate;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.enhanced.dynamodb.Key;

import java.time.LocalDate;

@ExtendWith(MockitoExtension.class)
public class CustomerServiceTest {

    @InjectMocks
    private CustomerService service;

    @Mock
    private DynamoDbTemplate dynamoDb;

    @Test
    @DisplayName("Should be possible create customer.")
    void should_be_possible_create_customer() {
        var body = new CreateCustomer(
                "email@email.com",
                "secretpassword",
                "name",
                null,
                LocalDate.now().minusYears(18)
        );

        Mockito.when(this.dynamoDb.load(Key.builder().partitionValue(body.email()).build(), Customer.class)).thenReturn(null);

        Assertions.assertDoesNotThrow(() -> this.service.create(body, null));
    }

    @Test
    @DisplayName("Should not be possible create customer with already registered email.")
    void should_not_be_possible_create_customer_with_already_registered_email() {
        var body = new CreateCustomer(
                "email@email.com",
                "secretpassword",
                "name",
                null,
                LocalDate.now().minusYears(18)
        );

        Mockito.when(this.dynamoDb.load(Key.builder().partitionValue(body.email()).build(), Customer.class)).thenReturn(new Customer());

        Assertions.assertThrows(CustomerAlreadyExistsException.class, () -> this.service.create(body, null));
    }

    @Test
    @DisplayName("Should not be possible create minor customer.")
    void should_not_be_possible_create_minor_customer() {
        var body = new CreateCustomer(
                "email@email.com",
                "secretpassword",
                "name",
                null,
                LocalDate.now().minusYears(16)
        );

        Assertions.assertThrows(MinorException.class, () -> this.service.create(body, null));
    }

    @Test
    @DisplayName("Should be possible get profile customer")
    void should_be_possible_get_profile_customer() {
        var email = "email@email.com";

        Mockito.when(this.dynamoDb.load(Key.builder().partitionValue(email).build(), Customer.class)).thenReturn(new Customer());

        Assertions.assertNotNull(this.service.profile(email));
    }

    @Test
    @DisplayName("Should not be possible get profile customer when customer not found")
    void should_not_be_possible_get_profile_customer_when_customer_not_found() {
        var email = "email@email.com";

        Mockito.when(this.dynamoDb.load(Key.builder().partitionValue(email).build(), Customer.class)).thenReturn(null);

        Assertions.assertThrows(CustomerNotFoundException.class, () -> this.service.profile(email));
    }

    @Test
    @DisplayName("Should be possible update customer data")
    void should_be_possible_update_customer_data() {
        var email = "email@email.com";

        var body = new UpdateCustomer(
                "updated name",
                "updated description",
                LocalDate.now().minusYears(30)
        );

        Mockito.when(this.dynamoDb.load(
                Key.builder().partitionValue(email).build(), Customer.class
        )).thenReturn(new Customer());

        Assertions.assertDoesNotThrow(() -> this.service.update(email, body));
    }

    @Test
    @DisplayName("Should not be possible update customer when customer not found")
    void should_not_be_possible_update_customer_when_customer_not_found() {
        var email = "email@email.com";

        var body = new UpdateCustomer(
                "updated name",
                "updated description",
                LocalDate.now().minusYears(30)
        );

        Mockito.when(this.dynamoDb.load(Key.builder().partitionValue(email).build(), Customer.class)).thenReturn(null);

        Assertions.assertThrows(CustomerNotFoundException.class, () -> this.service.update(email, body));
    }
}
