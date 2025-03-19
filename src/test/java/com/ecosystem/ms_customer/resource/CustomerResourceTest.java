package com.ecosystem.ms_customer.resource;

import com.ecosystem.ms_customer.entity.Customer;
import com.ecosystem.ms_customer.repository.CustomerRepository;
import com.ecosystem.ms_customer.resource.dto.CreateCustomer;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.time.LocalDate;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class CustomerResourceTest {
    @Autowired
    private MockMvc mvc;

    @Autowired
    private CustomerRepository repository;

    @BeforeEach
    public void setup() {
        this.repository.deleteAll();
    }

    @Test
    @DisplayName("Should be possible create customer.")
    void should_be_possible_create_customer() throws Exception {
        var body = new CreateCustomer(
                "name",
                "email@email.com",
                "secretpassword",
                "description",
                LocalDate.now().minusYears(18)
        );

        this.mvc.perform(MockMvcRequestBuilders.post("/customers")
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJSON(body))
        ).andExpect(MockMvcResultMatchers.status().isCreated());
    }

    @Test
    @DisplayName("Should not be possible create customer with already registered email.")
    void should_not_be_possible_create_customer_with_already_registered_email() throws Exception {
        var body = new CreateCustomer(
                "name",
                "email@email.com",
                "secretpassword",
                "description",
                LocalDate.now().minusYears(18)
        );

        var customer = Customer.builder()
                .name("name")
                .email("email@email.com")
                .password("secretpassword")
                .description("description")
                .birthDate( LocalDate.now().minusYears(18))
                .build();

        this.repository.save(customer);

        this.mvc.perform(MockMvcRequestBuilders.post("/customers")
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJSON(body))
        ).andExpect(MockMvcResultMatchers.status().isUnprocessableEntity());
    }

    private static String toJSON(Object object) throws JsonProcessingException {
        final ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        return objectMapper.writeValueAsString(object);
    }
}
