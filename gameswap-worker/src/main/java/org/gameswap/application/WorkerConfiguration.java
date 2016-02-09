package org.gameswap.application;

import com.fasterxml.jackson.annotation.JsonProperty;

import javax.validation.constraints.NotNull;

public class WorkerConfiguration {

    @NotNull
    @JsonProperty
    private String awsKeyId;

    @NotNull
    @JsonProperty
    private String awsSecretAccessKey;

    @NotNull
    @JsonProperty
    private String awsGameswapDirectory;

    @NotNull
    @JsonProperty
    private String jdbcUrl;

    public String getAwsKeyId() {
        return awsKeyId;
    }

    public String getAwsSecretAccessKey() {
        return awsSecretAccessKey;
    }

    public String getAwsGameswapDirectory() {
        return awsGameswapDirectory;
    }

    public String getJdbcUrl() {
        return jdbcUrl;
    }
}
