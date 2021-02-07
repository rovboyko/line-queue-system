package ru.home.linequeue.config;

import lombok.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public abstract class Configuration {

    private static final Logger log = LoggerFactory.getLogger(Configuration.class.getName());

    //CONFIG NAMES
    //General properties
    public static String PROP_FILE = "config";
    public static String DEFAULT_PROP_FILE = "config.properties";

    //Server properties
    public static String MASTER_HOST = "master.host";
    public static String MASTER_PORT = "master.port";

    //DEFAULT VALUES
    protected static final String DEFAULT_MASTER_HOST = "localhost";
    protected static final int DEFAULT_MASTER_PORT = 10042;

    private static final int DEFAULT_THREADS_COUNT = 4;
    private static final int DEFAULT_QUEUE_CAPACITY = 100 * 1000;

    protected static Set<String> argProperties = Stream.of("--"+PROP_FILE, "--"+ MASTER_HOST, "--"+ MASTER_PORT)
            .collect(Collectors.toCollection(HashSet::new));

    protected final Properties properties = new Properties();

    public static <T extends Configuration> T createFromArgs(@NonNull String[] args, @NonNull T config) {
        var propFilename = getPropFileName(args).orElse(Configuration.DEFAULT_PROP_FILE);
        var appConfig = createFromFile(propFilename, config);
        appConfig.addPropertiesFromArgs(args);
        return appConfig;
    }

    /**
     * Tries to find properties file in classpath or in filesystem and upload all props from it
     * @param filename - custom file name. Can't be null
     */
    public static <T extends Configuration> T createFromFile(@NonNull String filename, @NonNull T config) {
        try (InputStream input = Configuration.class.getClassLoader().getResourceAsStream(filename)) {

            if (input != null) {
                config.properties.load(input);
            } else {
                log.debug("Unable to find " + filename + " trying to load it from filesystem");
                try (InputStream fileInput = new FileInputStream(filename)) {
                    config.properties.load(fileInput);
                }
            }
            log.info("Config data was successfully uploaded from " + filename);
            return config;

        } catch (IOException ex) {
            log.error("IOException while trying to load config from file " + filename);
            return config;
        }
    }

    /**
     * Tries to parse args and add them to properties
     * @param args - args to be added
     */
    public void addPropertiesFromArgs(String[] args) {
        for (int i = 0; i < args.length; i++) {
            log.info("arg = " + args[i]);
            if (argProperties.contains(args[i])) {
                properties.put(args[i].replaceFirst("--", ""), args[++i]);
                log.info("arg = " + args[i]);
            } else {
                log.warn("Unknown property: " + args[i]);
            }
        }
    }

    public static Optional<String> getPropFileName(String[] args) {
        for (int i = 0; i < args.length; i++) {
            if (Objects.equals(args[i], "--"+PROP_FILE)) {
                return Optional.of(args[++i]);
            }
        }
        return Optional.empty();
    }

    public String getMasterHost() {
        return properties.getProperty(MASTER_HOST, DEFAULT_MASTER_HOST);
    }

    public int getMasterPort() {
        return Integer.parseInt(properties.getProperty(MASTER_PORT, String.valueOf(DEFAULT_MASTER_PORT)));
    }

    public String get(String propName) {
        return properties.getProperty(propName);
    }

}
