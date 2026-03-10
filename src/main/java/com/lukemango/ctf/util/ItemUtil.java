package com.lukemango.ctf.util;

import com.lukemango.ctf.model.impl.Team;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.LeatherArmorMeta;

public class ItemUtil {

    // Gives the player stone sword, axe, and coloured armor
    public static void giveTeamEquipment(Player player, Team team) {
        PlayerInventory inv = player.getInventory();

        // Weapons
        inv.addItem(new ItemStack(Material.STONE_SWORD));
        inv.addItem(new ItemStack(Material.STONE_AXE));

        // Parse team colour
        String[] rgb = team.getColor().split(",");
        int r = Integer.parseInt(rgb[0].trim());
        int g = Integer.parseInt(rgb[1].trim());
        int b = Integer.parseInt(rgb[2].trim());

        Color color = Color.fromRGB(r, g, b);

        // Armor
        inv.setHelmet(createColoredArmor(Material.LEATHER_HELMET, color));
        inv.setChestplate(createColoredArmor(Material.LEATHER_CHESTPLATE, color));
        inv.setLeggings(createColoredArmor(Material.LEATHER_LEGGINGS, color));
        inv.setBoots(createColoredArmor(Material.LEATHER_BOOTS, color));
    }

    private static ItemStack createColoredArmor(Material material, Color color) {
        ItemStack item = new ItemStack(material);

        LeatherArmorMeta meta = (LeatherArmorMeta) item.getItemMeta();
        meta.setColor(color);

        item.setItemMeta(meta);
        return item;
    }
}
