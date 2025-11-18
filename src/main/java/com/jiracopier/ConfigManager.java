package com.jiracopier;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

/**
 * Manages loading and saving of application configuration from/to a .properties file.
 */
public class ConfigManager {
    private final Properties properties = new Properties();
    private final String configFilePath;

    public static final String CONFIG_FILE_NAME = ".jiracopier.properties";

    public ConfigManager() {
        String userHome = System.getProperty("user.home");
        this.configFilePath = userHome + File.separator + CONFIG_FILE_NAME;
    }

    /**
     * Loads configuration from the properties file.
     * If the file doesn't exist, it will be ignored, and defaults will be used.
     */
    public void loadConfig() {
        try (FileInputStream fis = new FileInputStream(configFilePath)) {
            properties.load(fis);
        } catch (IOException e) {
            System.out.println("Info: Configuration file not found. A new one will be created upon saving.");
        }
    }

    /**
     * Saves the current configuration to the properties file.
     */
    public void saveConfig() {
        try (FileOutputStream fos = new FileOutputStream(configFilePath)) {
            properties.store(fos, "JIRA Copier Configuration");
        } catch (IOException e) {
            System.err.println("Error: Could not save configuration file.");
            e.printStackTrace();
        }
    }

    /**
     * Retrieves a property value by its key.
     *
     * @param key          The key of the property.
     * @param defaultValue The value to return if the key is not found.
     * @return The property value or the default value.
     */
    public String getProperty(String key, String defaultValue) {
        return properties.getProperty(key, defaultValue);
    }

    /**
     * Sets a property value.
     *
     * @param key   The key of the property.
     * @param value The value to set.
     */
    public void setProperty(String key, String value) {
        if (value != null) {
            properties.setProperty(key, value);
        } else {
            properties.remove(key);
        }
    }
}
