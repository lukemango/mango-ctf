package com.lukemango.ctf.commands.impl;

import com.lukemango.ctf.commands.util.AbstractCommand;
import com.lukemango.ctf.config.ConfigManager;
import com.lukemango.ctf.model.Game;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.incendo.cloud.annotation.specifier.Greedy;
import org.incendo.cloud.annotations.Argument;
import org.incendo.cloud.annotations.Command;
import org.incendo.cloud.annotations.suggestion.Suggestions;
import org.incendo.cloud.context.CommandContext;

import java.util.List;

public class PlayerCommands extends AbstractCommand {

    @Command("ctf join <team>")
    private void join(Player player, @Argument(value = "team", suggestions = "teams") @Greedy String team) {
        Game.get().join(player, team);
    }

    @Command("ctf leave")
    private void join(Player player) {
        Game.get().leave(player);
    }

    @Suggestions("teams")
    public List<String> teamsSuggestion(final CommandContext<CommandSender> commandContext, final String input) {
        final Player sender = (Player) commandContext.sender();

        return ConfigManager.get().getConfig().getTeams();
    }
}
