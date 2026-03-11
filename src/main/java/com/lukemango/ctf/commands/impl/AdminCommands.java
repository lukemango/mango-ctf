package com.lukemango.ctf.commands.impl;

import com.lukemango.ctf.commands.util.AbstractCommand;
import com.lukemango.ctf.config.ConfigManager;
import com.lukemango.ctf.model.Game;
import com.lukemango.ctf.model.impl.Team;
import org.bukkit.entity.Player;
import org.incendo.cloud.annotation.specifier.Greedy;
import org.incendo.cloud.annotations.Argument;
import org.incendo.cloud.annotations.Command;
import org.incendo.cloud.annotations.Permission;

public class AdminCommands extends AbstractCommand {

    @Command("ctf admin set-flag <team>")
    @Permission("ctf.admin")
    private void setFlag(Player player, @Argument(value = "team", suggestions = "teams") @Greedy String teamName) {
        Team team = Game.get().getTeam(teamName);
        if (team == null) {
            ConfigManager.get().getMessages().sendPlayerTeamNotFound(player, teamName);
            return;
        }
        team.setFlagLocation(player);
    }

    @Command("ctf admin start")
    @Permission("ctf.admin")
    private void startGame(Player player) {
        Game.get().start(player);
    }

    @Command("ctf admin stop")
    @Permission("ctf.admin")
    private void endGame(Player player) {
        Game.get().end(player);
    }
}
