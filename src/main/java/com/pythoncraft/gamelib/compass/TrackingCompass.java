package com.pythoncraft.gamelib.compass;

import java.util.UUID;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.CompassMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import com.pythoncraft.gamelib.GameLib;
import com.pythoncraft.gamelib.Chat;

public class TrackingCompass {
    private Player trackedPlayer;
    private Player owner;
    private final String compassUUID;
    private int slot = -1;
    public static final NamespacedKey isCompassKey   = new NamespacedKey(GameLib.getInstance(), "tracking-compass");
    public static final NamespacedKey compassUUIDKey = new NamespacedKey(GameLib.getInstance(), "compass-uuid");

    public TrackingCompass(Player owner, Player trackedPlayer) {
        this.compassUUID = UUID.randomUUID().toString();
        this.owner = owner;
        this.trackedPlayer = trackedPlayer;
    }

    public TrackingCompass(Player owner) {this(owner, null);}

    public void track(Player player) {
        this.trackedPlayer = player;
    }

    public void destroy() {
        this.trackedPlayer = null;
        
        if (this.owner != null && this.slot >= 0) {
            ItemStack item = this.owner.getInventory().getItem(this.slot);
            if (compassUUID.equals(getCompassUUID(item))) {
                item.setAmount(0);
            }
        }
        this.slot = -1;
    }

    public ItemStack createItem() {
        return createTrackingCompass(this.compassUUID);
    }

    public Player getTrackedPlayer() {
        return this.trackedPlayer;
    }

    public String getCompassUUID() {
        return this.compassUUID;
    }

    public Player getOwner() {
        return this.owner;
    }

    public void updateDirection() {
        if (this.owner == null || !this.owner.isOnline()) return;
        if (this.trackedPlayer == null || !this.trackedPlayer.isOnline()) return;
        
        // If slot unknown, try to find compass in inventory
        if (this.slot < 0) {
            findAndSetSlot();
            if (this.slot < 0) return; // Still not found
        }
        
        ItemStack item = this.owner.getInventory().getItem(this.slot);
        
        // Verify compass is still at this slot, otherwise find it
        if (!compassUUID.equals(getCompassUUID(item))) {
            findAndSetSlot();
            if (this.slot < 0) return;
            item = this.owner.getInventory().getItem(this.slot);
        }
        
        CompassMeta meta = (CompassMeta) item.getItemMeta();
        meta.setLodestoneTracked(false);
        meta.setLodestone(this.trackedPlayer.getLocation());
        item.setItemMeta(meta);
        
        // Set item back to force client update
        this.owner.getInventory().setItem(this.slot, item);
    }

    private void findAndSetSlot() {
        if (this.owner == null) return;
        
        for (int i = 0; i < this.owner.getInventory().getSize(); i++) {
            ItemStack item = this.owner.getInventory().getItem(i);
            if (compassUUID.equals(getCompassUUID(item))) {
                this.slot = i;
                return;
            }
        }
        this.slot = -1;
    }

    

    public static ItemStack createTrackingCompass(String uuid) {
        ItemStack compass = new ItemStack(Material.COMPASS);
        ItemMeta meta = compass.getItemMeta();
        
        meta.displayName(Chat.component("§d§lTracking Compass"));
        meta.getPersistentDataContainer().set(isCompassKey, PersistentDataType.BOOLEAN, true);
        meta.getPersistentDataContainer().set(compassUUIDKey, PersistentDataType.STRING, uuid);

        compass.setItemMeta(meta);

        return compass;
    }

    public static boolean isTrackingCompass(ItemStack item) {
        if (item == null || item.getType() != Material.COMPASS) {return false;}

        ItemMeta meta = item.getItemMeta();
        if (meta == null) {return false;}

        return meta.getPersistentDataContainer().has(isCompassKey, PersistentDataType.BOOLEAN);
    }

    public static String getCompassUUID(ItemStack item) {
        if (!isTrackingCompass(item)) {return null;}

        ItemMeta meta = item.getItemMeta();
        return meta.getPersistentDataContainer().get(compassUUIDKey, PersistentDataType.STRING);
    }
}