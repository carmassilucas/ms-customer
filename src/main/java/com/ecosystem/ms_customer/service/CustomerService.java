package com.ecosystem.ms_customer.service;

import com.ecosystem.ms_customer.entity.Customer;
import com.ecosystem.ms_customer.exception.CommonException;
import com.ecosystem.ms_customer.exception.CustomerAlreadyExistsException;
import com.ecosystem.ms_customer.exception.CustomerNotFoundException;
import com.ecosystem.ms_customer.exception.MinorException;
import com.ecosystem.ms_customer.resource.dto.CreateCustomer;
import com.ecosystem.ms_customer.resource.dto.CustomerProfile;
import com.ecosystem.ms_customer.resource.dto.UpdateCustomer;
import com.ecosystem.ms_customer.resource.dto.UpdateProfilePicture;
import io.awspring.cloud.dynamodb.DynamoDbTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.enhanced.dynamodb.Key;

import java.time.LocalDate;
import java.util.Arrays;

@Service
public class CustomerService {

    private final DynamoDbTemplate dynamoDb;
    private final StorageFileService storage;

    public CustomerService(DynamoDbTemplate dynamoDb, StorageFileService storage) {
        this.dynamoDb = dynamoDb;
        this.storage = storage;
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
