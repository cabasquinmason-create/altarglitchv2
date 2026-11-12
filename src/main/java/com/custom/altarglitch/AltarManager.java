package com.custom.altarglitch;

import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public class AltarManager {
    private final AltarGlitchPlugin plugin;
    private final Map<Location, GlitchType> altars = new HashMap<>();
    private final Set<GlitchType> craftedGlobal = new HashSet<>();
    private final Map<UUID, GlitchType> playerEquipped = new HashMap<>();

    public AltarManager(AltarGlitchPlugin plugin) {
        this.plugin = plugin;
    }

    public void spawnAltar(Location loc, GlitchType type) {
        Block block = loc.getBlock();
        block.setType(Material.LODESTONE);
        Location blockLoc = block.getLocation();
        altars.put(blockLoc, type);

        Location holoLoc = blockLoc.clone().add(0.5, 1.3, 0.5);
        ArmorStand stand = (ArmorStand) loc.getWorld().spawnEntity(holoLoc, EntityType.ARMOR_STAND);
        stand.setVisible(false);
        stand.setGravity(false);
        stand.setMarker(true);
        stand.setCustomNameVisible(true);
        stand.setCustomName(type.displayName + " §f(" + type.ingredientCost + "x " + type.ingredient.name() + ")");
    }

    public boolean handleAltarInteract(Player player, Block block) {
        GlitchType type = altars.get(block.getLocation());
        if (type == null) return false;

        if (craftedGlobal.contains(type)) {
            player.sendMessage("§cThis Glitch Relic has already been forged on this server!");
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1f, 0.5f);
            return true;
        }

        ItemStack required = new ItemStack(type.ingredient, type.ingredientCost);
        if (!player.getInventory().containsAtLeast(required, type.ingredientCost)) {
            player.sendMessage("§eAltar requires §b" + type.ingredientCost + "x " + type.ingredient.name() + " §eto forge!");
            return true;
        }

        player.getInventory().removeItem(required);
        craftedGlobal.add(type);
        player.getInventory().addItem(type.createItem(plugin.getGlitchKey()));

        Bukkit.broadcastMessage("§8[§5AltarSMP§8] §d" + player.getName() + " §7has forged the " + type.displayName + "§7!");
        block.getWorld().strikeLightningEffect(block.getLocation().add(0.5, 1, 0.5));
        return true;
    }

    public void openGlitchesGUI(Player player) {
        Inventory gui = Bukkit.createInventory(null, 9, "§8Glitch Weapon Loadout");

        ItemStack filler = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        for (int i = 0; i < 9; i++) gui.setItem(i, filler);

        GlitchType equipped = playerEquipped.get(player.getUniqueId());
        gui.setItem(4, equipped != null ? equipped.createItem(plugin.getGlitchKey()) : new ItemStack(Material.BARRIER));

        player.openInventory(gui);
    }

    public void setEquipped(UUID uuid, GlitchType type) {
        if (type == null) playerEquipped.remove(uuid);
        else playerEquipped.put(uuid, type);
    }

    public GlitchType getEquipped(UUID uuid) {
        return playerEquipped.get(uuid);
    }

    public void resetCrafted() {
        craftedGlobal.clear();
    }
}
