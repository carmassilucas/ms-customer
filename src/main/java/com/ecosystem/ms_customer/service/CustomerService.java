package com.ecosystem.ms_customer.service;

import com.ecosystem.ms_customer.entity.Customer;
import com.ecosystem.ms_customer.exception.CustomerAlreadyExistsException;
import com.ecosystem.ms_customer.exception.CustomerNotFoundException;
import com.ecosystem.ms_customer.exception.MinorException;
import com.ecosystem.ms_customer.resource.dto.CreateCustomer;
import com.ecosystem.ms_customer.resource.dto.CustomerProfile;
import com.ecosystem.ms_customer.resource.dto.UpdateCustomer;
import io.awspring.cloud.dynamodb.DynamoDbTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.enhanced.dynamodb.Key;

import java.time.LocalDate;

@Service
public class CustomerService {

    private final DynamoDbTemplate dynamoDb;
    private final StorageFileService service;

    public CustomerService(DynamoDbTemplate dynamoDb, StorageFileService service) {
        this.dynamoDb = dynamoDb;
        this.service = service;
    }

    public void create(CreateCustomer body) {
        if (LocalDate.now().minusYears(18).isBefore(body.birthDate()))
            throw new MinorException();

        if (getCustomer(body.email()) != null)
            throw new CustomerAlreadyExistsException();

        var customer = Customer.fromCreateCustomer(body);

        if (body.profilePicture() != null)
            customer.setProfilePicture(this.service.upload(body.profilePicture()));

        this.dynamoDb.save(customer);
    }

    public CustomerProfile profile(String email) {
        var customer = getCustomer(email);

        if (customer == null)
            throw new CustomerNotFoundException();

        return CustomerProfile.fromCustomer(customer);
    }

    public Customer update(String email, UpdateCustomer body) {
        var customer = getCustomer(email);

        if (customer == null)
            throw new CustomerNotFoundException();

        BeanUtils.copyProperties(body, customer);

        return this.dynamoDb.update(customer);
    }

    private Customer getCustomer(String email) {
        return this.dynamoDb.load(Key.builder().partitionValue(email).build(), Customer.class);
    }
}
