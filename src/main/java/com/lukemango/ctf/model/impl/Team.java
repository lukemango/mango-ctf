package com.lukemango.ctf.model.impl;

import com.lukemango.ctf.CTFPlugin;
import com.lukemango.ctf.config.ConfigManager;
import com.lukemango.ctf.model.Game;
import com.lukemango.ctf.util.FinePosition;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import javax.annotation.Nullable;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class Team {

    private final String name;
    private final String displayName;
    private final String color;
    private int score;
    private @Nullable FinePosition flagLocation;
    private final Set<UUID> members;
    private final Material flagMaterial;

    public Team(String name, String displayName, String color, Material flagMaterial) {
        this.name = name;
        this.displayName = displayName;
        this.color = color;
        this.score = 0;
        this.flagLocation = null;
        this.members = new HashSet<>();
        this.flagMaterial = flagMaterial;
    }

    public String getName() {
        return name;
    }

    public String getDisplayName() {
        return displayName;
    }

    public int getScore() {
        return score;
    }

    public void addScore(int points) {
        this.score += points;
    }

    public @Nullable FinePosition getFlagLocation() {
        return flagLocation;
    }

    public Set<UUID> getMembers() {
        return members;
    }

    public void addMember(UUID playerId) {
        members.add(playerId);
    }

    public void removeMember(UUID playerId) {
        members.remove(playerId);
    }

    public String getColor() {
        return color;
    }

    public Material getFlagMaterial() {
        return flagMaterial;
    }

    public boolean isFlag(Material material, Location loc) {
        if (flagLocation == null) {
            return false;
        }

        if (!material.equals(flagMaterial)) {
            return false;
        }

        return loc.getBlockX() == flagLocation.blockX()
                && loc.getBlockY() == flagLocation.blockY()
                && loc.getBlockZ() == flagLocation.blockZ()
                && loc.getWorld().getName().equals(flagLocation.world());
    }

    public void setFlagLocation(Player player) {
        // Is the player in a team?
        if (!Game.get().isInGame(player)) {
            ConfigManager.get().getMessages().sendAdminJoinFirst(player);
            return;
        }

        // Is the game already active?
        if (Game.get().isActive()) {
            ConfigManager.get().getMessages().sendAdminAlreadyActive(player);
            return;
        }

        // Is the flag location already set?
        if (this.getFlagLocation() != null) {
            this.deleteFlag();
            ConfigManager.get().getMessages().sendAdminRemovedExistingFlag(player, this.getDisplayName());
        }

        Location loc = player.getLocation();
        this.flagLocation = new FinePosition(loc.getBlockX(),
                loc.getBlockY() + 2, // +2 to account for the two fences we place on top of the flag
                loc.getBlockZ(),
                loc.getWorld().getName()
        );

        // Where the player is standing, place two fences with a piece
        // of the team's wool colour on top to mark the flag location
        Material woolMaterial = this.getFlagMaterial();
        Bukkit.getScheduler().runTask(CTFPlugin.get(), () -> {
            player.getWorld().getBlockAt(loc.clone().add(0, 2, 0)).setType(woolMaterial);
            player.getWorld().getBlockAt(loc.clone().add(0, 1, 0)).setType(Material.OAK_FENCE);
            player.getWorld().getBlockAt(loc).setType(Material.OAK_FENCE);
        });

        ConfigManager.get().getMessages().sendAdminSetFlag(player, this.getDisplayName());
    }

    public void deleteFlag() {
        FinePosition flagLocation = this.getFlagLocation();
        if (flagLocation != null) {
            Location loc = flagLocation.toLocation();
            Bukkit.getScheduler().runTask(CTFPlugin.get(), () -> {
                loc.getWorld().getBlockAt(loc).setType(Material.AIR);
                loc.getWorld().getBlockAt(loc.clone().add(0, -1, 0)).setType(Material.AIR);
                loc.getWorld().getBlockAt(loc.clone().add(0, -2, 0)).setType(Material.AIR);
            });
            this.flagLocation = null;
        }
    }

    // Used at shutdown to avoid trying to run a task when the plugin is disabled
    public void deleteFlagSync() {
        FinePosition flagLocation = this.getFlagLocation();
        if (flagLocation != null) {
            Location loc = flagLocation.toLocation();
            loc.getWorld().getBlockAt(loc).setType(Material.AIR);
            loc.getWorld().getBlockAt(loc.clone().add(0, -1, 0)).setType(Material.AIR);
            loc.getWorld().getBlockAt(loc.clone().add(0, -2, 0)).setType(Material.AIR);
            this.flagLocation = null;
        }
    }

}
