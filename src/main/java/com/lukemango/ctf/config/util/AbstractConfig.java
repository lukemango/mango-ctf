package com.lukemango.ctf.config.util;

import com.lukemango.ctf.CTFPlugin;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public abstract class AbstractConfig {
    private final String path;
    private final String filename;
    private File file;
    private YamlConfiguration configuration;

    public AbstractConfig(final String filename) {
        CTFPlugin instance = CTFPlugin.get();

        this.path = instance.getDataFolder() + "/" + filename;

        this.filename = filename;
        this.setFile();

        if (!file.exists()) {
            this.initializeFile();
            this.copyFromResource(instance.getResource(filename), file);
        }

        this.setYamlConfiguration();
    }

    /**
     * Create external file
     */
    private void initializeFile() {
        if (!file.exists())
            file.getParentFile().mkdirs();
        try {
            file.createNewFile();
        } catch (IOException e) {
            CTFPlugin.get().getLogger().warning("Could not create file " + filename + "with error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * @param resource the InputStream of the resource file.
     * @param target   copy chars(bytes) from resource(InputStream) to target(File).
     */
    private void copyFromResource(InputStream resource, File target) {
        byte[] buffer;
        try {
            buffer = new byte[resource.available()];
            resource.read(buffer);
            OutputStream outStream = new FileOutputStream(target);
            outStream.write(buffer);
            outStream.flush();
            outStream.close();

        } catch (IOException e) {
            CTFPlugin.get().getLogger().warning("Could not copy resource to file " + target.getName() + "with error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * @param path initialise file by path
     */
    public void initializeFile(String path) {
        File file = new File(path);
        if (!file.exists())
            file.getParentFile().mkdirs();
        try {
            file.createNewFile();
        } catch (IOException e) {
            CTFPlugin.get().getLogger().warning("Could not create file " + path + "with error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void save() {
        try {
            getYamlConfiguration().save(getFile());
            reload();
        } catch (IOException e) {
            CTFPlugin.get().getLogger().warning("Could not save " + filename + "with error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void setFile() {
        this.file = new File(path);
    }

    public File getFile() {
        return file;
    }

    private void setYamlConfiguration() {
        configuration = YamlConfiguration.loadConfiguration(file);
    }

    public YamlConfiguration getYamlConfiguration() {
        return configuration;
    }

    public void reload() {
        this.setYamlConfiguration();
    }
}