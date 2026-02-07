package com.pythoncraft.gamelib.game.event;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * Event fired every second during an active game.
 */

public class TickEvent extends Event {
    private static final HandlerList HANDLERS = new HandlerList();

    private final int elapsedSeconds;
    private final int totalSeconds;
    private final boolean isGracePeriod;

    public TickEvent(int elapsedSeconds, int totalSeconds, boolean isGracePeriod) {
        this.elapsedSeconds = elapsedSeconds;
        this.totalSeconds = totalSeconds;
        this.isGracePeriod = isGracePeriod;
    }

    public TickEvent(boolean _x, int remainingSeconds, int totalSeconds, boolean isGracePeriod) {
        this(totalSeconds - remainingSeconds, totalSeconds, isGracePeriod);
    }

    public TickEvent(boolean _x, int remainingSeconds, int totalSeconds) {
        this(totalSeconds - remainingSeconds, totalSeconds, false); 
    }

    public TickEvent(int elapsedSeconds, int totalSeconds) {
        this(elapsedSeconds, totalSeconds, false); 
    }

    public int getElapsedSeconds() {return elapsedSeconds;}
    public int getTotalSeconds() {return totalSeconds;}
    public int getTimeRemaining() {return totalSeconds - elapsedSeconds;}
    public boolean isGracePeriod() {return isGracePeriod;}

    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }
}
