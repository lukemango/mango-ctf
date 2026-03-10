package com.lukemango.ctf.listener;

import com.lukemango.ctf.model.Game;
import com.lukemango.ctf.model.impl.Team;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;

public class PlayerListener implements Listener {

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onFlagInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();

        if (!Game.get().isActive()) return;
        if (!Game.get().isInGame(player)) return;
        if (event.getHand() == EquipmentSlot.OFF_HAND) return;
        if (event.getAction() != org.bukkit.event.block.Action.RIGHT_CLICK_BLOCK
                && event.getAction() != Action.LEFT_CLICK_BLOCK
                || event.getClickedBlock() == null) return;

        Block block = event.getClickedBlock();
        Team team = Game.get().isFlag(block.getType(), block.getLocation());

        if (team == null) return;
        player.sendMessage("You interacted with the " + team.getDisplayName() + " flag!");
    }

}
