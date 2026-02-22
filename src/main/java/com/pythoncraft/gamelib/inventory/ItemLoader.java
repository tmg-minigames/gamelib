package com.pythoncraft.gamelib.inventory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.function.Predicate;

import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ArmorMeta;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.inventory.meta.trim.ArmorTrim;
import org.bukkit.inventory.meta.trim.TrimMaterial;
import org.bukkit.inventory.meta.trim.TrimPattern;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import com.pythoncraft.gamelib.Chat;
import com.pythoncraft.gamelib.GameLib;
import com.pythoncraft.gamelib.Logger;

public class ItemLoader {
    public static HashMap<String, String> getEntries(ConfigurationSection section) {
        HashMap<String, String> entries = new HashMap<>();
        for (String key : section.getKeys(false)) {
            if (!section.isConfigurationSection(key)) {
                entries.put(key, section.getString(key));
            }
        }
        return entries;
    }

    public static HashMap<String, ItemStack> loadItemsMap(ConfigurationSection itemsSection) {
        HashMap<String, ItemStack> items = new HashMap<>();
        if (itemsSection == null) {return items;}

        for (String key : itemsSection.getKeys(false)) {
            ConfigurationSection itemSection = itemsSection.getConfigurationSection(key);
            ItemStack itemStack;
            
            if (itemSection == null) {
                // Short version: `stick * 64`
                itemStack = loadShort(itemsSection.getString(key));
            } else {
                // Full version: use loadLong method
                itemStack = loadLong(itemSection, null);
            }
            
            if (itemStack == null) {
                Logger.warn("Failed to load item for key: " + key);
                continue;
            }

            items.put(key, itemStack);
            // Logger.info("Loaded item: " + itemStack.getType().toString());
        }

        return items;
    }

    public static List<ItemStack> loadItems(ConfigurationSection itemsSection) {
        return new ArrayList<>(loadItemsMap(itemsSection).values());
    }

    public static HashMap<String, ItemSet> loadItemSetsMap(ConfigurationSection itemsSection) {
        HashMap<String, ItemSet> items = new HashMap<>();
        if (itemsSection == null) {return items;}

        for (String key : itemsSection.getKeys(false)) {
            ConfigurationSection itemSection = itemsSection.getConfigurationSection(key);
            ItemSet itemSet;
            
            if (itemSection == null) {
                // Short version: `stick * 64;dirt * 32`
                itemSet = loadShortSet(itemsSection.getString(key));
            } else {
                // Full version: use loadSet method
                itemSet = loadSet(itemSection);
            }
            
            if (itemSet == null) {
                Logger.warn("Failed to load item set for key: " + key);
                continue;
            }

            items.put(key, itemSet);
            // Logger.info("Loaded item set with " + itemSet.size() + " items.");
        }

        return items;
    }

    public static List<ItemSet> loadItemSets(ConfigurationSection itemsSection) {
        return new ArrayList<>(loadItemSetsMap(itemsSection).values());
    }

    public static HashMap<String, ItemTemplate> loadConditionalItemsMap(ConfigurationSection itemsSection, HashMap<String, Predicate<Player>> conditions) {
        HashMap<String, ItemTemplate> templates = new HashMap<>();
        if (itemsSection == null) {return templates;}

        for (String itemKey : itemsSection.getKeys(false)) {
            ItemTemplate template;

            ConfigurationSection itemSection = itemsSection.getConfigurationSection(itemKey);
            
            if (itemSection == null) {
                // Short version: `stick * 64`
                template = new ItemTemplate(loadShort(itemsSection.getString(itemKey)));
            } else {
                // Full version
                template = loadConditional(itemSection, conditions);
            }

            if (template == null) {
                Logger.warn("Failed to load item template for key: " + itemKey);
                continue;
            }

            templates.put(itemKey, template);
        }

        return templates;
    }

    public static List<ItemTemplate> loadConditionalItems(ConfigurationSection itemsSection, HashMap<String, Predicate<Player>> conditions) {
        return new ArrayList<>(loadConditionalItemsMap(itemsSection, conditions).values());
    }


    public static ItemStack loadShort(String itemString) {
        if (itemString == null || itemString.isEmpty()) {return GameLib.getItemStack(Chat.c("§c§o§lInvalid item string"));}

        int count = 1;
        if (itemString.contains("*")) {
            String[] parts = itemString.split("\\*");
            itemString = parts[0].trim();
            count = Integer.parseInt(parts[1].trim());
        }
        Material itemMaterial = Material.getMaterial(itemString.toUpperCase());
        if (itemMaterial == null) {return GameLib.getItemStack(Chat.c("§c§o§lUnknown material: " + itemString));}

        ItemStack itemStack = new ItemStack(itemMaterial);
        itemStack.setAmount(count);
        return itemStack;
    }

    public static ItemSet loadShortSet(String items) {
        if (items == null || items.isEmpty()) {return null;}
        List<ItemStack> itemList = new ArrayList<>();

        for (String item : items.split(";")) {
            itemList.add(loadShort(item.trim()));
        }

        return new ItemSet(itemList);
    }

    public static ItemStack loadLong(ConfigurationSection section, ItemStack baseItem) {
        if (section == null) {return null;}
        
        String material = section.getString("id", GameLib.DEFAULT_MATERIAL.toString());
        String customName = section.getString("name", null);
        List<String> lore = section.getStringList("lore");
        Material itemMaterial = Material.getMaterial(material.toUpperCase());
        if (itemMaterial == null) {itemMaterial = GameLib.DEFAULT_MATERIAL;}
        Integer count = section.getInt("count", 1);

        ItemStack itemStack;
        if (baseItem != null) {
            itemStack = baseItem.clone();
            itemStack.setAmount(count);
        } else {
            itemStack = GameLib.getItemStack(itemMaterial, count, customName);
        }

        ItemMeta meta = itemStack.getItemMeta();

        if (customName != null && !customName.isEmpty()) {
            meta.setDisplayName(Chat.c(customName));
        }

        if (lore != null && !lore.isEmpty()) {
            meta.setLore(lore);
        }

        parseEnchantments(itemStack, section);
        parseDurability(itemStack, section);
        parseTrim(itemStack, section);
        parsePotion(itemStack, section);

        return itemStack;
    }

    public static ItemStack loadLong(ConfigurationSection section) {
        return loadLong(section, null);
    }

    public static ItemSet loadSet(ConfigurationSection section) {
        if (section == null) {return null;}

        String itemId = section.getString("id", null);

        if (itemId != null && itemId.equalsIgnoreCase("set")) {
            List<ItemStack> items = new ArrayList<>();
            ConfigurationSection itemsSection = section.getConfigurationSection("items");
            if (itemsSection == null) {
                Logger.warn("Item set section missing 'items' subsection - " + section.getCurrentPath() + ".");
                return new ItemSet(GameLib.getItemStack(), getEntries(section));
            }

            for (String key : itemsSection.getKeys(false)) {
                ConfigurationSection itemSection = itemsSection.getConfigurationSection(key);
                ItemStack item;
                
                if (itemSection == null) {
                    // Short version: `iron_sword * 64`
                    item = loadShort(itemsSection.getString(key));
                } else {
                    // Full version: use loadLong method
                    item = loadLong(itemSection);
                }
                
                if (item != null) {
                    items.add(item);
                } else {
                    Logger.warn("Failed to load item in set for key: " + key);
                }
            }

            return new ItemSet(items, getEntries(section));
        } else {
            return new ItemSet(loadLong(section), getEntries(section));
        }
    }

    public static ItemTemplate loadConditional(ConfigurationSection section, HashMap<String, Predicate<Player>> conditions) {
        if (section == null) {return null;}
        
        ItemTemplate template = new ItemTemplate();

        ItemStack defaultItem = loadLong(section);
        template.addItem(defaultItem, player -> true);

        for (String conditionKey : conditions.keySet()) {
            if (section.contains(conditionKey)) {
                Logger.info("Found condition {0} for item template at {1}.", conditionKey, section.getCurrentPath());
                ItemStack conditionalItem = loadLong(section.getConfigurationSection(conditionKey), defaultItem);
                template.addItem(conditionalItem, conditions.get(conditionKey));
            }
        }

        Logger.info("Loaded item template with {0} conditional items: {1}", template.items.size(), section.getCurrentPath());

        return template;
    }


    private static void parseEnchantments(ItemStack itemStack, ConfigurationSection section) {
        ConfigurationSection enchantmentsSection = section.getConfigurationSection("enchantments");
        if (enchantmentsSection == null) {return;}

        ItemMeta meta = itemStack.getItemMeta();

        for (String enchantmentKey : enchantmentsSection.getKeys(false)) {
            int level = enchantmentsSection.getInt(enchantmentKey, 1);
            meta.addEnchant(ItemLoader.getEnchantmentByName(enchantmentKey), level, true);
        }

        itemStack.setItemMeta(meta);
    }

    private static void parseDurability(ItemStack itemStack, ConfigurationSection section) {
        if (!section.contains("durability")) {return;}

        int durability = section.getInt("durability", -1);
        if (durability < 0) {return;}

        ItemMeta meta = itemStack.getItemMeta();
        
        if (durability == 0) {
            meta.setUnbreakable(true);
        } else if (meta instanceof Damageable damageable) {
            meta.setUnbreakable(false);
            int maxDurability = itemStack.getType().getMaxDurability();
            if (durability > maxDurability) {durability = maxDurability;}

            damageable.setDamage(maxDurability - durability);
        }

        itemStack.setItemMeta(meta);
    }

    private static void parseTrim(ItemStack itemStack, ConfigurationSection section) {
        if (!section.contains("trim-material") && !section.contains("trim-pattern")) {return;}

        TrimMaterial trimMaterial = ItemLoader.getTrimMaterialByName(section.getString("trim-material", "").toLowerCase());
        TrimPattern trimPattern = ItemLoader.getTrimPatternByName(section.getString("trim-pattern", "").toLowerCase());

        ItemMeta meta = itemStack.getItemMeta();

        if (!(meta instanceof ArmorMeta armorMeta)) {return;}

        if (trimMaterial != null && trimPattern != null) {
            armorMeta.setTrim(new ArmorTrim(trimMaterial, trimPattern));
        } else if (trimMaterial != null) {
            armorMeta.setTrim(new ArmorTrim(trimMaterial, armorMeta.getTrim().getPattern()));
        } else if (trimPattern != null) {
            armorMeta.setTrim(new ArmorTrim(armorMeta.getTrim().getMaterial(), trimPattern));
        }
    }

    private static void parsePotion(ItemStack itemStack, ConfigurationSection section) {
        if (!section.contains("effects") && !section.contains("potion-color")) {return;}

        ItemMeta meta = itemStack.getItemMeta();
        if (!(meta instanceof PotionMeta potionMeta)) {return;}

        ConfigurationSection effectsSection = section.getConfigurationSection("effects");
        if (effectsSection != null) {
            ItemLoader.loadPotionEffects(effectsSection).forEach(effect -> {
                potionMeta.addCustomEffect(effect, true);
            });
        }

        if (section.contains("potion-color")) {
            String colorString = section.getString("potion-color", "#FFFFFF").replace("#", "").toUpperCase();

            try {
                Color potionColor = Color.fromRGB(Integer.parseInt(colorString, 16));
                potionMeta.setColor(potionColor);
            } catch (Exception e) {
                Logger.error("Invalid potion color: " + colorString);
            }
        }

        itemStack.setItemMeta(meta);
    }


    public static List<PotionEffect> loadPotionEffects(ConfigurationSection effectsSection) {
        List<PotionEffect> potionEffects = new ArrayList<>();

        if (effectsSection != null) {
            for (String effectKey : effectsSection.getKeys(false)) {
                var effectSection = effectsSection.getConfigurationSection(effectKey);
                if (effectSection == null) {continue;}

                int duration = effectSection.getInt("duration", 0);
                int amplifier = effectSection.getInt("amplifier", 0);
                boolean hide = effectSection.getBoolean("hide", false);

                PotionEffectType effectType = getPotionEffectTypeByName(effectKey);
                if (effectType == null) {
                    Logger.error("Unknown potion effect type: {0}", effectKey);
                    continue;
                }
                
                PotionEffect effect = new PotionEffect(effectType, duration, amplifier, false, hide);
                potionEffects.add(effect);
            }
        }
        
        return potionEffects;
    }

    public static TrimMaterial getTrimMaterialByName(String name) {
        if (name == null || name.isEmpty()) {return null;}
        return Registry.TRIM_MATERIAL.get(NamespacedKey.minecraft(name.toLowerCase()));
    }

    public static TrimPattern getTrimPatternByName(String name) {
        if (name == null || name.isEmpty()) {return null;}
        return Registry.TRIM_PATTERN.match(name.toLowerCase());
    }

    public static Enchantment getEnchantmentByName(String name) {
        return Registry.ENCHANTMENT.get(NamespacedKey.minecraft(name.toLowerCase()));
    }

    public static PotionEffectType getPotionEffectTypeByName(String name) {
        return Registry.EFFECT.get(NamespacedKey.minecraft(name.toLowerCase()));
    }

    public static Location getLocationFromSection(ConfigurationSection locationSection, World world) {
        if (locationSection == null) {return null;}

        double x = locationSection.getDouble("x", 0);
        double y = locationSection.getDouble("y", 0);
        double z = locationSection.getDouble("z", 0);
        float yaw = (float) locationSection.getDouble("yaw", 0);
        float pitch = (float) locationSection.getDouble("pitch", 0);
        if (world == null) {
            String worldName = locationSection.getString("world");
            world = GameLib.getInstance().getServer().getWorld(worldName);
        }

        if (world == null) {return null;}
        return new Location(world, x + 0.5, y, z + 0.5, yaw, pitch);
    }

    public static Location getLocationFromSection(ConfigurationSection locationSection) {
        return getLocationFromSection(locationSection, null);
    }

    public static Location getLocationFromSequence(String sequence, World world) {
        if (sequence == null || sequence.isEmpty() || world == null) {return null;}

        String[] parts = sequence.split(",");
        if (parts.length < 3) {return null;}

        double x = Double.parseDouble(parts[0].trim());
        double y = Double.parseDouble(parts[1].trim());
        double z = Double.parseDouble(parts[2].trim());
        float yaw = 0;
        float pitch = 0;

        if (parts.length == 5) {
            yaw = Float.parseFloat(parts[3].trim());
            pitch = Float.parseFloat(parts[4].trim());
        }

        if (parts.length == 4 || parts.length > 5) {return null;}

        return new Location(world, x, y, z, yaw, pitch);
    }
}
