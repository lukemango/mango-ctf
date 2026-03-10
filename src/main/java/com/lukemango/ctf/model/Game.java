package com.lukemango.ctf.model;

import com.lukemango.ctf.CTFPlugin;
import com.lukemango.ctf.config.ConfigManager;
import com.lukemango.ctf.model.impl.CachedPlayer;
import com.lukemango.ctf.model.impl.Team;
import com.lukemango.ctf.util.FinePosition;
import com.lukemango.ctf.util.ItemUtil;
import net.kyori.adventure.audience.Audience;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;

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
    private Map<UUID, CachedPlayer> cachedPlayer;
    private Set<Player> audience;

    public Game() {
        instance = this;
        this.active = false;
        this.teams = new HashSet<>();
        this.cachedPlayer = new ConcurrentHashMap<>();
        this.audience = new HashSet<>();
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
            this.teams = new HashSet<>();
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
                this.restoreInventory(player);
                this.audience.remove(player);

                ConfigManager.get().getMessages().sendPlayerQuit(player);
                ConfigManager.get().getMessages().sendPlayerQuitBroadcast(this.getAudience(), player.getName());
                return;
            }
        }
    }

    /**
     * Starts the game
     */
    public void start(Player admin) {
        // Check all flags are set
        for (Team team : teams) {
            if (team.getFlagLocation() == null) {
                ConfigManager.get().getMessages().sendAdminNotAllFlagsSet(admin);
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
                ConfigManager.get().getMessages().sendAdminNeedPlayers(admin);
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

        ConfigManager.get().getMessages().sendAdminGameStarted(admin);
        ConfigManager.get().getMessages().sendPlayerGameStarted(this.getAudience());
    }

    public void end(@Nullable Player admin, boolean cancelled) {
        // Delete flags from the world in case set before game start
        if (admin == null) {
            this.teams.forEach(Team::deleteFlagSync);
        } else {
            this.teams.forEach(Team::deleteFlag);
        }

        if (!this.active) return;
    }

    public @Nullable Team isFlag(Material material, Location loc) {
        for (Team team : teams) {
            if (team.isFlag(material, loc)) {
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

    public Team getTeam(String name) {
        return teams.stream()
                .filter(t -> t.getName().equalsIgnoreCase(name))
                .findFirst()
                .orElse(null);
    }

    private Audience getAudience() {
        return Audience.audience(audience);
    }

    private void cacheInventory(Player player) {
        cachedPlayer.put(
                player.getUniqueId(),
                new CachedPlayer(player.getInventory().getExtraContents(), player.getLocation())
        );
        player.getInventory().clear();
    }

    private void restoreInventory(Player player) {
        CachedPlayer cached = cachedPlayer.get(player.getUniqueId());
        if (cached != null) {
            player.getInventory().setExtraContents(cached.getInventoryContents());
            player.teleport(cached.getPosition().toLocation());
            cachedPlayer.remove(player.getUniqueId());
        }
    }


    public static Game get() {
        return instance;
    }
}
