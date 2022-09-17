package org.nsu.fit.tm_backend.exception;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import lombok.extern.slf4j.Slf4j;
import lombok.var;
import org.nsu.fit.tm_backend.exception.data.ServerExceptionResponse;

@Slf4j
public class ServerExceptionMapper implements ExceptionMapper<Throwable> {
    @Override
    public Response toResponse(Throwable exception) {
        log.error("UNHANDLED ERROR: " + exception.getMessage(), exception);

        var serverExceptionResponse = ServerExceptionResponse.builder()
            .message("Something went wrong...")
            .details(exception.getMessage())
            .build();

        return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
            .entity(serverExceptionResponse)
            .type(MediaType.APPLICATION_JSON_TYPE)
            .build();
    }
}
