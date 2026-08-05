package com.altencir.serverless.api;

import com.altencir.serverless.application.CommandNotFoundException;
import com.altencir.serverless.application.CommandQueue;
import com.altencir.serverless.application.CommandService;
import com.altencir.serverless.application.SubmitCommand;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.net.URI;
import java.util.Map;

@Path("/api/commands")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class CommandResource {
    private final CommandService commands;
    private final CommandQueue queue;

    public CommandResource(CommandService commands, CommandQueue queue) {
        this.commands = commands;
        this.queue = queue;
    }

    @POST
    public Response submit(@Valid CommandRequest request) {
        var result = commands.submit(new SubmitCommand(request.messageId(), request.payload(), request.simulateFailure()));
        var status = result.duplicate() ? Response.Status.OK : Response.Status.ACCEPTED;
        return Response.status(status)
                .location(URI.create("/api/commands/" + result.record().messageId()))
                .entity(new CommandResponse(result.record(), result.duplicate())).build();
    }

    @GET
    @Path("/{messageId}")
    public CommandResponse find(@PathParam("messageId") String messageId) {
        return commands.find(messageId).map(record -> new CommandResponse(record, false))
                .orElseThrow(NotFoundException::new);
    }

    @POST
    @Path("/{messageId}/duplicate")
    public Response duplicate(@PathParam("messageId") String messageId) {
        try {
            commands.publishDuplicate(messageId);
            return Response.accepted(Map.of("messageId", messageId, "published", true)).build();
        } catch (CommandNotFoundException error) {
            throw new NotFoundException(error.getMessage());
        }
    }

    @GET
    @Path("/operations/queues")
    public QueueStatus queues() {
        return new QueueStatus(queue.pendingMessages(), queue.deadLetterMessages());
    }

    public record CommandRequest(
            @NotBlank @Size(max = 80) String messageId,
            @NotBlank @Size(max = 500) String payload,
            boolean simulateFailure) { }

    public record CommandResponse(com.altencir.serverless.domain.ProcessingRecord command, boolean duplicate) { }
    public record QueueStatus(long pending, long deadLetter) { }
}
