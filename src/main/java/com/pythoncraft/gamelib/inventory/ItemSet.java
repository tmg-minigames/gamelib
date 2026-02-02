package com.pythoncraft.gamelib.inventory;

import java.util.List;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class ItemSet {
    /* Expresses a set of one or more ItemStacks/ItemTemplates */

    public List<ItemStack> items;

    public ItemSet(List<ItemStack> itemList) {this.items = itemList.stream().filter(itemStack -> itemStack != null).toList();}
    public ItemSet(ItemStack item) {this.items = List.of(item);}
    public ItemSet() {this.items = List.of();}

    public ItemSet clone() {
        return new ItemSet(this.items.stream().map(itemStack -> itemStack.clone()).toList());
    }

    public void giveToPlayer(Player player) {
        for (ItemStack item : items) {
            player.getInventory().addItem(item.clone());
        }
    }
}
