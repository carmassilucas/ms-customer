package com.ecosystem.ms_customer.resource;

import com.ecosystem.ms_customer.resource.dto.*;
import com.ecosystem.ms_customer.service.CustomerService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/v1/customers")
public class CustomerResource {

    private final CustomerService service;

    public CustomerResource(CustomerService service) {
        this.service = service;
    }

    @PostMapping(consumes = "multipart/form-data")
    public ResponseEntity<Void> create(@ModelAttribute @Valid CreateCustomer body, @RequestPart(value = "profilePicture",required = false) MultipartFile file) {
        this.service.create(body, file);
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

    @PatchMapping(value = "/{email}/profile-picture", consumes = "multipart/form-data")
    public ResponseEntity<Void> updateProfilePicture(@PathVariable("email") String email, @ModelAttribute @Valid UpdateProfilePicture body) {
        this.service.updateProfilePicture(email, body);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{email}/password")
    public ResponseEntity<Void> updatePassword(@PathVariable("email") String email, @RequestBody @Valid UpdatePassword body) {
        this.service.updatePassword(email, body);
        return ResponseEntity.noContent().build();
    }
}
