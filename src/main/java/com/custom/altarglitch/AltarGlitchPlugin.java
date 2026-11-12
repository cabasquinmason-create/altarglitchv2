package com.custom.altarglitch;

import org.bukkit.NamespacedKey;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class AltarGlitchPlugin extends JavaPlugin {
    private NamespacedKey glitchKey;
    private AltarManager altarManager;
    private AbilityListener abilityListener;

    @Override
    public void onEnable() {
        this.glitchKey = new NamespacedKey(this, "glitch_id");
        this.altarManager = new AltarManager(this);
        this.abilityListener = new AbilityListener(this, altarManager);
        
        getServer().getPluginManager().registerEvents(abilityListener, this);
        abilityListener.startChestplateTask();
        
        getLogger().info("AltarGlitchSMP enabled!");
    }

    public NamespacedKey getGlitchKey() {
        return glitchKey;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) return true;

        if (command.getName().equalsIgnoreCase("glitches") || command.getName().equalsIgnoreCase("glitch")) {
            altarManager.openGlitchesGUI(player);
            return true;
        }

        if (command.getName().equalsIgnoreCase("altar")) {
            if (args.length == 2 && args[0].equalsIgnoreCase("spawn")) {
                GlitchType type = GlitchType.fromId(args[1]);
                if (type == null) {
                    player.sendMessage("§cValid types: freeze_blade, glitch_chestplate");
                    return true;
                }
                altarManager.spawnAltar(player.getLocation(), type);
                player.sendMessage("§aSpawned Altar at your location!");
                return true;
            }
            if (args.length == 1 && args[0].equalsIgnoreCase("reset")) {
                altarManager.resetCrafted();
                player.sendMessage("§eAll 1-of-1 server craft limits have been reset.");
                return true;
            }
            player.sendMessage("§eUsage: /altar spawn <freeze_blade|glitch_chestplate> | /altar reset");
        }
        return true;
    }
}
