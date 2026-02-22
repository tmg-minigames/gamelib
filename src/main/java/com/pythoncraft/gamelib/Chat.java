package com.pythoncraft.gamelib;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;

import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;

public class Chat {
    public static String BLACK       = "§0";
    public static String DARK_BLUE   = "§1";
    public static String DARK_GREEN  = "§2";
    public static String DARK_AQUA   = "§3";
    public static String DARK_RED    = "§4";
    public static String DARK_PURPLE = "§5";
    public static String GOLD        = "§6";
    public static String DARK_GRAY   = "§7";
    public static String GRAY        = "§8";
    public static String BLUE        = "§9";
    public static String GREEN       = "§a";
    public static String AQUA        = "§b";
    public static String RED         = "§c";
    public static String PURPLE      = "§d";
    public static String YELLOW      = "§e";
    public static String WHITE       = "§f";

    public static String RESET       = "§r";
    public static String BOLD        = "§l";
    public static String ITALIC      = "§o";
    public static String UNDERLINE   = "§n";
    public static String STRIKETHROUGH = "§m";

    public static String c(String message) {
        return message.replace("&", "§");
    }

    public static void actionBar(Player player, String message) {
        player.spigot().sendMessage(ChatMessageType.ACTION_BAR, component(message));
    }

    public static void actionBar(Iterable<Player> players, String message) {
        for (Player player : players) {actionBar(player, message);}
    }

    public static void message(Player player, String message) {
        player.sendMessage(c(message));
    }

    public static void message(Iterable<Player> players, String message) {
        for (Player player : players) {message(player, message);}
    }

    public static void broadcast(String message) {
        Bukkit.getServer().broadcast(message, "*");
    }



    public static String string(BaseComponent component) {
        return component.toPlainText();
    }

    public static BaseComponent component(String message) {
        return TextComponent.fromLegacy(c(message));
    }

    public static List<BaseComponent> components(Iterable<String> messages) {
        List<BaseComponent> components = new ArrayList<>();
        for (String msg : messages) {components.add(component(msg));}
        return components;
    }

    public static NamespacedKey namespacedKey(String key, boolean isMinecraftKey) {
        if (isMinecraftKey) {
            return NamespacedKey.minecraft(key);
        } else {
            return NamespacedKey.fromString(key, GameLib.getInstance());
        }
    }

    public static NamespacedKey namespacedKey(String key) {
        return namespacedKey(key, false);
    }
}
