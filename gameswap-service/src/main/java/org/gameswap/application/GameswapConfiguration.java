package org.gameswap.application;

import com.fasterxml.jackson.annotation.JsonProperty;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import com.sun.org.apache.xpath.internal.operations.Bool;
import io.dropwizard.Configuration;
import io.dropwizard.client.JerseyClientConfiguration;
import io.dropwizard.db.DataSourceFactory;

public class GameswapConfiguration extends Configuration {

    @NotNull
    @JsonProperty
    private boolean redirectAllToHttps = false;

    @Valid
    @NotNull
    @JsonProperty
    private DataSourceFactory database = new DataSourceFactory();

    @Valid
    @NotNull
    @JsonProperty
    private JerseyClientConfiguration httpClient = new JerseyClientConfiguration();

    @Valid
    @NotNull
    @JsonProperty
    private String googleSecret;

    @Valid
    @NotNull
    @JsonProperty
    private String rabbitMqSendUrl;

    @Valid
    @NotNull
    @JsonProperty
    private String rabbitMqReceiveUrl;

    @Valid
    @NotNull
    @JsonProperty
    private Boolean rabbitMqEnabled;

    @Valid
    @NotNull
    @JsonProperty
    private String jwtSecret;

    @Valid
    @NotNull
    @JsonProperty
    private int socketReadTimeoutMillis;

    public DataSourceFactory getDataSourceFactory() {
        return database;
    }

    public void setDataSourceFactory(DataSourceFactory database) {
        this.database = database;
    }

    public boolean isRedirectAllToHttps() {
        return redirectAllToHttps;
    }

    public void setRedirectAllToHttps(boolean redirectAllToHttps) {
        this.redirectAllToHttps = redirectAllToHttps;
    }

    public String getGoogleSecret() {
        return googleSecret;
    }

    public void setGoogleSecret(String googleSecret) {
        this.googleSecret = googleSecret;
    }

    public JerseyClientConfiguration getJerseyClient() {
        return httpClient;
    }

    public String getJwtSecret() {
        return jwtSecret;
    }

    public int getSocketReadTimeoutMillis() {
        return socketReadTimeoutMillis;
    }

    public void setRabbitMqSendUrl(String rabbitMqSendUrl) {
        this.rabbitMqSendUrl = rabbitMqSendUrl;
    }

    public void setRabbitMqReceiveUrl(String rabbitMqReceiveUrl) {
        this.rabbitMqReceiveUrl = rabbitMqReceiveUrl;
    }

    public String getRabbitMqSendUrl() {
        return rabbitMqSendUrl;
    }

    public String getRabbitMqReceiveUrl() {
        return rabbitMqReceiveUrl;
    }

    public Boolean isRabbitMqEnabled() {
        return rabbitMqEnabled;
    }

    public void setRabbitMqEnabled(Boolean rabbitMqEnabled) {
        this.rabbitMqEnabled = rabbitMqEnabled;
    }
}
