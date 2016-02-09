package org.gameswap.application;

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
        ByteStreams.copy(this.getClass().getClassLoader().getResourceAsStream("config.json"), new FileOutputStream(file));
        WorkerConfiguration configuration = Worker.loadConfig(file.getAbsolutePath());
        assertEquals(configuration.getAwsGameswapDirectory(), "bucket/gameswap");
        assertEquals(configuration.getAwsKeyId(), "key");
        assertEquals(configuration.getAwsSecretAccessKey(), "secretKey");
        assertEquals(configuration.getJdbcUrl(), "jdbc://url");
    }


    @Test
    public void validProperties() throws Exception {
        File tempDir = Files.createTempDir();
        File file = new File(tempDir, "config.json");
        ByteStreams.copy(this.getClass().getClassLoader().getResourceAsStream("config.json"), new FileOutputStream(file));
        new Worker().run(new String[]{file.getAbsolutePath()});
    }

    @Test(expected = IllegalArgumentException.class)
    public void missingPropertiesArg() throws Exception {
        new Worker().run(new String[]{});
    }

    @Test(expected = FileNotFoundException.class)
    public void nonExistentFile() throws Exception {
        new Worker().run(new String[]{"noFile"});
    }

    @Test(expected = ConfigurationParsingException.class)
    public void badJson() throws Exception {
        File tempDir = Files.createTempDir();
        File file = new File(tempDir, "bad.json");
        ByteStreams.copy(this.getClass().getClassLoader().getResourceAsStream("bad.json"), new FileOutputStream(file));
        new Worker().run(new String[]{file.getAbsolutePath()});
    }
}