package com.pythoncraft.gamelib.inventory;

import java.util.HashMap;
import java.util.List;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class ItemSet {
    /* Expresses a set of one or more ItemStacks/ItemTemplates */

    public List<ItemStack> items;
    public HashMap<String, String> metadata = new HashMap<>();

    public ItemSet(List<ItemStack> itemList, HashMap<String, String> metadata) {
        this.items = itemList.stream().filter(itemStack -> itemStack != null).toList();
        this.metadata = metadata;
    }

    public ItemSet(List<ItemStack> itemList) {this(itemList, new HashMap<>());}
    public ItemSet(ItemStack item, HashMap<String, String> metadata) {this(List.of(item), metadata);}
    public ItemSet(ItemStack item) {this(List.of(item), new HashMap<>());}
    public ItemSet() {this.items = List.of();}

    public ItemSet clone() {
        return new ItemSet(this.items.stream().map(itemStack -> itemStack.clone()).toList(), this.metadata);
    }

    public void giveToPlayer(Player player) {
        for (ItemStack item : items) {
            player.getInventory().addItem(item.clone());
        }
    }

    public String getMetadata(String key) {return this.metadata.get(key);}
    public String getMetadata(String key, String defaultValue) {return this.metadata.getOrDefault(key, defaultValue);}

    public int getMetadataInt(String key, int defaultValue) {
        String value = this.metadata.get(key);
        if (value == null) {return defaultValue;}
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    public boolean hasMetadata(String key) {
        return this.metadata.containsKey(key);
    }
}
