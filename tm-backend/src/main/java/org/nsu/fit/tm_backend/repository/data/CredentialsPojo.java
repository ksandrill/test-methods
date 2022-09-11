package org.nsu.fit.tm_backend.repository.data;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class CredentialsPojo {
    @JsonProperty("login")
    public String login;

    @JsonProperty("pass")
    public String pass;

    @Override
    public String toString() {
        return "login='" + login + '\'' +
                ", pass='" + pass + '\'';
    }
}
