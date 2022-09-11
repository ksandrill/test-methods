package org.nsu.fit.tm_backend.repository.data;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.UUID;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class TopUpBalancePojo {
    @JsonProperty("customer_id")
    public UUID customerId;

    @JsonProperty("money")
    public int money;
}
