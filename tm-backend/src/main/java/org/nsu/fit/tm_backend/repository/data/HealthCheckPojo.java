package org.nsu.fit.tm_backend.repository.data;

import com.fasterxml.jackson.annotation.JsonProperty;

public class HealthCheckPojo {
    public HealthCheckPojo() {
        status = "OK";
    }

    @JsonProperty("status")
    public String status;

    @JsonProperty("db_status")
    public String dbStatus;
}
