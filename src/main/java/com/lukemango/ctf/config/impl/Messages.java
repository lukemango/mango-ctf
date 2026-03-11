package com.lukemango.ctf.config.impl;

import com.lukemango.ctf.CTFPlugin;
import com.lukemango.ctf.config.util.AbstractConfig;
import com.lukemango.ctf.model.impl.Team;
import com.lukemango.ctf.util.Colourify;
import net.kyori.adventure.audience.Audience;
import org.bukkit.entity.Player;

public class Messages extends AbstractConfig {

    public Messages() {
        super("messages.yml");
    }

    // Player messages
    public void sendPlayerGameActive(Player player) {
        this.sendMessage(player, "player-messages.already-active");
    }

    public void sendPlayerJoinedTeam(Player player, String teamName) {
        this.sendMessage(player, "player-messages.joined-team", "<team>", teamName);
    }

    public void sendPlayerTeamNotFound(Player player, String teamName) {
        this.sendMessage(player, "player-messages.team-not-found", "<team>", teamName);
    }

    public void sendPlayerQuit(Player player) {
        this.sendMessage(player, "player-messages.quit");
    }

    public void sendPlayerQuitBroadcast(Audience audience, String playerName) {
        this.sendMessage(audience, "player-messages.quit-broadcast",
                "<player>", playerName
        );
    }

    public void sendPlayerAlreadyInTeam(Player player) {
        this.sendMessage(player, "player-messages.already-in-team");
    }

    public void sendPlayerGameStarted(Audience audience) {
        this.sendMessage(audience, "player-messages.game-started",
                "captures-to-win", String.valueOf(CTFPlugin.get().getConfigManager().getConfig().getCapturesToWin())
        );
    }

    public void sendPlayerOutOfTime(Audience audience) {
        this.sendMessage(audience, "player-messages.out-of-time");
    }

    public void sendPlayerCantStealOwnFlag(Player player) {
        this.sendMessage(player, "player-messages.cant-steal-own-flag");
    }

    public void sendPlayerAlreadyCarryingFlag(Player player) {
        this.sendMessage(player, "player-messages.already-carrying-flag");
    }

    public void sendPlayerFlagAlreadyStolen(Player player) {
        this.sendMessage(player, "player-messages.flag-already-stolen");
    }

    public void sendPlayerFlagStolen(Audience audience, String playerName, String teamName) {
        this.sendMessage(audience, "player-messages.flag-stolen",
                "<player>", playerName,
                "<team>", teamName
        );
    }

    public void sendPlayerFlagCaptured(Audience audience, String playerName, String teamName, int captures, int capturesToWin) {
        this.sendMessage(audience, "player-messages.flag-captured",
                "<player>", playerName,
                "<team>", teamName,
                "<current-captures>", String.valueOf(captures),
                "<captures-to-win>", String.valueOf(capturesToWin)
        );
    }

    public void sendPlayerTimeUpdate(Audience audience, String timeRemaining) {
        this.sendMessage(audience, "player-messages.time-update",
                "<time-remaining>", timeRemaining
        );
    }

    public void sendPlayerScoreUpdate(Audience audience, String teamName, int currentCaptures, int capturesToWin) {
        this.sendMessage(audience, "player-messages.score-update",
                "<team>", teamName,
                "<current-captures>", String.valueOf(currentCaptures),
                "<captures-to-win>", String.valueOf(capturesToWin)
        );
    }

    public void sendPlayerGameEndedNoPlayersOnTeam(Audience audience, Team team) {
        this.sendMessage(audience, "player-messages.game-ended-no-players-on-team",
                "<team>", team.getDisplayName()
        );
    }

    public void sendPlayerGameEnded(Audience audience, Team team) {
        this.sendMessage(audience, "player-messages.game-ended",
                "<team>", team.getDisplayName()
        );
    }

    public void sendPlayerNoGameActive(Player player) {
        this.sendMessage(player, "player-messages.no-game-active");
    }

    public void sendPlayerPickedUpDroppedFlag(Audience audience, String playerName, String teamName) {
        this.sendMessage(audience, "player-messages.picked-up-dropped-flag",
                "<player>", playerName,
                "<team>", teamName
        );
    }

    public void sendPlayerDroppedFlag(Audience audience, String playerName, String teamName) {
        this.sendMessage(audience, "player-messages.dropped-flag",
                "<player>", playerName,
                "<team>", teamName
        );
    }

    public void sendPlayerReturnedFlag(Audience audience, String playerName, String teamName) {
        this.sendMessage(audience, "player-messages.returned-flag",
                "<player>", playerName,
                "<team>", teamName
        );
    }

    // Admin messages
    public void sendAdminAlreadyActive(Player player) {
        this.sendMessage(player, "admin-messages.already-active");
    }
    public void sendAdminJoinFirst(Player player) {
        this.sendMessage(player, "admin-messages.join-first");
    }
    public void sendAdminNotAllFlagsSet(Player player) {
        this.sendMessage(player, "admin-messages.not-all-flags-set");
    }
    public void sendAdminRemovedExistingFlag(Player player, String teamName) {
        this.sendMessage(player, "admin-messages.removed-existing-flag", "<team>", teamName);
    }
    public void sendAdminNeedPlayers(Player player) {
        this.sendMessage(player, "admin-messages.need-players");
    }
    public void sendAdminSetFlag(Player player, String teamName) {
        this.sendMessage(player, "admin-messages.set-flag", "<team>", teamName);
    }
    public void sendAdminGameStarted(Player player) {
        this.sendMessage(player, "admin-messages.game-started");
    }


    /**
     * Send a message to a player from the messages.yml file with error handling
     *
     * @param players      The player to send the message to
     * @param path         The path to the message
     * @param placeholders The placeholders to replace in the message, in the format placeholder, value, placeholder, value, etc.
     */
    private void sendMessage(Audience players, String path, String... placeholders) {
        String message = this.getMessage(path);

        // Replace placeholders
        if (placeholders.length % 2 != 0) {
            CTFPlugin.get().getLogger().warning("Invalid placeholders provided for message: " + path);
        } else {
            for (int i = 0; i < placeholders.length; i += 2) {
                String placeholder = placeholders[i];
                String value = placeholders[i + 1];
                message = message.replace(placeholder, value);
            }
        }

        players.sendMessage(Colourify.colour(message));
    }

    /**
     * Get a message from the messages.yml file with error handling
     *
     * @param path The path to the message
     * @return The message
     */
    private String getMessage(String path) {
        String message = getYamlConfiguration().getString(path);
        if (message == null) {
            CTFPlugin.get().getLogger().warning("Message " + path + " not found or configured incorrectly.");
            return "<red>Message not found or configured incorrectly. Please contact an administrator.";
        }
        return getYamlConfiguration().getString(path);
    }
}
