package org.susf.cubesat.gcs.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

public final class Env {

    private static final Properties FILE = load(Path.of(".env"));

    private Env() {}

    private static Properties load(Path path) {
        Properties props = new Properties();
        if (Files.exists(path)) {
            try (BufferedReader reader = Files.newBufferedReader(path)) {
                props.load(reader);
            } catch (IOException e) {
                throw new RuntimeException("Could not read " + path.toAbsolutePath(), e);
            }
        }
        return props;
    }

    public static String get(String key) {
        String value = System.getenv(key);
        return value != null ? value : FILE.getProperty(key);
    }

    public static String get(String key, String defaultValue) {
        String value = get(key);
        return value != null ? value : defaultValue;
    }
}