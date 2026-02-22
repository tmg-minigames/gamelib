package com.pythoncraft.gamelib.compass;

import java.util.HashMap;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.CompassMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import com.pythoncraft.gamelib.Chat;
import com.pythoncraft.gamelib.GameLib;
import com.pythoncraft.gamelib.PlayerActions;
import com.pythoncraft.gamelib.gui.GUIClickEvent;
import com.pythoncraft.gamelib.gui.GUIIdentifier;
import com.pythoncraft.gamelib.gui.GUIManager;

public class CompassManager implements Listener {
    private static CompassManager instance;
    private final HashMap<String, TrackingCompass> activeCompasses = new HashMap<>();
    private final String showCoordsPattern;
    private final ShowWhen showWhen;

    public CompassManager(String showCoordsPattern, ShowWhen showWhen) {
        /* Available placeholders for showCoordsPattern: {TARGET}, {OWNER}, {X}, {Y}, {Z}, {DISTANCE} */
        this.showCoordsPattern = showCoordsPattern;
        this.showWhen = showWhen;
        
        GameLib gameLib = GameLib.getInstance();
        gameLib.getServer().getPluginManager().registerEvents(this, gameLib);
        
        instance = this;

        GUIManager.getInstance().register("compassMenu", true, (player) -> {
            Inventory inventory = Bukkit.createInventory(new GUIIdentifier("compassMenu"), 27, Chat.c("§lTracking Compass Menu"));

            int i = 0;
            for (Player p : Bukkit.getOnlinePlayers()) {
                if (i >= 27 || p == player) continue;
                if (p == player) {continue;}

                ItemStack head = PlayerActions.getPlayerHead(p);
                inventory.setItem(i, head);
                i++;
            }

            return inventory;
        });
    }

    public CompassManager(String showCoordsPattern) {this(showCoordsPattern, ShowWhen.IN_INVENTORY);}

    public static CompassManager getInstance() {return instance;}

    public TrackingCompass createTrackingCompass(Player owner) {
        TrackingCompass compass = new TrackingCompass(owner);
        this.addCompass(compass);
        return compass;
    }

    public void addCompass(TrackingCompass compass) {
        this.activeCompasses.put(compass.getCompassUUID(), compass);
    }

    public void removeCompass(String uuid) {
        TrackingCompass compass = this.activeCompasses.get(uuid);
        if (compass != null) {compass.destroy();}

        this.activeCompasses.remove(uuid);
    }

    public void removeCompass(TrackingCompass compass) {
        if (compass == null) {return;}
        this.removeCompass(compass.getCompassUUID());
    }

    public HashMap<String, TrackingCompass> getActiveCompasses() {
        return new HashMap<>(this.activeCompasses);
    }

    private boolean coords(Player player, ItemStack compass) {
        if (!TrackingCompass.isTrackingCompass(compass)) {return false;}

        ItemMeta itemMeta = compass.getItemMeta();
        String uuid = itemMeta.getPersistentDataContainer().get(TrackingCompass.compassUUIDKey, PersistentDataType.STRING);
        TrackingCompass trackingCompass = activeCompasses.get(uuid);

        if (trackingCompass == null) {return false;}

        Player trackedPlayer = trackingCompass.getTrackedPlayer();
        if (trackedPlayer == null || !trackedPlayer.isOnline()) {return false;}

        if (!(itemMeta instanceof CompassMeta)) {return false;}
        CompassMeta compassMeta = (CompassMeta) itemMeta;

        compassMeta.setLodestoneTracked(false);
        compassMeta.setLodestone(trackedPlayer.getLocation());
        
        compass.setItemMeta(compassMeta);

        Location loc = trackedPlayer.getLocation();

        int x = loc.getBlockX();
        int y = loc.getBlockY();
        int z = loc.getBlockZ();
        double distance = Math.round(player.getLocation().distance(loc));

        String message = showCoordsPattern
            .replace("{TARGET}", trackedPlayer.getName())
            .replace("{OWNER}", player.getName())
            .replace("{X}", String.valueOf(x))
            .replace("{Y}", String.valueOf(y))
            .replace("{Z}", String.valueOf(z))
            .replace("{DISTANCE}", String.valueOf((int) distance));

        Chat.actionBar(player, message);

        return true;
    }
    
    public void update() {
        for (TrackingCompass compass : activeCompasses.values()) {
            compass.updateDirection();
        }

        for (Player player : Bukkit.getOnlinePlayers()) {
            Inventory inventory = player.getInventory();

            switch (showWhen) {
                case IN_INVENTORY -> {
                    for (ItemStack compass : inventory.all(Material.COMPASS).values()) {
                        if (coords(player, compass)) {break;}
                    }
                }
                case IN_HOTBAR -> {
                    for (int i = 0; i < 9; i++) {
                        ItemStack compass = inventory.getItem(i);
                        if (compass != null && coords(player, compass)) {break;}
                    }
                }
                case IN_HAND -> {
                    ItemStack mainHand = player.getInventory().getItemInMainHand();
                    if (coords(player, mainHand)) {break;}
                    ItemStack offHand = player.getInventory().getItemInOffHand();
                    coords(player, offHand);
                }
            }
        }
    }

    @EventHandler
    public void onItemDrop(PlayerDropItemEvent event) {
        Item item = event.getItemDrop();
        ItemStack itemStack = item.getItemStack();

        if (TrackingCompass.isTrackingCompass(itemStack)) {
            String uuid = itemStack.getItemMeta().getPersistentDataContainer().get(TrackingCompass.compassUUIDKey, PersistentDataType.STRING);
            item.remove();

            removeCompass(uuid);
        }
    }

    @EventHandler
    public void onRightClick(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack item = event.getItem();

        if (TrackingCompass.isTrackingCompass(item) && event.getAction().toString().contains("RIGHT_CLICK")) {
            String uuid = TrackingCompass.getCompassUUID(item);
            TrackingCompass compass = activeCompasses.get(uuid);

            if (compass != null) {
                GUIManager.getInstance().open("compassMenu", player);
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onGUIClick(GUIClickEvent event) {
        if (!event.getID().equals("compassMenu")) {return;}
        InventoryClickEvent invEvent = event.getInventoryClickEvent();
        
        Player player = (Player) invEvent.getWhoClicked();
        ItemStack clickedItem = invEvent.getCurrentItem();

        if (clickedItem == null || clickedItem.getType() != Material.PLAYER_HEAD) {return;}

        String trackedName = clickedItem.getItemMeta().getDisplayName();
        Player trackedPlayer = Bukkit.getPlayerExact(trackedName);

        if (trackedPlayer == null || !trackedPlayer.isOnline()) {
            Chat.message(player, "§cThat player is not online.");
            return;
        }

        ItemStack compassItem = player.getInventory().getItemInMainHand();
        if (!TrackingCompass.isTrackingCompass(compassItem)) {return;}

        String uuid = TrackingCompass.getCompassUUID(compassItem);
        TrackingCompass compass = activeCompasses.get(uuid);

        if (compass == null) {return;}

        compass.track(trackedPlayer);
        Chat.message(player, "§aNow tracking §f" + trackedPlayer.getName() + "§a.");

        player.closeInventory();
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        
        handleCompassMove(player, event.getCurrentItem(), event, true);
        handleCompassMove(player, event.getCursor(), event, false);
    }

    private void handleCompassMove(Player player, ItemStack item, InventoryClickEvent event, boolean isClickedItem) {
        if (!TrackingCompass.isTrackingCompass(item)) return;
        
        String uuid = TrackingCompass.getCompassUUID(item);
        if (activeCompasses.get(uuid) == null) return;
        
        boolean isExternalInventory = event.getClickedInventory() != null
            && event.getClickedInventory().getType() != InventoryType.PLAYER
            && event.getClickedInventory().getType() != InventoryType.CRAFTING;
        
        boolean shouldRemove = isClickedItem
            ? (event.isShiftClick() && event.getClickedInventory() != null 
               && event.getClickedInventory().getType() == InventoryType.PLAYER
               && event.getView().getTopInventory().getType() != InventoryType.CRAFTING)
            : isExternalInventory;
        
        if (shouldRemove) {
            event.setCancelled(true);
            if (isClickedItem) {
                item.setAmount(0);
            } else {
                player.setItemOnCursor(null);
            }
            removeCompass(uuid);
        }
    }
}