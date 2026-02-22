package com.pythoncraft.gamelib;

import java.io.File;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.GameRule;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;


public class GameLib extends JavaPlugin {
    
    static GameLib instance;
    public static GameLib getInstance() { return instance; }

    public static final Material DEFAULT_MATERIAL = Material.STICK;
    public static File configFile = null;
    public static FileConfiguration config = null;

    @Override
    public void onEnable() {
        instance = this;
        Logger.info("GameLib is starting up...");
    }

    @Override
    public void onDisable() {
        Logger.info("GameLib is shutting down...");
    }

    public static void setConfig(File file, FileConfiguration config) {
        GameLib.configFile = file;
        GameLib.config = config;
    }

    public static ItemStack getItemStack(Material material, int amount, String displayName) {
        ItemStack itemStack = new ItemStack(material, amount);
        ItemMeta meta = itemStack.getItemMeta();

        if (displayName != null && !displayName.isEmpty()) {
            meta.setDisplayName(Chat.c(displayName));
        }

        itemStack.setItemMeta(meta);
        return itemStack;
    }

    public static ItemStack getItemStack(Material material, int amount) {
        return getItemStack(material, amount, null);
    }

    public static ItemStack getItemStack(Material material) {
        return getItemStack(material, 1, null);
    }

    public static ItemStack getItemStack() {
        return getItemStack(DEFAULT_MATERIAL, 1, null);
    }

    public static ItemStack getItemStack(String displayName) {
        return getItemStack(DEFAULT_MATERIAL, 1, displayName);
    }

    public static void forceLoadChunk(World world, int x, int z) {
        world.getChunkAt(x, z).addPluginChunkTicket(GameLib.getInstance());
    }

    public static void forceLoadChunk(World world, int x, int z, int radius) {
        for (int dx = -radius; dx <= radius; dx++) {
            for (int dz = -radius; dz <= radius; dz++) {
                forceLoadChunk(world, x + dx, z + dz);
            }
        }
    }

    public static void forceLoadChunkStop(World world, int x, int z) {
        world.getChunkAt(x, z).removePluginChunkTicket(GameLib.getInstance());
    }

    public static void forceLoadChunkStop(World world, int x, int z, int radius) {
        for (int dx = -radius; dx <= radius; dx++) {
            for (int dz = -radius; dz <= radius; dz++) {
                forceLoadChunkStop(world, x + dx, z + dz);
            }
        }
    }

    public static void spectate(Player player, Location spectatorSpawn) {
        player.sendMessage(Chat.c("§c§lYou are now spectating the game!"));
        player.getInventory().clear();
        player.setGameMode(GameMode.ADVENTURE);
        player.setHealth(20);
        player.setFoodLevel(20);

        Bukkit.getScheduler().runTask(getInstance(), () -> {
            player.setGameMode(GameMode.SPECTATOR);
            player.teleport(spectatorSpawn);
        });
    }

    public static Team createTeam(String id, String displayName, ChatColor color) {
        Scoreboard scoreboard = Bukkit.getScoreboardManager().getMainScoreboard();
        Team team = scoreboard.getTeam(id);
        if (team != null) {team.unregister();}
        
        team = scoreboard.registerNewTeam(id);
        team.setDisplayName(Chat.c(displayName));
        team.setColor(color);;
        return team;
    }

    public static boolean setGamerule(World world, String[] rules, boolean value) {
        for (String rule : rules) {
            try {
                GameRule<Boolean> gameRule = (GameRule<Boolean>) GameRule.getByName(rule);
                world.setGameRule(gameRule, value);
                return true;
            } catch (IllegalArgumentException e) {}
        }

        Logger.warn("Failed to set gamerule. Tried: " + String.join(", ", rules));
        return false;
    }
}
