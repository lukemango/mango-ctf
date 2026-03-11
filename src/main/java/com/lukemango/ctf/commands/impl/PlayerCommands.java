package com.lukemango.ctf.commands.impl;

import com.lukemango.ctf.CTFPlugin;
import com.lukemango.ctf.commands.util.AbstractCommand;
import com.lukemango.ctf.config.ConfigManager;
import com.lukemango.ctf.config.impl.Messages;
import com.lukemango.ctf.model.Game;
import com.lukemango.ctf.model.impl.Team;
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
    private void leave(Player player) {
        Game.get().leave(player);
    }

    @Command("ctf score")
    private void score(Player player) {
        Messages messages = ConfigManager.get().getMessages();

        if (!Game.get().isActive()) {
            messages.sendPlayerNoGameActive(player);
            return;
        }

        messages.sendPlayerTimeUpdate(player, Game.get().formatTimeRemaining());
        int capturesToWin = CTFPlugin.get().getConfigManager().getConfig().getCapturesToWin();
        for (Team team : Game.get().getTeams()) {
            messages.sendPlayerScoreUpdate(player, team.getDisplayName(), team.getScore(), capturesToWin);
        }
    }

    @Suggestions("teams")
    public List<String> teamsSuggestion(final CommandContext<CommandSender> commandContext, final String input) {
        final Player sender = (Player) commandContext.sender();

        return ConfigManager.get().getConfig().getTeams();
    }
}
