package com.pythoncraft.gamelib;

import java.util.HashSet;
import java.util.List;

import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Criteria;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;

public class Score {
    private static Scoreboard scoreboard = GameLib.getInstance().getServer().getScoreboardManager().getMainScoreboard();
    private Objective objective;

    public Score(String name, String displayName) {
        if (scoreboard.getObjective(name) != null) {
            scoreboard.getObjective(name).unregister();
        }
        this.objective = scoreboard.registerNewObjective(name, Criteria.DUMMY, Chat.component(displayName));
    }

    public Score(String name) {
        this(name, name);
    }

    public void setDisplayName(String displayName) {
        this.objective.displayName(Chat.component(displayName));
    }

    public void setDisplaySlot(DisplaySlot slot) {
        this.objective.setDisplaySlot(slot);
    }

    public void clearDisplaySlot() {
        this.objective.setDisplaySlot(null);
    }

    public void setScore(String entry, int score) {
        this.objective.getScore(entry).setScore(score);
    }

    public void setScore(Player player, int score) {
        this.setScore(player.getName(), score);
    }

    public void setScore(HashSet<Player> players, int score) {
        for (Player player : players) {this.setScore(player, score);}
    }

    public void addScore(String entry, int increment) {
        this.objective.getScore(entry).setScore(this.getScore(entry) + increment);
    }

    public void addScore(Player player, int increment) {
        this.addScore(player.getName(), increment);
    }

    public int getScore(String entry) {
        return this.objective.getScore(entry).getScore();
    }

    public int getScore(Player player) {
        return this.getScore(player.getName());
    }

    public void resetScores() {
        scoreboard.resetScores(this.objective.getName());
    }

    public void unregister() {
        this.objective.unregister();
    }
}
