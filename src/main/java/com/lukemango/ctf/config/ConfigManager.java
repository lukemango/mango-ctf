package com.lukemango.ctf.config;

import com.lukemango.ctf.config.impl.Config;
import com.lukemango.ctf.config.impl.Messages;

public class ConfigManager {

    private static ConfigManager instance;

    private Config config;
    private Messages messages;

    public ConfigManager() {
        instance = this;
        this.init();
    }

    private void init() {
        config = new Config();
        messages = new Messages();
    }

    public void reload() {
        config.reload();
        messages.reload();
    }

    public static ConfigManager get() {
        return instance;
    }

    public Config getConfig() {
        return config;
    }

    public Messages getMessages() {
        return messages;
    }
}
