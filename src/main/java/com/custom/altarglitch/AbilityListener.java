package com.custom.altarglitch;

import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.*;

public class AbilityListener implements Listener {
    private final AltarGlitchPlugin plugin;
    private final AltarManager manager;
    private final Map<String, Long> cooldowns = new HashMap<>();

    public AbilityListener(AltarGlitchPlugin plugin, AltarManager manager) {
        this.plugin = plugin;
        this.manager = manager;
    }

    @EventHandler
    public void onBlockInteract(PlayerInteractEvent e) {
        if (e.getHand() != EquipmentSlot.HAND) return;

        if (e.getAction() == Action.RIGHT_CLICK_BLOCK && e.getClickedBlock() != null) {
            if (manager.handleAltarInteract(e.getPlayer(), e.getClickedBlock())) {
                e.setCancelled(true);
                return;
            }
        }

        if (e.getAction() == Action.RIGHT_CLICK_AIR || e.getAction() == Action.RIGHT_CLICK_BLOCK) {
            ItemStack item = e.getPlayer().getInventory().getItemInMainHand();
            GlitchType glitch = extractGlitch(item);
            if (glitch == GlitchType.FREEZE_BLADE) {
                e.setCancelled(true);
                triggerFreeze(e.getPlayer());
            }
        }
    }

    @EventHandler
    public void onGuiClose(InventoryCloseEvent e) {
        if (!e.getView().getTitle().equals("§8Glitch Weapon Loadout")) return;
        Player player = (Player) e.getPlayer();
        ItemStack centerItem = e.getInventory().getItem(4);
        GlitchType glitch = extractGlitch(centerItem);
        manager.setEquipped(player.getUniqueId(), glitch);
        player.sendMessage("§aGlitch Loadout updated!");
    }

    private GlitchType extractGlitch(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return null;
        String id = item.getItemMeta().getPersistentDataContainer().get(plugin.getGlitchKey(), PersistentDataType.STRING);
        return GlitchType.fromId(id);
    }

    private void triggerFreeze(Player player) {
        String cdKey = player.getUniqueId() + ":freeze_blade";
        long now = System.currentTimeMillis();
        if (cooldowns.containsKey(cdKey) && cooldowns.get(cdKey) > now) {
            long left = (cooldowns.get(cdKey) - now) / 1000;
            player.sendMessage("§cGlitch Frost Blade is on cooldown for §e" + left + "s§c!");
            return;
        }

        Entity target = player.getTargetEntity(15);
        if (!(target instanceof Player victim)) {
            player.sendMessage("§cLook at a player within 15 blocks to freeze them!");
            return;
        }

        cooldowns.put(cdKey, now + (GlitchType.FREEZE_BLADE.cooldownSeconds * 1000L));
        trapInIceBox(victim);
        player.sendMessage("§b[Cryo Lock] §fFroze §b" + victim.getName() + " §ffor 5s!");
    }

    private void trapInIceBox(Player victim) {
        Location center = victim.getLocation().getBlock().getLocation();
        List<Block> cage = new ArrayList<>();
        for (int x = -1; x <= 1; x++) {
            for (int y = 0; y <= 2; y++) {
                for (int z = -1; z <= 1; z++) {
                    if (x == 0 && z == 0 && (y == 0 || y == 1)) continue;
                    Block b = center.clone().add(x, y, z).getBlock();
                    if (b.getType() == Material.AIR) {
                        b.setType(Material.PACKED_ICE);
                        cage.add(b);
                    }
                }
            }
        }
        Bukkit.getScheduler().runTaskLater(plugin, () -> cage.forEach(b -> b.setType(Material.AIR)), 100L);
    }

    public void startChestplateTask() {
        Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            for (Player player : Bukkit.getOnlinePlayers()) {
                ItemStack chest = player.getInventory().getChestplate();
                if (extractGlitch(chest) == GlitchType.GLITCH_CHESTPLATE) {
                    player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 40, 1, true, false));
                    player.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, 40, 0, true, false));
                }
            }
        }, 0L, 20L);
    }
}
