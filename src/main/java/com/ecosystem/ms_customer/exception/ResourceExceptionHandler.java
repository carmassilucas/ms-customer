package com.ecosystem.ms_customer.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ResourceExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleMethodArgumentNotValidException(MethodArgumentNotValidException exception) {
        var errors = exception.getFieldErrors().stream().map(
                field -> new InvalidParam(field.getField(), field.getDefaultMessage())
        );

        var problem = ProblemDetail.forStatus(HttpStatus.UNPROCESSABLE_ENTITY);
        problem.setTitle("Invalid params");
        problem.setProperty("invalid_params", errors);

        return problem;
    }

    @ExceptionHandler(CommonException.class)
    public ProblemDetail handleCommonException(CommonException exception) {
        return exception.toProblemDetail();
    }

    private record InvalidParam(String name, String reason) {

    }
}
