package com.lukemango.ctf.model.impl;

import com.lukemango.ctf.util.FinePosition;
import org.bukkit.inventory.ItemStack;

public record CachedPlayer(ItemStack[] inventoryContents, ItemStack[] armorContents, ItemStack[] extraContents,
                           FinePosition position) {

}
