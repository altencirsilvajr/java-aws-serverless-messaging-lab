package com.altencir.serverless.api;

import jakarta.validation.ConstraintViolationException;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import java.net.URI;
import java.time.Instant;

@Provider
public class ProblemDetailsMapper implements ExceptionMapper<Exception> {
    @Override
    public Response toResponse(Exception exception) {
        var status = status(exception);
        var title = switch (status) {
            case 400 -> "Invalid request";
            case 404 -> "Resource not found";
            default -> "Unexpected error";
        };
        var detail = status == 500 ? "The request could not be completed." : exception.getMessage();
        var problem = new Problem(URI.create("https://httpstatuses.io/" + status), title, status, detail, Instant.now());
        return Response.status(status).type("application/problem+json").entity(problem).build();
    }

    private static int status(Exception exception) {
        if (exception instanceof ConstraintViolationException) return 400;
        if (exception instanceof NotFoundException) return 404;
        if (exception instanceof WebApplicationException web) return web.getResponse().getStatus();
        return 500;
    }

    public record Problem(URI type, String title, int status, String detail, Instant timestamp) { }
}
