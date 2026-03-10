package com.lukemango.ctf.config.impl;

import com.lukemango.ctf.config.util.AbstractConfig;
import org.bukkit.Material;

import java.util.List;

public class Config extends AbstractConfig {

    public Config() {
        super("config.yml");
    }

    public List<String> getTeams() {
        return getYamlConfiguration().getConfigurationSection("teams").getKeys(false).stream().toList();
    }

    public String getTeamName(String team) {
        return getYamlConfiguration().getString("teams." + team + ".display-name", team);
    }

    public String getTeamColor(String team) {
        return getYamlConfiguration().getString("teams." + team + ".color", "255,255,255");
    }

    public Material getTeamFlagMaterial(String team) {
        String materialString = getYamlConfiguration().getString("teams." + team + ".flag-material", "STONE");
        try {
            return Material.valueOf(materialString);
        } catch (IllegalArgumentException e) {
            return Material.STONE;
        }
    }

    public int getGameMaxDuration() {
        return getYamlConfiguration().getInt("game.game-time", 600);
    }

    public int getCapturesToWin() {
        return getYamlConfiguration().getInt("game.captures-to-win", 3);
    }
}
