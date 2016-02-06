package org.gameswap.application;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.dropwizard.Configuration;
import io.dropwizard.client.JerseyClientConfiguration;
import io.dropwizard.db.DataSourceFactory;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;

public class GameswapConfiguration extends Configuration {

    @NotNull
    @JsonProperty
    private String sampleProperty;

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

    public String getSampleProperty() {
        return sampleProperty;
    }

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
}
