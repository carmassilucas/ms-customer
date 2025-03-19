package com.ecosystem.ms_customer.resource;

import com.ecosystem.ms_customer.resource.dto.CreateCustomer;
import com.ecosystem.ms_customer.service.CustomerService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/customers")
@AllArgsConstructor
public class CustomerResource {

    private final CustomerService service;

    @PostMapping
    public ResponseEntity<Void> create(@RequestBody @Valid CreateCustomer body) {
        this.service.create(body);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
}
