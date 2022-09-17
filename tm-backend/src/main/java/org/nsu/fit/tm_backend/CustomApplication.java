package org.nsu.fit.tm_backend;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.glassfish.jersey.logging.LoggingFeature;
import org.glassfish.jersey.server.ResourceConfig;
import org.nsu.fit.tm_backend.config.AuthenticationFilter;
import org.nsu.fit.tm_backend.config.AuthorizationFilter;
import org.nsu.fit.tm_backend.config.CORSFilter;
import org.nsu.fit.tm_backend.controller.RestController;
import org.nsu.fit.tm_backend.exception.ServerExceptionMapper;

public class CustomApplication extends ResourceConfig {
    public CustomApplication() {
        register(
            new LoggingFeature(
                Logger.getLogger(LoggingFeature.DEFAULT_LOGGER_NAME),
                Level.INFO,
                LoggingFeature.Verbosity.PAYLOAD_TEXT,
                10000));

        register(ServerExceptionMapper.class);

        register(AuthenticationFilter.class);
        register(AuthorizationFilter.class);
        register(CORSFilter.class);
        register(RestController.class);
    }
}
