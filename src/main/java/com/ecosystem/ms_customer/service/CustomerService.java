package com.ecosystem.ms_customer.service;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.ecosystem.ms_customer.entity.Customer;
import com.ecosystem.ms_customer.exception.*;
import com.ecosystem.ms_customer.resource.dto.*;
import io.awspring.cloud.dynamodb.DynamoDbTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.enhanced.dynamodb.Key;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Arrays;

@Service
public class CustomerService {

    private final String issuer;

    private final String secret;

    private final DynamoDbTemplate dynamoDb;

    private final StorageFileService storage;

    public CustomerService(@Value("${authentication.jwt.issuer}") String issuer,
                           @Value("${authentication.algorithm.secret}") String secret,
                           DynamoDbTemplate dynamoDb, StorageFileService storage) {
        this.issuer = issuer;
        this.secret = secret;
        this.dynamoDb = dynamoDb;
        this.storage = storage;
    }

    public AuthResponse auth(AuthCustomer body) {
        var customer = getCustomer(body.email());

        if (customer == null)
            throw new CustomerNotFoundException();

        if (!body.password().equals(customer.getPassword()))
            throw new PasswordsNotMatchesException();

        var algorithm = Algorithm.HMAC256(this.secret);
        var expiresIn = Instant.now().plus(Duration.ofHours(8));

        var token = JWT.create()
                .withIssuer(this.issuer)
                .withSubject(customer.getEmail())
                .withExpiresAt(expiresIn)
                .sign(algorithm);

        return new AuthResponse(token, expiresIn.toEpochMilli());
    }

    public void create(CreateCustomer body, MultipartFile file) {
        if (LocalDate.now().minusYears(18).isBefore(body.birthDate()))
            throw new MinorException();

        if (getCustomer(body.email()) != null)
            throw new CustomerAlreadyExistsException();

        var customer = Customer.fromCreateCustomer(body);

        if (file != null)
            customer.setProfilePicture(this.storage.upload(file));

        this.dynamoDb.save(customer);
    }

    public CustomerProfile profile(String email) {
        var customer = getCustomer(email);

        if (customer == null)
            throw new CustomerNotFoundException();

        return CustomerProfile.fromCustomer(customer);
    }

    public void update(String email, UpdateCustomer body) {
        var customer = getCustomer(email);

        if (customer == null)
            throw new CustomerNotFoundException();

        copyNonNullProperties(body, customer);

        this.dynamoDb.update(customer);
    }

    public void updateProfilePicture(String email, UpdateProfilePicture body) {
        var customer = getCustomer(email);

        if (customer == null)
            throw new CustomerNotFoundException();

        if (customer.getProfilePicture() != null)
            this.storage.remove(customer.getProfilePicture());

        customer.setProfilePicture(this.storage.upload(body.profilePicture()));

        this.dynamoDb.update(customer);
    }

    public void updatePassword(String email, UpdatePassword body) {
        var customer = getCustomer(email);

        if (customer == null)
            throw new CustomerNotFoundException();

        if (!body.currentPassword().equals(customer.getPassword()))
            throw new PasswordsNotMatchesException();

        customer.setPassword(body.newPassword());

        this.dynamoDb.update(customer);
    }

    private Customer getCustomer(String email) {
        return this.dynamoDb.load(Key.builder().partitionValue(email).build(), Customer.class);
    }

    public static void copyNonNullProperties(Object source, Object target) {
        Arrays.stream(source.getClass().getDeclaredFields()).forEach(sourceField -> {
            sourceField.setAccessible(true);

            try {
                var value = sourceField.get(source);

                if (value != null && !value.toString().isBlank()) {
                    var targetField = target.getClass().getDeclaredField(sourceField.getName());
                    targetField.setAccessible(true);
                    targetField.set(target, value);
                }
            } catch (IllegalAccessException | NoSuchFieldException e) {
                throw new CommonException();
            }
        });
    }
}
