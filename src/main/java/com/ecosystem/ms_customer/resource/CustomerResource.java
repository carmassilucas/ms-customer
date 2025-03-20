package com.ecosystem.ms_customer.resource;

import com.ecosystem.ms_customer.resource.dto.CreateCustomer;
import com.ecosystem.ms_customer.resource.dto.CustomerProfile;
import com.ecosystem.ms_customer.resource.dto.UpdateCustomer;
import com.ecosystem.ms_customer.service.CustomerService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/customers")
public class CustomerResource {

    private final CustomerService service;

    public CustomerResource(CustomerService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<Void> create(@RequestBody @Valid CreateCustomer body) {
        this.service.create(body);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @GetMapping("/{email}/profile")
    public ResponseEntity<CustomerProfile> profile(@PathVariable("email") String email) {
        return ResponseEntity.ok(this.service.profile(email));
    }

    @PutMapping("/{email}")
    public ResponseEntity<Void> update(@PathVariable("email") String email, @RequestBody UpdateCustomer body) {
        this.service.update(email, body);
        return ResponseEntity.noContent().build();
    }
}
