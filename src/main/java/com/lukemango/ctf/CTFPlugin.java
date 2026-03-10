package com.lukemango.ctf;

import com.lukemango.ctf.commands.CommandManager;
import com.lukemango.ctf.commands.impl.AdminCommands;
import com.lukemango.ctf.commands.impl.PlayerCommands;
import com.lukemango.ctf.config.ConfigManager;
import com.lukemango.ctf.listener.PlayerListener;
import com.lukemango.ctf.model.Game;
import org.bukkit.plugin.java.JavaPlugin;

public final class CTFPlugin extends JavaPlugin {

    private static CTFPlugin instance;

    private ConfigManager configManager;
    private CommandManager commandManager;

    private Game game;

    @Override
    public void onEnable() {
        instance = this;

        // Initialize the config manager & configs with it
        configManager = new ConfigManager();

        // Initialize the command manager & commands with it
        this.initCommands();

        // Initialize the game
        game = new Game();

        // Register event listeners
        getServer().getPluginManager().registerEvents(new PlayerListener(), this);
    }

    @Override
    public void onDisable() {
        Game.get().end(null, false);
    }

    private void initCommands() {
        commandManager = new CommandManager(instance);
        commandManager.registerCommand(
                new PlayerCommands(),
                new AdminCommands()
        );
    }

    public static CTFPlugin get() {
        return instance;
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }

    public CommandManager getCommandManager() {
        return commandManager;
    }

    public Game getGame() {
        return game;
    }
}
