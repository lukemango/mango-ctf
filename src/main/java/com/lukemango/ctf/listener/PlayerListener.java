package com.lukemango.ctf.listener;

import com.lukemango.ctf.config.ConfigManager;
import com.lukemango.ctf.config.impl.Messages;
import com.lukemango.ctf.model.Game;
import com.lukemango.ctf.model.impl.Team;
import com.lukemango.ctf.util.ItemUtil;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.Map;
import java.util.UUID;

public class PlayerListener implements Listener {

    @EventHandler(ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        if (!Game.get().isActive()) return;
        if (Game.get().isInGame(event.getPlayer())) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();

        if (!Game.get().isActive()) return;
        if (!Game.get().isInGame(player)) return;
        this.attemptStealFlag(event, player);
        this.attemptTakeOwnFlagBack(event, player);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();

        if (!Game.get().isActive()) return;
        if (!Game.get().isInGame(player)) return;
        this.attemptClaimFlag(event, player);
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Game.get().leave(event.getPlayer());
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Game.get().restoreInventory(event.getPlayer());
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();

        if (!Game.get().isActive()) return;
        if (!Game.get().isInGame(player)) return;
        this.attemptDropFlag(player);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        Player player = event.getPlayer();

        if (!Game.get().isActive()) return;
        if (!Game.get().isInGame(player)) return;

        Team team = Game.get().getPlayerTeam(player);
        if (team == null) return; // Shouldn't happen, but just in case

        event.setRespawnLocation(team.getFlagLocation().toLocation());
        ItemUtil.giveTeamEquipment(player, team);
    }

    private void attemptDropFlag(Player player) {
        Map<UUID, String> stolenFlags = Game.get().getFlagCarriers();
        if (!stolenFlags.containsKey(player.getUniqueId())) return; // Player is not carrying a flag

        String teamName = stolenFlags.get(player.getUniqueId());
        Team team = Game.get().getTeam(teamName);
        if (team == null) return; // Shouldn't happen, but just in case

        Location dropLocation = player.getLocation();
        dropLocation.getBlock().setType(team.getFlagMaterial());
        team.setDroppedFlagLocation(dropLocation);

        stolenFlags.remove(player.getUniqueId());

        // Broadcast message
        ConfigManager.get().getMessages().sendPlayerDroppedFlag(Game.get().getAudience(),
                player.getName(),
                team.getDisplayName()
        );
    }

    private void attemptTakeOwnFlagBack(PlayerInteractEvent event, Player player) {
        if (event.getHand() == EquipmentSlot.OFF_HAND) return;
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK
                && event.getAction() != Action.LEFT_CLICK_BLOCK
                || event.getClickedBlock() == null) return;

        Block block = event.getClickedBlock();
        Team team = Game.get().isDroppedFlag(block.getType(), block.getLocation());

        if (team == null) return;
        if (!team.getMembers().contains(player.getUniqueId())) return; // Player is trying to take back another team's dropped flag

        // Set the flag block to air to simulate it being picked up
        block.setType(Material.AIR);
        team.setDroppedFlagLocation(null);

        // Add glow effect to the player to indicate they are carrying the flag
        player.addPotionEffect(new PotionEffect(PotionEffectType.GLOWING,
                Integer.MAX_VALUE,
                0,
                false,
                false
        ));

        // Add the player to the flag carriers map
        Game.get().getFlagCarriers().put(player.getUniqueId(), team.getName());

        // Broadcast message
        ConfigManager.get().getMessages().sendPlayerPickedUpDroppedFlag(Game.get().getAudience(),
                player.getName(),
                team.getDisplayName()
        );
    }

    private void attemptStealFlag(PlayerInteractEvent event, Player player) {
        if (event.getHand() == EquipmentSlot.OFF_HAND) return;
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK
                && event.getAction() != Action.LEFT_CLICK_BLOCK
                || event.getClickedBlock() == null) return;

        Messages messages = ConfigManager.get().getMessages();
        Block block = event.getClickedBlock();
        Team team = Game.get().isFlag(block.getType(), block.getLocation());

        if (team == null) return;
        if (team.getMembers().contains(player.getUniqueId())) { // Player is trying to steal their own flag
            messages.sendPlayerCantStealOwnFlag(player);
            return;
        }

        Map<UUID, String> stolenFlags = Game.get().getFlagCarriers();
        if (stolenFlags.containsKey(player.getUniqueId())) { // Player is already carrying a flag
            messages.sendPlayerAlreadyCarryingFlag(player);
            return;
        }

        if (stolenFlags.containsValue(team.getName())) { // Flag is already stolen by another player
            messages.sendPlayerFlagAlreadyStolen(player);
            return;
        }

        stolenFlags.put(player.getUniqueId(), team.getName());
        team.setDroppedFlagLocation(null);

        // Give player glow effect
        player.addPotionEffect(new PotionEffect(PotionEffectType.GLOWING,
                Integer.MAX_VALUE,
                0,
                false,
                false
        ));

        // Set the flag block to air to simulate it being stolen
        block.setType(org.bukkit.Material.AIR);

        // Broadcast message
        messages.sendPlayerFlagStolen(Game.get().getAudience(),
                player.getName(),
                team.getDisplayName()
        );
    }

    private void attemptClaimFlag(PlayerMoveEvent event, Player player) {
        Map<UUID, String> stolenFlags = Game.get().getFlagCarriers();
        if (!stolenFlags.containsKey(player.getUniqueId())) return; // Player is not carrying a flag

        Messages messages = ConfigManager.get().getMessages();
        Team stolenFlagTeam = Game.get().getTeam(stolenFlags.get(player.getUniqueId()));
        Team playerTeam = Game.get().getPlayerTeam(player);

        if (stolenFlagTeam == null) return; // Shouldn't happen, but just in case

        Location playerLocation = event.getTo();
        Location claimLocation = playerTeam.getFlagLocation()
                .toLocation()
                .clone()
                .add(0, -2, 0); // -2 to account for the two fences on top of the flag

        // If player has gone within 2 blocks of their flag location, claim the flag
        if (playerLocation.distance(claimLocation) > 2) return;

        player.removePotionEffect(PotionEffectType.GLOWING);
        stolenFlags.remove(player.getUniqueId());

        // Set the flag block back to its original material
        Block flagBlock = stolenFlagTeam.getFlagLocation().toLocation().getBlock();
        flagBlock.setType(stolenFlagTeam.getFlagMaterial());

        // Broadcast message
        if (playerTeam == stolenFlagTeam) {
            messages.sendPlayerReturnedFlag(Game.get().getAudience(),
                    player.getName(),
                    stolenFlagTeam.getDisplayName()
            );
            return;
        }

        playerTeam.addScore(1);

        int remainingToWin = ConfigManager.get().getConfig().getCapturesToWin() - playerTeam.getScore();
        if (remainingToWin == 0) {
            messages.sendPlayerGameEnded(Game.get().getAudience(), playerTeam);
            Game.get().end(null);
            return;
        }

        messages.sendPlayerFlagCaptured(Game.get().getAudience(),
                player.getName(),
                stolenFlagTeam.getDisplayName(),
                playerTeam.getScore(),
                ConfigManager.get().getConfig().getCapturesToWin()
        );

    }

}
