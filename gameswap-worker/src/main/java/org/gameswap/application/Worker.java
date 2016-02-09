package org.gameswap.application;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.dropwizard.configuration.*;
import io.dropwizard.validation.BaseValidator;

import java.io.IOException;

public class Worker {

    public static void main(String[] args) {
        try {
            Worker worker = new Worker();
            worker.run(args);
        }catch (Exception e){
            throw new RuntimeException(e);
        }
    }

    public void run(String[] args) throws IOException, ConfigurationException {
        if(args.length == 0){
            throw new IllegalArgumentException("expect at least one argument");
        }
        loadConfig(args[0]);
    }

    public static WorkerConfiguration loadConfig(String arg) throws IOException, ConfigurationException {
        ConfigurationFactory<WorkerConfiguration> dw = new ConfigurationFactory<>(WorkerConfiguration.class, BaseValidator.newValidator(), new ObjectMapper(), "dw");
        EnvironmentVariableSubstitutor substitutor = new EnvironmentVariableSubstitutor(false);
        SubstitutingSourceProvider provider = new SubstitutingSourceProvider(new FileConfigurationSourceProvider(), substitutor);
        return dw.build(provider, arg);
    }
}
