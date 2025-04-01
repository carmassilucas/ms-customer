package com.ecosystem.ms_customer.resource;

import com.ecosystem.ms_customer.config.AwsConfigTest;
import com.ecosystem.ms_customer.entity.Customer;
import com.ecosystem.ms_customer.resource.dto.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.awspring.cloud.dynamodb.DynamoDbTemplate;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.utils.IoUtils;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Objects;

@SpringBootTest
@AutoConfigureMockMvc
@Import(AwsConfigTest.class)
public class CustomerResourceTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private DynamoDbTemplate dynamoDb;

    @Autowired
    private S3Client s3Client;

    @Value("${aws.localstack.s3.bucket-name}")
    private String bucket;

    @BeforeEach
    void setup() {
        var customer = this.dynamoDb.load(Key.builder().partitionValue("email@email.com").build(), Customer.class);

        if (customer == null) return;

        if (customer.getProfilePicture() != null) {
            var name = Arrays.stream(customer.getProfilePicture().split("/")).toList().getLast();

            var request = DeleteObjectRequest.builder().bucket(bucket).key(name).build();
            this.s3Client.deleteObject(request);
        }

        this.dynamoDb.delete(customer);
    }

    @Test
    @DisplayName("Should be possible to create a customer with profile picture")
    void should_be_possible_create_customer_with_profile_picture() throws Exception {
        var image = IoUtils.toByteArray(Objects.requireNonNull(getClass()
                .getClassLoader()
                .getResourceAsStream("upload/default-profile-picture.jpeg"))
        );

        var file = new MockMultipartFile("default-profile-picture.jpeg",image);

        var body = new CreateCustomer(
                "email@email.com",
                "secretpassword",
                "name",
                null,
                LocalDate.now().minusYears(18)
        );

        this.mvc.perform(MockMvcRequestBuilders.multipart("/v1/customers")
                        .file("profilePicture", file.getBytes())
                        .param("email", body.email())
                        .param("password", body.password())
                        .param("name", body.name())
                        .param("birthDate", body.birthDate().toString())
                        .contentType(MediaType.MULTIPART_FORM_DATA)
        ).andExpect(MockMvcResultMatchers.status().isCreated());
    }

    @Test
    @DisplayName("Should be possible to create a customer without profile picture")
    void should_be_possible_create_customer_without_profile_picture() throws Exception {
        var body = new CreateCustomer(
                "email@email.com",
                "secretpassword",
                "name",
                null,
                LocalDate.now().minusYears(18)
        );

        this.mvc.perform(MockMvcRequestBuilders.multipart("/v1/customers")
                        .param("email", body.email())
                        .param("password", body.password())
                        .param("name", body.name())
                        .param("description", body.description())
                        .param("birthDate", body.birthDate().toString())
                        .contentType(MediaType.MULTIPART_FORM_DATA)
        ).andExpect(MockMvcResultMatchers.status().isCreated());
    }


    @Test
    @DisplayName("Should not be possible create customer with already registered email")
    void should_not_be_possible_create_customer_with_already_registered_email() throws Exception {
        var body = new CreateCustomer(
                "email@email.com",
                "secretpassword",
                "name",
                null,
                LocalDate.now().minusYears(18)
        );

        var customer = Customer.fromCreateCustomer(body);

        this.dynamoDb.save(customer);

        this.mvc.perform(MockMvcRequestBuilders.multipart("/v1/customers")
                        .param("email", body.email())
                        .param("password", body.password())
                        .param("name", body.name())
                        .param("description", body.description())
                        .param("birthDate", body.birthDate().toString())
                        .contentType(MediaType.MULTIPART_FORM_DATA)
        ).andExpect(MockMvcResultMatchers.status().isUnprocessableEntity());
    }

    @Test
    @DisplayName("Should not be possible create customer with null data values")
    void should_not_be_possible_create_customer_with_null_data_values() throws Exception {
        var body = new CreateCustomer(
                "email@email.com",
                null,
                "name",
                null,
                LocalDate.now().minusYears(18)
        );

        this.mvc.perform(MockMvcRequestBuilders.multipart("/v1/customers")
                        .param("email", body.email())
                        .param("password", body.password())
                        .param("name", body.name())
                        .param("description", body.description())
                        .param("birthDate", body.birthDate().toString())
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

    @Test
    @DisplayName("Should be possible update customer data")
    void should_be_possible_update_customer_data() throws Exception {
        var body = new UpdateCustomer(
                "updated name",
                "updated description",
                LocalDate.now().minusYears(30)
        );

        var customer = Customer.fromCreateCustomer(new CreateCustomer(
                "email@email.com",
                "secretpassword",
                "name",
                null,
                LocalDate.now().minusYears(18)
        ));

        this.dynamoDb.save(customer);

        this.mvc.perform(MockMvcRequestBuilders.put("/v1/customers/email@email.com")
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJSON(body))
        ).andExpect(MockMvcResultMatchers.status().isNoContent());

        var response = this.mvc.perform(MockMvcRequestBuilders.get("/v1/customers/email@email.com/profile")
                .contentType(MediaType.APPLICATION_JSON)
        ).andReturn().getResponse().getContentAsString();

        var profile = fromJSON(response);

        Assertions.assertEquals(body.name(), profile.name());
    }

    @Test
    @DisplayName("Should not be possible update customer when customer not found")
    void should_not_be_possible_update_customer_when_customer_not_found() throws Exception {
        var body = new UpdateCustomer(
                "updated name",
                "updated description",
                LocalDate.now().minusYears(30)
        );

        this.mvc.perform(MockMvcRequestBuilders.put("/v1/customers/email@email.com")
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJSON(body))
        ).andExpect(MockMvcResultMatchers.status().isNotFound());
    }

    @Test
    @DisplayName("Should be possible update customer data with null data values")
    void should_be_possible_update_customer_data_with_null_data_values() throws Exception {
        var customer = Customer.fromCreateCustomer(new CreateCustomer(
                "email@email.com",
                "secretpassword",
                "name",
                null,
                LocalDate.now().minusYears(18)
        ));

        this.dynamoDb.save(customer);

        this.mvc.perform(MockMvcRequestBuilders.put("/v1/customers/email@email.com")
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJSON(new UpdateCustomer(null,null, null)))
        ).andExpect(MockMvcResultMatchers.status().isNoContent());

        var response = this.mvc.perform(MockMvcRequestBuilders.get("/v1/customers/email@email.com/profile")
                .contentType(MediaType.APPLICATION_JSON)
        ).andReturn().getResponse().getContentAsString();

        var profile = fromJSON(response);

        Assertions.assertEquals(customer.getName(), profile.name());
    }

    @Test
    @DisplayName("Should be possible update customer profile picture when already has profile picture")
    void should_be_possible_update_customer_profile_picture_when_already_has_profile_picture() throws Exception {
        var image = IoUtils.toByteArray(Objects.requireNonNull(getClass()
                .getClassLoader()
                .getResourceAsStream("upload/default-profile-picture.jpeg"))
        );
        var file = new MockMultipartFile("default-profile-picture.jpeg",image);

        var name = Instant.now().toEpochMilli() + ".jpeg";
        var request = PutObjectRequest.builder().bucket(this.bucket).key(name).build();

        this.s3Client.putObject(request, RequestBody.fromBytes(file.getBytes()));

        var customer = Customer.fromCreateCustomer(new CreateCustomer(
                "email@email.com",
                "secretpassword",
                "name",
                null,
                LocalDate.now().minusYears(18)
        ));
        customer.setProfilePicture("http://localhost:4566/customer-profile-picture/" + name);

        this.dynamoDb.save(customer);

        this.mvc.perform(MockMvcRequestBuilders.multipart(HttpMethod.PATCH, "/v1/customers/email@email.com/profile-picture")
                .file("profilePicture", file.getBytes())
                .contentType(MediaType.MULTIPART_FORM_DATA)
        ).andExpect(MockMvcResultMatchers.status().isNoContent());

        var response = this.mvc.perform(MockMvcRequestBuilders.get("/v1/customers/email@email.com/profile")
                .contentType(MediaType.APPLICATION_JSON)
        ).andReturn().getResponse().getContentAsString();

        var profile = fromJSON(response);

        Assertions.assertNotEquals(customer.getProfilePicture(), profile.profilePicture());
    }

    @Test
    @DisplayName("Should be possible update customer profile picture when they don't have a profile picture")
    void should_be_possible_update_customer_profile_picture_when_they_dont_have_profile_picture() throws Exception {
        var image = IoUtils.toByteArray(Objects.requireNonNull(getClass()
                .getClassLoader()
                .getResourceAsStream("upload/default-profile-picture.jpeg"))
        );
        var file = new MockMultipartFile("default-profile-picture.jpeg",image);

        var customer = Customer.fromCreateCustomer(new CreateCustomer(
                "email@email.com",
                "secretpassword",
                "name",
                null,
                LocalDate.now().minusYears(18)
        ));

        this.dynamoDb.save(customer);

        var response = this.mvc.perform(MockMvcRequestBuilders.get("/v1/customers/email@email.com/profile")
                .contentType(MediaType.APPLICATION_JSON)
        ).andReturn().getResponse().getContentAsString();

        var profile = fromJSON(response);

        Assertions.assertNull(profile.profilePicture());

        this.mvc.perform(MockMvcRequestBuilders.multipart(HttpMethod.PATCH, "/v1/customers/email@email.com/profile-picture")
                .file("profilePicture", file.getBytes())
                .contentType(MediaType.MULTIPART_FORM_DATA)
        ).andExpect(MockMvcResultMatchers.status().isNoContent());

        response = this.mvc.perform(MockMvcRequestBuilders.get("/v1/customers/email@email.com/profile")
                .contentType(MediaType.APPLICATION_JSON)
        ).andReturn().getResponse().getContentAsString();

        profile = fromJSON(response);

        Assertions.assertNotNull(profile.profilePicture());
    }

    @Test
    @DisplayName("Should not be possible update customer profile picture when customer not found")
    void should_not_be_possible_update_customer_profile_picture_when_customer_not_found() throws Exception {
        var image = IoUtils.toByteArray(Objects.requireNonNull(getClass()
                .getClassLoader()
                .getResourceAsStream("upload/default-profile-picture.jpeg"))
        );
        var file = new MockMultipartFile("default-profile-picture.jpeg",image);

        this.mvc.perform(MockMvcRequestBuilders.multipart(HttpMethod.PATCH, "/v1/customers/email@email.com/profile-picture")
                .file("profilePicture", file.getBytes())
                .contentType(MediaType.MULTIPART_FORM_DATA)
        ).andExpect(MockMvcResultMatchers.status().isNotFound());
    }

    @Test
    @DisplayName("Should be possible update customer password")
    void should_be_possible_update_customer_password() throws Exception {
        var customer = Customer.fromCreateCustomer(new CreateCustomer(
                "email@email.com",
                "secretpassword",
                "name",
                null,
                LocalDate.now().minusYears(18)
        ));

        this.dynamoDb.save(customer);

        this.mvc.perform(MockMvcRequestBuilders.patch("/v1/customers/email@email.com/password")
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJSON(new UpdatePassword("secretpassword", "newpassword")))
        ).andExpect(MockMvcResultMatchers.status().isNoContent());
    }

    @Test
    @DisplayName("Should not be possible update customer password when customer not found")
    void should_not_be_possible_update_customer_password_when_customer_not_found() throws Exception {
        this.mvc.perform(MockMvcRequestBuilders.patch("/v1/customers/email@email.com/password")
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJSON(new UpdatePassword("secretpassword", "newpassword")))
        ).andExpect(MockMvcResultMatchers.status().isNotFound());
    }

    @Test
    @DisplayName("Should not be possible update customer password when password incorrect")
    void should_not_be_possible_update_customer_password_when_password_incorrect() throws Exception {
        var customer = Customer.fromCreateCustomer(new CreateCustomer(
                "email@email.com",
                "secretpassword",
                "name",
                null,
                LocalDate.now().minusYears(18)
        ));

        this.dynamoDb.save(customer);

        this.mvc.perform(MockMvcRequestBuilders.patch("/v1/customers/email@email.com/password")
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJSON(new UpdatePassword("incorrectpassword", "newpassword")))
        ).andExpect(MockMvcResultMatchers.status().isUnprocessableEntity());
    }

    @Test
    @DisplayName("Should be possible authenticate customer")
    void should_be_possible_authenticate_customer() throws Exception {
        var customer = Customer.fromCreateCustomer(new CreateCustomer(
                "email@email.com",
                "secretpassword",
                "name",
                null,
                LocalDate.now().minusYears(18)
        ));

        this.dynamoDb.save(customer);

        this.mvc.perform(MockMvcRequestBuilders.post("/v1/customers/auth")
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJSON(new AuthCustomer(customer.getEmail(), customer.getPassword())))
        ).andExpect(MockMvcResultMatchers.status().isOk());
    }

    @Test
    @DisplayName("Should not be possible authenticate customer when customer not found")
    void should_not_be_possible_authenticate_customer_when_customer_not_found() throws Exception {
        this.mvc.perform(MockMvcRequestBuilders.post("/v1/customers/auth")
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJSON(new AuthCustomer("email@email.com", "secretpassword")))
        ).andExpect(MockMvcResultMatchers.status().isNotFound());
    }

    @Test
    @DisplayName("Should not be possible authenticate customer when password is incorrect")
    void should_not_be_possible_authenticate_customer_when_password_is_incorrect() throws Exception {
        var customer = Customer.fromCreateCustomer(new CreateCustomer(
                "email@email.com",
                "secretpassword",
                "name",
                null,
                LocalDate.now().minusYears(18)
        ));

        this.dynamoDb.save(customer);

        this.mvc.perform(MockMvcRequestBuilders.post("/v1/customers/auth")
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJSON(new AuthCustomer(customer.getEmail(), "incorrectpassword")))
        ).andExpect(MockMvcResultMatchers.status().isUnprocessableEntity());
    }

    private static String toJSON(Object object) throws JsonProcessingException {
        final ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        return objectMapper.writeValueAsString(object);
    }

    public static CustomerProfile fromJSON(String json) throws JsonProcessingException {
        final ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.readValue(json, CustomerProfile.class);
    }
}
