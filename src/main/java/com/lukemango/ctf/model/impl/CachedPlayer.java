package com.lukemango.ctf.model.impl;

import com.lukemango.ctf.util.FinePosition;
import org.bukkit.Location;
import org.bukkit.inventory.ItemStack;

import java.util.UUID;

public class CachedPlayer {

    private ItemStack[] inventoryContents;
    private FinePosition position;

    public CachedPlayer(ItemStack[] inventoryContents, Location position) {
        this.inventoryContents = inventoryContents;
        this.position = new FinePosition(
                position.getBlockX(),
                position.getBlockY(),
                position.getBlockZ(),
                position.getWorld().getName()
        );
    }

    public ItemStack[] getInventoryContents() {
        return inventoryContents;
    }

    public void setInventoryContents(ItemStack[] inventoryContents) {
        this.inventoryContents = inventoryContents;
    }

    public FinePosition getPosition() {
        return position;
    }

    public void setPosition(FinePosition position) {
        this.position = position;
    }
}
