package com.custom.altarglitch;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.inventory.EquipmentSlotGroup;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.List;

public enum GlitchType {
    FREEZE_BLADE(
            "freeze_blade",
            "§b§l[RELIC] §3Glitch Frost Blade",
            Material.DIAMOND_SWORD,
            Material.DIAMOND, 16,
            35,
            List.of("§7Right-click to freeze your target", "§7in a packed ice box for §e5 seconds§7.")
    ),
    GLITCH_CHESTPLATE(
            "glitch_chestplate",
            "§5§l[RELIC] §dGlitch Overclocked Chestplate",
            Material.NETHERITE_CHESTPLATE,
            Material.NETHERITE_INGOT, 2,
            0,
            List.of("§7Grants permanent Speed II &", "§7Resistance while worn.")
    );

    public final String id;
    public final String displayName;
    public final Material icon;
    public final Material ingredient;
    public final int ingredientCost;
    public final int cooldownSeconds;
    public final List<String> lore;

    GlitchType(String id, String displayName, Material icon, Material ingredient, int ingredientCost, int cooldownSeconds, List<String> lore) {
        this.id = id;
        this.displayName = displayName;
        this.icon = icon;
        this.ingredient = ingredient;
        this.ingredientCost = ingredientCost;
        this.cooldownSeconds = cooldownSeconds;
        this.lore = lore;
    }

    public ItemStack createItem(NamespacedKey key) {
        ItemStack item = new ItemStack(icon);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(Component.text(displayName).decoration(TextDecoration.ITALIC, false));
        meta.setLore(lore);
        meta.getPersistentDataContainer().set(key, PersistentDataType.STRING, id);
        
        if (this == GLITCH_CHESTPLATE) {
            meta.addAttributeModifier(Attribute.GENERIC_ARMOR, new AttributeModifier(
                    NamespacedKey.minecraft("glitch_armor"), 15.0, AttributeModifier.Operation.ADD_NUMBER, EquipmentSlotGroup.CHEST));
        }

        item.setItemMeta(meta);
        return item;
    }

    public static GlitchType fromId(String id) {
        if (id == null) return null;
        for (GlitchType type : values()) {
            if (type.id.equalsIgnoreCase(id)) return type;
        }
        return null;
    }
}
