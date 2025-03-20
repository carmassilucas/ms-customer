package com.ecosystem.ms_customer.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;

public class CustomerNotFoundException extends CommonException {

    @Override
    public ProblemDetail toProblemDetail() {
        var problem = ProblemDetail.forStatus(HttpStatus.NOT_FOUND);
        problem.setTitle("Customer not found");
        problem.setDetail("email address not registered in the system.");

        return problem;
    }
}
