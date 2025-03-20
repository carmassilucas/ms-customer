package com.ecosystem.ms_customer.resource;

import com.ecosystem.ms_customer.config.DynamoDbTestConfig;
import com.ecosystem.ms_customer.entity.Customer;
import com.ecosystem.ms_customer.resource.dto.CreateCustomer;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.awspring.cloud.dynamodb.DynamoDbTemplate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import software.amazon.awssdk.enhanced.dynamodb.Key;

import java.time.LocalDate;

@SpringBootTest
@AutoConfigureMockMvc
@Import(DynamoDbTestConfig.class)
public class CustomerResourceTest {
    @Autowired
    private MockMvc mvc;

    @Autowired
    private DynamoDbTemplate dynamoDb;

    @BeforeEach
    void setup() {
        var customer = this.dynamoDb.load(Key.builder().partitionValue("email@email.com").build(), Customer.class);

        if (customer != null)
            this.dynamoDb.delete(customer);
    }

    @Test
    @DisplayName("Should be possible create customer.")
    void should_be_possible_create_customer() throws Exception {
        var body = new CreateCustomer(
                "email@email.com",
                "secretpassword",
                "name",
                null,
                null,
                LocalDate.now().minusYears(18)
        );

        this.mvc.perform(MockMvcRequestBuilders.post("/v1/customers")
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJSON(body))
        ).andExpect(MockMvcResultMatchers.status().isCreated());
    }

    @Test
    @DisplayName("Should not be possible create customer with already registered email.")
    void should_not_be_possible_create_customer_with_already_registered_email() throws Exception {
        var body = new CreateCustomer(
                "email@email.com",
                "secretpassword",
                "name",
                null,
                null,
                LocalDate.now().minusYears(18)
        );

        var customer = Customer.fromCreateCustomer(body);

        this.dynamoDb.save(customer);

        this.mvc.perform(MockMvcRequestBuilders.post("/v1/customers")
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJSON(body))
        ).andExpect(MockMvcResultMatchers.status().isUnprocessableEntity());
    }

    @Test
    @DisplayName("Should be possible get profile customer")
    void should_be_possible_get_profile_customer() throws Exception {
        var body = new CreateCustomer(
                "email@email.com",
                "secretpassword",
                "name",
                null,
                null,
                LocalDate.now().minusYears(18)
        );

        var customer = Customer.fromCreateCustomer(body);

        this.dynamoDb.save(customer);

        this.mvc.perform(MockMvcRequestBuilders.get("/v1/customers/email@email.com/profile")
                .contentType(MediaType.APPLICATION_JSON)
        ).andExpect(MockMvcResultMatchers.status().isOk());
    }

    @Test
    @DisplayName("Should not be possible get profile customer when customer not found")
    void should_not_be_possible_get_profile_customer_when_customer_not_found() throws Exception {
        this.mvc.perform(MockMvcRequestBuilders.get("/v1/customers/email@email.com/profile")
                .contentType(MediaType.APPLICATION_JSON)
        ).andExpect(MockMvcResultMatchers.status().isNotFound());
    }

    private static String toJSON(Object object) throws JsonProcessingException {
        final ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        return objectMapper.writeValueAsString(object);
    }
}
