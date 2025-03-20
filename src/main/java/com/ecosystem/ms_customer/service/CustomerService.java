package com.ecosystem.ms_customer.service;

import com.ecosystem.ms_customer.entity.Customer;
import com.ecosystem.ms_customer.exception.CustomerAlreadyExistsException;
import com.ecosystem.ms_customer.exception.CustomerNotFoundException;
import com.ecosystem.ms_customer.exception.MinorException;
import com.ecosystem.ms_customer.resource.dto.CreateCustomer;
import com.ecosystem.ms_customer.resource.dto.CustomerProfileResponse;
import io.awspring.cloud.dynamodb.DynamoDbTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.enhanced.dynamodb.Key;

import java.time.LocalDate;

@Service
public class CustomerService {

    private final DynamoDbTemplate dynamoDb;

    public CustomerService(DynamoDbTemplate dynamoDb) {
        this.dynamoDb = dynamoDb;
    }

    public void create(CreateCustomer body) {
        if (LocalDate.now().minusYears(18).isBefore(body.birthDate()))
            throw new MinorException();

        var customer = this.dynamoDb.load(Key.builder().partitionValue(body.email()).build(), Customer.class);

        if (customer != null)
            throw new CustomerAlreadyExistsException();

        this.dynamoDb.save(Customer.fromCreateCustomer(body));
    }

    public CustomerProfileResponse profile(String email) {
        var customer = this.dynamoDb.load(Key.builder().partitionValue(email).build(), Customer.class);

        if (customer == null)
            throw new CustomerNotFoundException();

        return CustomerProfileResponse.fromCustomer(customer);
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
