package com.ecosystem.ms_customer.entity;

import com.ecosystem.ms_customer.resource.dto.CreateCustomer;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbAttribute;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey;

import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;
import java.time.LocalDate;

@DynamoDbBean
public class Customer implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private String email;
    private String password;
    private String name;
    private String profilePicture;
    private String description;
    private LocalDate birthDate;
    private Instant createdAt;

    public static Customer fromCreateCustomer(CreateCustomer body) {
        var customer = new Customer();

        customer.setEmail(body.email());
        customer.setPassword(body.password());
        customer.setName(body.name());
        customer.setProfilePicture(body.profilePicture());
        customer.setDescription(body.description());
        customer.setBirthDate(body.birthDate());
        customer.setCreatedAt(Instant.now());

        return customer;
    }

    @DynamoDbPartitionKey
    @DynamoDbAttribute("email")
    public String getEmail() {
        return email;
    }

    @DynamoDbAttribute("password")
    public String getPassword() {
        return password;
    }

    @DynamoDbAttribute("name")
    public String getName() {
        return name;
    }

    @DynamoDbAttribute("profile_picture")
    public String getProfilePicture() {
        return profilePicture;
    }

    @DynamoDbAttribute("description")
    public String getDescription() {
        return description;
    }

    @DynamoDbAttribute("birth_date")
    public LocalDate getBirthDate() {
        return birthDate;
    }

    @DynamoDbAttribute("created_at")
    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setProfilePicture(String profilePicture) {
        this.profilePicture = profilePicture;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setBirthDate(LocalDate birthDate) {
        this.birthDate = birthDate;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}
