package com.ecosystem.ms_customer.service;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.ecosystem.ms_customer.entity.Customer;
import com.ecosystem.ms_customer.exception.*;
import com.ecosystem.ms_customer.resource.dto.*;
import io.awspring.cloud.dynamodb.DynamoDbTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.enhanced.dynamodb.Key;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Arrays;

@Service
public class CustomerService {

    private static final Logger log = LoggerFactory.getLogger(CustomerService.class);
    private final String issuer;

    private final String secret;

    private final DynamoDbTemplate dynamoDb;

    private final PasswordEncoder encoder;

    private final StorageFileService storage;

    public CustomerService(@Value("${authentication.jwt.issuer}") String issuer,
                           @Value("${authentication.algorithm.secret}") String secret,
                           DynamoDbTemplate dynamoDb, PasswordEncoder encoder, StorageFileService storage) {
        this.issuer = issuer;
        this.secret = secret;
        this.dynamoDb = dynamoDb;
        this.encoder = encoder;
        this.storage = storage;
    }

    public AuthResponse auth(AuthCustomer body) {
        var customer = getCustomer(body.email());

        if (customer == null) {
            log.warn("Não foi possível autenticar usuário, e-mail não encontrado");
            throw new CustomerNotFoundException();
        }

        if (!this.encoder.matches(body.password(), customer.getPassword())) {
            log.warn("Não foi possível autenticar usuário, senha incorreta");
            throw new PasswordsNotMatchesException();
        }

        var algorithm = Algorithm.HMAC256(this.secret);
        var expiresIn = Instant.now().plus(Duration.ofHours(8));

        var token = JWT.create()
                .withIssuer(this.issuer)
                .withSubject(customer.getEmail())
                .withExpiresAt(expiresIn)
                .sign(algorithm);

        log.info("Autenticação válida, gerando token de acesso");

        return new AuthResponse(token, expiresIn.toEpochMilli());
    }

    public void create(CreateCustomer body, MultipartFile file) {
        if (LocalDate.now().minusYears(18).isBefore(body.birthDate())) {
            log.warn("Usuário deve ser maior de idade para se cadastrar");
            throw new MinorException();
        }

        if (getCustomer(body.email()) != null) {
            log.warn("Não foi possível cadastrar usuário, e-mail já cadastrado no sistema");
            throw new CustomerAlreadyExistsException();
        }

        var customer = Customer.fromCreateCustomer(body);
        customer.setPassword(this.encoder.encode(body.password()));

        if (file != null && !file.isEmpty()) {
            log.debug("Foto de perfil no body da requisição, fazendo upload do arquivo");
            customer.setProfilePicture(this.storage.upload(file));
        }

        this.dynamoDb.save(customer);
        log.info("Usuário cadastrado na base de dados");
    }

    public CustomerProfile profile(String email) {
        var customer = getCustomer(email);

        if (customer == null) {
            log.warn("Não foi possível recuperar o perfil, usuário não encontrado");
            throw new CustomerNotFoundException();
        }

        log.debug("Usuário recuperado com sucesso da base dados");
        var profile = CustomerProfile.fromCustomer(customer);

        log.info("Retonando perfil do usuário");
        return profile;
    }

    public void update(String email, UpdateCustomer body) {
        var customer = getCustomer(email);

        if (customer == null) {
            log.warn("Não foi possível atualizar informações, usuário não encontrado");
            throw new CustomerNotFoundException();
        }

        log.debug("Copiando dados da requisição para o usuário");
        copyNonNullProperties(body, customer);

        this.dynamoDb.update(customer);
        log.info("Atualizações atualizadas com sucesso");
    }

    public void updateProfilePicture(String email, UpdateProfilePicture body) {
        var customer = getCustomer(email);

        if (customer == null) {
            log.warn("Não foi possível atualizar foto de perfil, usuário não encontrado");
            throw new CustomerNotFoundException();
        }

        if (customer.getProfilePicture() != null) {
            log.debug("Removendo foto de perfil atual do usuário");
            this.storage.remove(customer.getProfilePicture());
        }

        log.debug("Atualizando foto de perfil do usuário");
        customer.setProfilePicture(this.storage.upload(body.profilePicture()));

        this.dynamoDb.update(customer);
        log.info("Foto de perfil do usuário atualizada com sucesso");
    }

    public void updatePassword(String email, UpdatePassword body) {
        var customer = getCustomer(email);

        if (customer == null) {
            log.warn("Não foi possível atualizar senha, usuário não encontrado");
            throw new CustomerNotFoundException();
        }

        if (!this.encoder.matches(body.currentPassword(), customer.getPassword())) {
            log.warn("Não foi possível atualizar senha, senha atual incorreta");
            throw new PasswordsNotMatchesException();
        }

        customer.setPassword(body.newPassword());

        this.dynamoDb.update(customer);
        log.info("Senha do usuário atualizada com sucesso");
    }

    private Customer getCustomer(String email) {
        log.debug("Recuperando usuário da base de dados");
        return this.dynamoDb.load(Key.builder().partitionValue(email).build(), Customer.class);
    }

    public static void copyNonNullProperties(Object source, Object target) {
        log.debug("Transferindo informações não nulas de campos com mesmo nome de um objeto para o outro");
        Arrays.stream(source.getClass().getDeclaredFields()).forEach(sourceField -> {
            sourceField.setAccessible(true);

            try {
                var value = sourceField.get(source);

                if (value != null && !value.toString().isBlank()) {
                    var targetField = target.getClass().getDeclaredField(sourceField.getName());
                    targetField.setAccessible(true);
                    targetField.set(target, value);
                }
            } catch (IllegalAccessException | NoSuchFieldException e) {
                log.error("Erro ao transferir valores não nulos de um objeto para outro: {}", e.getMessage());
                throw new CommonException();
            }
        });
    }
}
