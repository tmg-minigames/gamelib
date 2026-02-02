package com.pythoncraft.gamelib;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.function.BiConsumer;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.advancement.Advancement;
import org.bukkit.advancement.AdvancementProgress;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import com.pythoncraft.gamelib.compass.CompassManager;
import com.pythoncraft.gamelib.compass.TrackingCompass;

public class PlayerActions {
    public static BiConsumer<Player, HashSet<Player>> setupPlayerPrepare(GameMode gameMode, boolean disableJumping, ItemStack food, boolean giveCompass, Player compassTarget) {
        return (Player player, HashSet<Player> players) -> {
            resetHealth(player);
            resetHunger(player);
            clearEffects(player);
            clearXp(player);
            clearEnderChest(player);
            setGamemode(player, gameMode);
            setInertTrue(player, disableJumping);

            addEffect(player, PotionEffectType.INVISIBILITY, -1, 0);
            addEffect(player, PotionEffectType.NIGHT_VISION, -1, 0);
    
            Inventory i = player.getInventory();
            i.clear();

            if (food != null) {i.setItem(40, food);} // food in offhand
            if (giveCompass) {
                TrackingCompass compass = CompassManager.getInstance().createTrackingCompass(player);
                if (compassTarget != null) {
                    compass.track(players.stream().filter(p -> {return p != player;}).findFirst().orElse(null));
                } else {
                    compass.track(compassTarget);
                }

                i.setItem(0, compass.createItem());
            }
        };
    }

    public static BiConsumer<Player, HashSet<Player>> setupPlayerReset(List<PotionEffect> effects) {
        return (Player player, HashSet<Player> players) -> {
            setGamemode(player, GameMode.SURVIVAL);
            setInertFalse(player);
            clearEffects(player);
            resetHealth(player);
            resetHunger(player);
            clearXp(player);
            clearEnderChest(player);

            if (effects != null) {
                for (PotionEffect effect : effects) {
                    player.addPotionEffect(effect);
                }
            }
        };
    }


    public static void resetHealth(Player player) {
        player.setHealth(getMaxHealth(player));
    }

    public static double getMaxHealth(Player player) {
        return player.getAttribute(Attribute.MAX_HEALTH).getValue();
    }

    public static void resetHunger(Player player) {
        player.setFoodLevel(20);
        player.setSaturation(20.0f);
        player.setExhaustion(0.0f);
    }

    public static void clearEffects(Player player) {
        player.clearActivePotionEffects();
    }

    public static void clearInventory(Player player) {
        player.getInventory().clear();
    }

    public static void clearEnderChest(Player player) {
        player.getEnderChest().clear();
    }

    public static void clearXp(Player player) {
        player.setExp(0);
        player.setLevel(0);
    }

    public static void setGamemode(Player player, GameMode gameMode) {
        if (gameMode == null) {
            gameMode = GameMode.SURVIVAL;
        } else {
            player.setGameMode(gameMode);
        }
    }

    public static void addEffect(Player player, PotionEffectType effectType, int durationSeconds, int amplifier) {
        player.addPotionEffect(effectType.createEffect(durationSeconds * 20, amplifier));
    }

    public static void removeEffect(Player player, PotionEffectType effectType) {
        player.removePotionEffect(effectType);
    }

    public static void setInertTrue(Player player, boolean disableJumping) {
        player.setInvulnerable(true);
        player.setCollidable(false);
        player.setCanPickupItems(false);
        player.setSilent(true);
        addEffect(player, PotionEffectType.MINING_FATIGUE, -1, 255);
        addEffect(player, PotionEffectType.WEAKNESS, -1, 255);
        addEffect(player, PotionEffectType.SLOWNESS, -1, 255);
    }

    public static void setInertFalse(Player player) {
        player.setInvulnerable(false);
        player.setCollidable(true);
        player.setCanPickupItems(true);
        player.setSilent(false);
        removeEffect(player, PotionEffectType.MINING_FATIGUE);
        removeEffect(player, PotionEffectType.WEAKNESS);
        removeEffect(player, PotionEffectType.SLOWNESS);
    }

    public static ItemStack getPlayerHead(Player p) {
        ItemStack head = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta meta = (SkullMeta) head.getItemMeta();
        meta.setOwningPlayer(p);
        meta.displayName(Chat.component(p.getName()));
        head.setItemMeta(meta);
        return head;
    }

    public static void revokeAllAdvancements(Player player) {
        Iterator<Advancement> advancements = Bukkit.getServer().advancementIterator();

        while (advancements.hasNext()) {
            AdvancementProgress progress = player.getAdvancementProgress(advancements.next());
            for (String s : progress.getAwardedCriteria())
                progress.revokeCriteria(s);
        }
    }
}
