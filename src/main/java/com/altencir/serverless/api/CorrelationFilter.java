package com.altencir.serverless.api;

import jakarta.annotation.Priority;
import jakarta.ws.rs.Priorities;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.container.ContainerResponseContext;
import jakarta.ws.rs.container.ContainerResponseFilter;
import jakarta.ws.rs.ext.Provider;
import java.util.UUID;
import org.jboss.logging.MDC;

@Provider
@Priority(Priorities.AUTHENTICATION - 100)
public class CorrelationFilter implements ContainerRequestFilter, ContainerResponseFilter {
    public static final String HEADER = "X-Correlation-ID";

    @Override
    public void filter(ContainerRequestContext request) {
        var supplied = request.getHeaderString(HEADER);
        MDC.put("correlationId", supplied == null || supplied.isBlank() ? UUID.randomUUID().toString() : supplied);
    }

    @Override
    public void filter(ContainerRequestContext request, ContainerResponseContext response) {
        response.getHeaders().putSingle(HEADER, MDC.get("correlationId"));
        MDC.remove("correlationId");
    }
}
