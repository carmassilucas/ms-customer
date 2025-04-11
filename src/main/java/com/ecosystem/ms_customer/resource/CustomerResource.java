package com.ecosystem.ms_customer.resource;

import com.ecosystem.ms_customer.resource.dto.*;
import com.ecosystem.ms_customer.service.CustomerService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/v1/customers")
public class CustomerResource {

    private static final Logger log = LoggerFactory.getLogger(CustomerResource.class);
    private final CustomerService service;

    public CustomerResource(CustomerService service) {
        this.service = service;
    }

    @PostMapping("/auth")
    public ResponseEntity<AuthResponse> auth(@Valid @RequestBody AuthCustomer body) {
        log.info("Solicitação de autenticação recebida");
        var response = this.service.auth(body);
        log.info("Autenticação realizada com sucesso, retornando token de acesso");
        return ResponseEntity.ok(response);
    }

    @PostMapping(consumes = "multipart/form-data")
    public ResponseEntity<Void> create(@ModelAttribute @Valid CreateCustomer body,
                                       @RequestPart(value = "profilePicture", required = false) MultipartFile file) {
        log.info("Solicitação de criação de usuário recebida: {}", body);
        this.service.create(body, file);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @GetMapping("/profile")
    public ResponseEntity<CustomerProfile> profile(HttpServletRequest request) {
        log.info("Solicitação de recuperação de perfil do usuário autenticado");
        var email = request.getAttribute("customerEmail").toString();
        return ResponseEntity.ok(this.service.profile(email));
    }

    @PutMapping
    public ResponseEntity<Void> update(HttpServletRequest request, @RequestBody UpdateCustomer body) {
        log.info("Solicitação de atualização de informações do usuário recebida");
        var email = request.getAttribute("customerEmail").toString();
        this.service.update(email, body);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping(value = "/profile-picture", consumes = "multipart/form-data")
    public ResponseEntity<Void> updateProfilePicture(HttpServletRequest request,
                                                     @ModelAttribute @Valid UpdateProfilePicture body) {
        log.info("Solicitação de atualização de foto de perfil recebida");
        var email = request.getAttribute("customerEmail").toString();
        this.service.updateProfilePicture(email, body);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/password")
    public ResponseEntity<Void> updatePassword(HttpServletRequest request, @RequestBody @Valid UpdatePassword body) {
        log.info("Solicitação de atualização de senha recebida");
        var email = request.getAttribute("customerEmail").toString();
        this.service.updatePassword(email, body);
        return ResponseEntity.noContent().build();
    }
}
