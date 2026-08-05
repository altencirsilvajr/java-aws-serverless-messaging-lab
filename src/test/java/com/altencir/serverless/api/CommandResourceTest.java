package com.altencir.serverless.api;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.altencir.serverless.application.CommandQueue;
import com.altencir.serverless.application.ProcessingStateStore;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

@QuarkusTest
class CommandResourceTest {
    @InjectMock ProcessingStateStore states;
    @InjectMock CommandQueue queue;

    @BeforeEach
    void allowCreation() {
        when(states.create(any())).thenReturn(true);
    }

    @Test
    void acceptsAValidCommandAndReturnsCorrelation() {
        given()
                .contentType("application/json")
                .header("X-Correlation-ID", "test-correlation")
                .body("""
                        {"messageId":"api-1","payload":"work","simulateFailure":false}
                        """)
        .when()
                .post("/api/commands")
        .then()
                .statusCode(202)
                .header("X-Correlation-ID", "test-correlation")
                .body("command.status", equalTo("ACCEPTED"))
                .body("duplicate", equalTo(false));
    }

    @Test
    void rejectsBlankCommandWithProblemDetails() {
        given()
                .contentType("application/json")
                .body("""
                        {"messageId":"","payload":"","simulateFailure":false}
                        """)
        .when()
                .post("/api/commands")
        .then()
                .statusCode(400)
                .contentType("application/problem+json")
                .body("title", equalTo("Invalid request"));
    }
}
