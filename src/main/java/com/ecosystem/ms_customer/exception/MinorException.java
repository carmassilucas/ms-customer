package com.ecosystem.ms_customer.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;

public class MinorException extends CommonException {

    @Override
    public ProblemDetail toProblemDetail() {
        var problem = ProblemDetail.forStatus(HttpStatus.UNPROCESSABLE_ENTITY);
        problem.setTitle("Customer must be of legal age");
        problem.setDetail("customer must be of legal age to register in the system.");

        return problem;
    }
}
