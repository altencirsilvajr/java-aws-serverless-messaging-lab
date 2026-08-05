package com.altencir.serverless.api;

import io.quarkus.hibernate.validator.runtime.jaxrs.ResteasyReactiveViolationException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import java.net.URI;
import java.time.Instant;

@Provider
public class ValidationProblemMapper implements ExceptionMapper<ResteasyReactiveViolationException> {
    @Override
    public Response toResponse(ResteasyReactiveViolationException exception) {
        var detail = exception.getConstraintViolations().stream()
                .map(violation -> violation.getPropertyPath() + " " + violation.getMessage())
                .sorted().reduce((left, right) -> left + "; " + right).orElse("Invalid request");
        var problem = new ProblemDetailsMapper.Problem(
                URI.create("https://httpstatuses.io/400"), "Invalid request", 400, detail, Instant.now());
        return Response.status(400).type("application/problem+json").entity(problem).build();
    }
}
