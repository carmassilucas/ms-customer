package com.ecosystem.ms_customer.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;

public abstract class CommonException extends RuntimeException {

    public ProblemDetail toProblemDetail() {
        var problem = ProblemDetail.forStatus(HttpStatus.INTERNAL_SERVER_ERROR);
        problem.setTitle("Internal Server Error");

        return problem;
    }
}
