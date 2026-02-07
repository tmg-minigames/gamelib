package com.pythoncraft.gamelib.game.event;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/*
 * Event fired when the game ends.
 * If there is a winner, the winner Player object is provided; otherwise, it is null.
 */

public class GameEndEvent extends Event {
    private static final HandlerList HANDLERS = new HandlerList();
    private final Player winner;
    private final int endReason;

    public static int MANUAL = 0;
    public static int STOPPED = 1;
    public static int GAME_END = 2;
    public static int TIMEOUT = 3;

    public GameEndEvent(Player winner, int endReason) {
        this.winner = winner;
        this.endReason = endReason;
    }

    public GameEndEvent(Player winner) {
        this(winner, STOPPED);
    }

    public GameEndEvent(int endReason) {
        this(null, endReason);
    }

    public GameEndEvent() {
        this(null, STOPPED);
    }

    public Player getWinner() {
        return winner;
    }

    public int getEndReason() {
        return endReason;
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }
}
