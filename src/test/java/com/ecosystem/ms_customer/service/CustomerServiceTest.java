package com.ecosystem.ms_customer.service;

import com.ecosystem.ms_customer.entity.Customer;
import com.ecosystem.ms_customer.exception.CustomerAlreadyExistsException;
import com.ecosystem.ms_customer.exception.CustomerNotFoundException;
import com.ecosystem.ms_customer.exception.MinorException;
import com.ecosystem.ms_customer.exception.PasswordsNotMatchesException;
import com.ecosystem.ms_customer.resource.dto.CreateCustomer;
import com.ecosystem.ms_customer.resource.dto.UpdateCustomer;
import com.ecosystem.ms_customer.resource.dto.UpdatePassword;
import com.ecosystem.ms_customer.resource.dto.UpdateProfilePicture;
import io.awspring.cloud.dynamodb.DynamoDbTemplate;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.utils.IoUtils;

import java.io.IOException;
import java.time.LocalDate;
import java.util.Objects;

@ExtendWith(MockitoExtension.class)
public class CustomerServiceTest {

    @InjectMocks
    private CustomerService service;

    @Mock
    private StorageFileService storage;

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

    @Test
    @DisplayName("Should be possible update customer profile picture")
    void should_be_possible_update_customer_profile_picture() throws IOException {
        var customer = new Customer();
        customer.setProfilePicture("http://localhost:4566/customer-profile-picture/1742940126863.jpeg");
        customer.setEmail("email@email.com");

        var image = IoUtils.toByteArray(Objects.requireNonNull(getClass()
                .getClassLoader()
                .getResourceAsStream("upload/default-profile-picture.jpeg"))
        );

        var file = new MockMultipartFile("default-profile-picture.jpeg", image);

        Mockito.when(this.dynamoDb.load(Key.builder().partitionValue(customer.getEmail()).build(), Customer.class)).thenReturn(customer);

        Mockito.doNothing().when(this.storage).remove(customer.getProfilePicture());

        Assertions.assertDoesNotThrow(() -> this.service.updateProfilePicture(customer.getEmail(), new UpdateProfilePicture(file)));
    }

    @Test
    @DisplayName("Should not be possible update customer profile picture when customer not found")
    void should_not_be_possible_update_customer_profile_picture_when_customer_not_found() throws IOException {
        var email = "email@email.com";

        var image = IoUtils.toByteArray(Objects.requireNonNull(getClass()
                .getClassLoader()
                .getResourceAsStream("upload/default-profile-picture.jpeg"))
        );

        var file = new MockMultipartFile("default-profile-picture.jpeg", image);

        Mockito.when(this.dynamoDb.load(Key.builder().partitionValue(email).build(), Customer.class)).thenReturn(null);

        Assertions.assertThrows(CustomerNotFoundException.class, () -> this.service.updateProfilePicture(email, new UpdateProfilePicture(file)));
    }

    @Test
    @DisplayName("Should be possible update customer password")
    void should_be_possible_update_customer_password() {
        var customer = new Customer();
        customer.setEmail("email@email.com");
        customer.setPassword("secretpassword");

        var body = new UpdatePassword("secretpassword", "newpassword");

        Mockito.when(this.dynamoDb.load(Key.builder().partitionValue(customer.getEmail()).build(), Customer.class)).thenReturn(customer);

        this.service.updatePassword(customer.getEmail(), body);

        Mockito.verify(this.dynamoDb, Mockito.times(1)).update(customer);
    }

    @Test
    @DisplayName("Should not be possible update customer password when customer not found")
    void should_not_be_possible_update_customer_password_when_customer_not_found() {
        var email = "email@email.com";
        var body = new UpdatePassword("secretpassword", "newpassword");

        Mockito.when(this.dynamoDb.load(Key.builder().partitionValue(email).build(), Customer.class)).thenReturn(null);

        Assertions.assertThrows(CustomerNotFoundException.class, () -> this.service.updatePassword(email, body));
    }

    @Test
    @DisplayName("Should not be possible update customer password when password incorrect")
    void should_not_be_possible_update_customer_password_when_password_incorrect() {
        var customer = new Customer();
        customer.setEmail("email@email.com");
        customer.setPassword("secretpassword");

        var body = new UpdatePassword("incorrectpassword", "newpassword");

        Mockito.when(this.dynamoDb.load(Key.builder().partitionValue(customer.getEmail()).build(), Customer.class)).thenReturn(customer);

        Assertions.assertThrows(PasswordsNotMatchesException.class,() -> this.service.updatePassword(customer.getEmail(), body));
    }
}
