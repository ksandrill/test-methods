package org.nsu.fit.tm_backend;

import org.glassfish.jersey.server.ResourceConfig;

import java.io.IOException;
import java.util.logging.LogManager;
import org.nsu.fit.tm_backend.config.AuthenticationFilter;
import org.nsu.fit.tm_backend.config.AuthorizationFilter;
import org.nsu.fit.tm_backend.config.CORSFilter;
import org.nsu.fit.tm_backend.controller.RestController;

public class CustomApplication extends ResourceConfig {
    public CustomApplication() {
        try {
            LogManager.getLogManager().readConfiguration(
                    Thread.currentThread().getContextClassLoader().getResourceAsStream("./logging.properties"));
        } catch (IOException ex) {
            ex.printStackTrace();
            throw new RuntimeException(ex);
        }

        register(AuthenticationFilter.class);
        register(AuthorizationFilter.class);
        register(CORSFilter.class);
        register(RestController.class);
    }
}
