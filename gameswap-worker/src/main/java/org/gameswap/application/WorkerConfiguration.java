package org.gameswap.application;

import com.fasterxml.jackson.annotation.JsonProperty;

import javax.validation.Valid;
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
    private String bggApiRootUrl;

    @NotNull
    @JsonProperty
    private String jdbcUrl;

    @Valid
    @NotNull
    @JsonProperty
    private String rabbitMqSendUrl;

    @Valid
    @NotNull
    @JsonProperty
    private String rabbitMqReceiveUrl;

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

    public String getRabbitMqSendUrl() {
        return rabbitMqSendUrl;
    }

    public void setRabbitMqSendUrl(String rabbitMqSendUrl) {
        this.rabbitMqSendUrl = rabbitMqSendUrl;
    }

    public String getRabbitMqReceiveUrl() {
        return rabbitMqReceiveUrl;
    }

    public void setRabbitMqReceiveUrl(String rabbitMqReceiveUrl) {
        this.rabbitMqReceiveUrl = rabbitMqReceiveUrl;
    }

    public String getBggApiRootUrl() {
        return bggApiRootUrl;
    }

    public void setBggApiRootUrl(String bggApiRootUrl) {
        this.bggApiRootUrl = bggApiRootUrl;
    }
}
