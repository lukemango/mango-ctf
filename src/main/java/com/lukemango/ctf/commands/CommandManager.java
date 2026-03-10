package com.lukemango.ctf.commands;

import com.lukemango.ctf.CTFPlugin;
import com.lukemango.ctf.commands.util.AbstractCommand;
import io.leangen.geantyref.TypeToken;
import org.bukkit.command.CommandSender;
import org.incendo.cloud.SenderMapper;
import org.incendo.cloud.annotations.AnnotationParser;
import org.incendo.cloud.execution.ExecutionCoordinator;
import org.incendo.cloud.paper.LegacyPaperCommandManager;
import org.incendo.cloud.parser.ArgumentParser;
import org.incendo.cloud.setting.ManagerSetting;

import java.lang.reflect.InvocationTargetException;

public class CommandManager {

    private final LegacyPaperCommandManager<CommandSender> commandManager;
    private final AnnotationParser<CommandSender> annotationParser;
    private final CTFPlugin plugin;

    public CommandManager(CTFPlugin plugin) {
        this.plugin = plugin;
        this.commandManager = new LegacyPaperCommandManager<>(
                plugin,
                ExecutionCoordinator.asyncCoordinator(),
                SenderMapper.identity()
        );

        commandManager.settings().set(ManagerSetting.OVERRIDE_EXISTING_COMMANDS, true);
        commandManager.settings().set(ManagerSetting.ALLOW_UNSAFE_REGISTRATION, true);

        annotationParser = new AnnotationParser<>(
                commandManager,
                CommandSender.class
        );
    }

    public CommandManager registerCommands(Class<? extends AbstractCommand>... commands) {
        for (Class<? extends AbstractCommand> command : commands) {
            plugin.getLogger().info("Registering command: " + command.getSimpleName());
            try {
                annotationParser.parse(command.getConstructor().newInstance());
            } catch (InstantiationException | IllegalAccessException | InvocationTargetException |
                     NoSuchMethodException e) {
                plugin.getLogger().info("Failed to register command: " + command.getSimpleName());
                e.printStackTrace();
            }
        }
        return this;
    }

    public CommandManager registerCommand(AbstractCommand... abstractCommands) {
        for (AbstractCommand command : abstractCommands) {
            plugin.getLogger().info("Registering command: " + command.getClass().getSimpleName());
            annotationParser.parse(command);
        }
        return this;
    }

    public <T> CommandManager withArgumentType(Class<T> clazz, ArgumentParser<CommandSender, T> parser) {
        commandManager.parserRegistry().registerParserSupplier(TypeToken.get(clazz), params -> parser);
        return this;
    }
}
