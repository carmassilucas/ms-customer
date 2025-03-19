package com.ecosystem.ms_customer.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;

public class CustomerAlreadyExistsException extends CommonException {

    @Override
    public ProblemDetail toProblemDetail() {
        var problem = ProblemDetail.forStatus(HttpStatus.UNPROCESSABLE_ENTITY);
        problem.setTitle("Email Already Exists");
        problem.setDetail("email address already registered in the system.");

        return problem;
    }
}
