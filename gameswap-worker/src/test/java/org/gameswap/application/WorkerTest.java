package org.gameswap.application;

import com.fasterxml.jackson.databind.ObjectMapper;

import com.google.common.io.ByteStreams;
import com.google.common.io.Files;

import io.dropwizard.configuration.ConfigurationParsingException;

import org.junit.Test;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

import static org.junit.Assert.assertEquals;

public class WorkerTest {

    @Test
    public void configuration() throws Exception {
        File tempDir = Files.createTempDir();
        File file = new File(tempDir, "config.json");
        FileOutputStream outputStream = new FileOutputStream(file);
        ByteStreams.copy(this.getClass().getClassLoader().getResourceAsStream("config.json"), outputStream);
        outputStream.close();
        WorkerConfiguration configuration = Worker.loadConfig(file.getAbsolutePath(), new ObjectMapper());
        assertEquals(configuration.getAwsGameswapDirectory(), "bucket/gameswap");
        assertEquals(configuration.getAwsKeyId(), "key");
        assertEquals(configuration.getAwsSecretAccessKey(), "secretKey");
        assertEquals(configuration.getJdbcUrl(), "jdbc://url");
        assertEquals(configuration.getBggApiRootUrl(), "hello");
    }
    
    @Test(expected = ExceptionInInitializerError.class)
    public void missingPropertiesArg() throws Exception {
        new Worker(new String[]{});
    }

    @Test(expected = ExceptionInInitializerError.class)
    public void nonExistentFile() throws Exception {
        new Worker(new String[]{"noFile"});
    }

    @Test(expected = ExceptionInInitializerError.class)
    public void badJson() throws Exception {
        File tempDir = Files.createTempDir();
        File file = new File(tempDir, "bad.json");
        ByteStreams.copy(this.getClass().getClassLoader().getResourceAsStream("bad.json"), new FileOutputStream(file));
        new Worker(new String[]{file.getAbsolutePath()});
    }
}