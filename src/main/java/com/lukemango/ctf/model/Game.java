package com.lukemango.ctf.model;

import com.lukemango.ctf.CTFPlugin;
import com.lukemango.ctf.config.ConfigManager;
import com.lukemango.ctf.config.impl.Messages;
import com.lukemango.ctf.model.impl.CachedPlayer;
import com.lukemango.ctf.model.impl.TaskType;
import com.lukemango.ctf.model.impl.Team;
import com.lukemango.ctf.util.FinePosition;
import com.lukemango.ctf.util.ItemUtil;
import net.kyori.adventure.audience.Audience;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import javax.annotation.Nullable;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class Game {

    private static Game instance;

    private boolean active;
    private Set<Team> teams;
    private long startTime;
    private Map<UUID, CachedPlayer> cachedInventories = new ConcurrentHashMap<>(); // Not in reset() in case players leave the server, and we need to restore their inventory when they rejoin
    private Set<Player> audience;
    private Map<UUID, String> flagCarriers; // Map of player UUID to team name
    private Map<TaskType, BukkitTask> tasks;

    public Game() {
        instance = this;
        this.reset();
    }

    /**
     * Reset caches and sets to default values.
     * Called on plugin enable and when a new game starts
     */
    private void reset() {
        this.active = false;
        this.teams = new HashSet<>();
        this.audience = new HashSet<>();
        this.flagCarriers = new ConcurrentHashMap<>();
        this.tasks = new ConcurrentHashMap<>();
    }

    /**
     * Initialise the game, setting it to active and creating the teams.
     */
    public void join(Player player, @Nullable String team) {
        ConfigManager cfgManager = CTFPlugin.get().getConfigManager();

        if (active) {
            cfgManager.getMessages().sendPlayerGameActive(player);
            return;
        }

        if (teams.isEmpty()) {
            this.reset();
            for (String teamName : cfgManager.getConfig().getTeams()) {
                teams.add(new Team(
                        teamName,
                        cfgManager.getConfig().getTeamName(teamName),
                        cfgManager.getConfig().getTeamColor(teamName),
                        cfgManager.getConfig().getTeamFlagMaterial(teamName)
                ));
            }
        } else {
            for (Team t : teams) {
                if (t.getMembers().contains(player.getUniqueId())) {
                    cfgManager.getMessages().sendPlayerAlreadyInTeam(player);
                    return;
                }
            }
        }

        // Add to audience
        audience.add(player);

        // Add the player to the team with the least members if no team specified, otherwise add to the specified team
        Team teamToJoin;
        if (team == null) {
            teamToJoin = teams.stream()
                    .min(Comparator.comparingInt(t -> t.getMembers().size()))
                    .orElse(null);
        } else {
            teamToJoin = teams.stream()
                    .filter(t -> t.getName().equalsIgnoreCase(team))
                    .findFirst()
                    .orElse(null);
        }

        if (teamToJoin == null) {
            cfgManager.getMessages().sendPlayerTeamNotFound(player, team);
            return;
        }

        teamToJoin.addMember(player.getUniqueId());
        cfgManager.getMessages().sendPlayerJoinedTeam(player, teamToJoin.getName());
    }

    public void leave(Player player) {
        for (Team team : teams) {
            if (team.getMembers().contains(player.getUniqueId())) {
                team.removeMember(player.getUniqueId());
                this.audience.remove(player);
                Bukkit.getScheduler().runTask(CTFPlugin.get(), () -> this.restoreInventory(player)); // Needs to be on main thread for teleporting

                ConfigManager.get().getMessages().sendPlayerQuit(player);
                ConfigManager.get().getMessages().sendPlayerQuitBroadcast(this.getAudience(), player.getName());

                if (team.getMembers().isEmpty() && active) {
                    ConfigManager.get().getMessages().sendPlayerGameEndedNoPlayersOnTeam(this.getAudience(), team);
                    this.end(null);
                }
                return;
            }
        }
    }

    /**
     * Starts the game
     */
    public void start(Player admin) {
        Messages messages = ConfigManager.get().getMessages();

        // Check all flags are set
        for (Team team : teams) {
            if (team.getFlagLocation() == null) {
                messages.sendAdminNotAllFlagsSet(admin);
                return;
            }
        }

        this.startTime = System.currentTimeMillis();
        this.active = true;

        for (Team team : teams) {
            // Verify players are still online and in the team, removing any that aren't
            for (UUID playerId : team.getMembers()) {
                Player player = CTFPlugin.get().getServer().getPlayer(playerId);
                if (player == null || !player.isOnline()) {
                    team.removeMember(playerId);
                }
            }

            // Ensure each team has at least one member
            if (team.getMembers().isEmpty()) {
                messages.sendAdminNeedPlayers(admin);
                return;
            }
        }

        for (Team team : teams) {
            for (UUID playerId : team.getMembers()) {
                Player player = CTFPlugin.get().getServer().getPlayer(playerId);
                this.cacheInventory(player); // Can't be null as we check above that all players are online
                ItemUtil.giveTeamEquipment(player, team);
            }
        }

        this.tasks.put(TaskType.TIMEOUT,
                CTFPlugin.get().getServer().getScheduler().runTaskLater(CTFPlugin.get(), () -> {
                    this.end(null);
                    messages.sendPlayerOutOfTime(this.getAudience());
                }, CTFPlugin.get().getConfigManager().getConfig().getGameMaxDuration() * 20L)
        );

        this.tasks.put(TaskType.SCORE_UPDATE,
                CTFPlugin.get().getServer().getScheduler().runTaskTimerAsynchronously(CTFPlugin.get(), () -> {
                    messages.sendPlayerTimeUpdate(this.getAudience(), this.formatTimeRemaining());
                    int capturesToWin = CTFPlugin.get().getConfigManager().getConfig().getCapturesToWin();
                    for (Team team : teams) {
                        messages.sendPlayerScoreUpdate(this.getAudience(), team.getDisplayName(), team.getScore(), capturesToWin);
                    }
                }, 20 * 30L, 20 * 30L)
        ); // Update every 30 seconds

        messages.sendAdminGameStarted(admin);
        messages.sendPlayerGameStarted(this.getAudience());
    }

    /**
     * Ends the game, resetting all values and deleting flags.
     *
     * @param admin     The admin who ended the game (can be null)
     */
    public void end(@Nullable Player admin) {
        // Delete flags from the world in case set before game start
        teams.forEach(team -> {
            if (admin == null) { // Prevents error messages about async block changes
                team.deleteFlagSync();
            } else {
                team.deleteFlag();
            }

            team.getMembers().forEach(playerId -> {
                Player player = CTFPlugin.get().getServer().getPlayer(playerId);
                if (player != null) this.restoreInventory(player);
            });
        });

        this.tasks.values().forEach(BukkitTask::cancel);
        this.reset();
    }

    public @Nullable Team isFlag(Material material, Location loc) {
        for (Team team : teams) {
            if (team.isFlag(material, loc)) {
                return team;
            }
        }
        return null;
    }

    public @Nullable Team isDroppedFlag(Material material, Location loc) {
        for (Team team : teams) {
            if (team.isDroppedFlag(material, loc)) {
                return team;
            }
        }
        return null;
    }

    public boolean isActive() {
        return active;
    }

    public boolean isInGame(Player player) {
        return teams.stream().anyMatch(t -> t.getMembers().contains(player.getUniqueId()));
    }

    public Team getPlayerTeam(Player player) {
        return teams.stream()
                .filter(t -> t.getMembers().contains(player.getUniqueId()))
                .findFirst()
                .orElse(null);
    }

    public Team getTeam(String teamName) {
        return teams.stream()
                .filter(t -> t.getName().equalsIgnoreCase(teamName))
                .findFirst()
                .orElse(null);
    }

    public Map<UUID, String> getFlagCarriers() {
        return flagCarriers;
    }

    public Audience getAudience() {
        return Audience.audience(audience);
    }

    public Set<Team> getTeams() {
        return teams;
    }

    public String formatTimeRemaining() {
        long elapsedSeconds = (System.currentTimeMillis() - startTime) / 1000;
        long remainingSeconds = CTFPlugin.get().getConfigManager().getConfig().getGameMaxDuration() - elapsedSeconds;

        long minutes = remainingSeconds / 60;
        long seconds = remainingSeconds % 60;

        return String.format("%02d:%02d", minutes, seconds);
    }

    private void cacheInventory(Player player) {
        cachedInventories.put(
                player.getUniqueId(),
                new CachedPlayer(
                        player.getInventory().getContents().clone(),
                        player.getInventory().getArmorContents().clone(),
                        player.getInventory().getExtraContents().clone(),
                        new FinePosition(
                                player.getLocation().getX(),
                                player.getLocation().getY(),
                                player.getLocation().getZ(),
                                player.getLocation().getWorld().getName()
                        )
                )
        );

        player.getInventory().clear();
        player.getInventory().setArmorContents(null);
        player.getInventory().setExtraContents(null);
    }

    public void restoreInventory(Player player) {
        CachedPlayer cached = cachedInventories.get(player.getUniqueId());

        if (cached != null) {
            player.getInventory().setContents(cached.inventoryContents());
            player.getInventory().setArmorContents(cached.armorContents());
            player.getInventory().setExtraContents(cached.extraContents());

            player.teleport(cached.position().toLocation());

            cachedInventories.remove(player.getUniqueId());
        }
    }

    public static Game get() {
        return instance;
    }
}
