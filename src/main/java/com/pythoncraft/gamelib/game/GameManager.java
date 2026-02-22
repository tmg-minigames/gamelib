package com.pythoncraft.gamelib.game;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerMoveEvent;

import java.io.File;
import java.util.HashSet;
import java.util.function.BiConsumer;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.WorldBorder;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import com.pythoncraft.gamelib.GameLib;
import com.pythoncraft.gamelib.Logger;
import com.pythoncraft.gamelib.Timer;
import com.pythoncraft.gamelib.game.event.GameEndEvent;
import com.pythoncraft.gamelib.game.event.GamePrepareEvent;
import com.pythoncraft.gamelib.game.event.GameStartEvent;
import com.pythoncraft.gamelib.game.event.GracePeriodEndEvent;
import com.pythoncraft.gamelib.game.event.TickEvent;

public class GameManager implements Listener {
    private static GameManager instance;
    public int gap = 6000;
    public int currentGame = 0;
    public int nextGame = 1;
    public HashSet<String> avoidedBiomes;
    public boolean gracePeriodPVP = false;

    public int borderSize = 400; // 0 means no border
    public int borderDamageAmount = 100;
    public int borderWarnDistance = 0;

    public int prepareTimeSec = 10;
    public int gracePeriodSec = 0; // 0 means no grace period
    public int gameTimeSec = 0; // 0 means infinite time

    public boolean isGame = false;
    public boolean isPreparing = false;
    public boolean isGracePeriod = false;

    public BiConsumer<Player, HashSet<Player>> onPrepareStart;
    public BiConsumer<Player, HashSet<Player>> onPrepareEnd;

    public int x = 0;
    public int y = 0;
    public int z = 0;

    public HashSet<Player> playersInGame = new HashSet<>();
    public Timer timer;
    public World world;
    public BossBar bossbar;

    private File configFile;
    private FileConfiguration config;
    private String gameNumberLabel;

    public BiConsumer<Integer, TickEvent> gameTickFunction;;
    public BarColor bossbarColor = BarColor.GREEN;
    public BarStyle bossbarStyle = BarStyle.SOLID;

    public static GameManager getInstance() {
        return instance;
    }
    
    public GameManager() {
        instance = this;
        GameLib.getInstance().getServer().getPluginManager().registerEvents(this, GameLib.getInstance());
    }


    public void setWorld(World world) {
        this.world = world;
    }

    public void setGap(int gap) {
        this.gap = gap;
    }

    public void setGameTime(int gameTimeSec, int prepareTimeSec, int gracePeriodSec) {
        this.prepareTimeSec = prepareTimeSec;
        this.gracePeriodSec = gracePeriodSec;
        this.gameTimeSec = gameTimeSec;
    }

    public void setGameTime(int gameTimeSec, int prepareTimeSec) {setGameTime(gameTimeSec, prepareTimeSec, 0);}
    public void setGameTime(int gameTimeSec) {setGameTime(gameTimeSec, 0, 0);}

    public void setBorder(int borderSize, int borderDamageAmount, int borderWarnDistance) {
        this.borderSize = borderSize;
        this.borderDamageAmount = borderDamageAmount;
        this.borderWarnDistance = borderWarnDistance;
    }

    public void setBorder(int borderSize, int borderDamageAmount) {setBorder(borderSize, borderDamageAmount, 0);}
    public void setBorder(int borderSize) {setBorder(borderSize, 100, 0);}

    public void setAvoidedBiomes(HashSet<String> avoidedBiomes) {
        this.avoidedBiomes = avoidedBiomes;
    }

    public void setPlayerSetupMethod(BiConsumer<Player, HashSet<Player>> onPrepareStart, BiConsumer<Player, HashSet<Player>> onGameStart) {
        this.onPrepareStart = onPrepareStart;
        this.onPrepareEnd = onGameStart;
    }

    public void setBossbar(BarColor color, BarStyle style) {
        removeBossbar();
        this.bossbarColor = color;
        this.bossbarStyle = style;
        this.bossbar = Bukkit.createBossBar("", this.bossbarColor, this.bossbarStyle);
    }

    public void setGameTickFunction(BiConsumer<Integer, TickEvent> gameTickFunction) {
        this.gameTickFunction = gameTickFunction;
    }

    public void setConfig(File configFile, FileConfiguration config, String gameNumberID) {
        this.configFile = configFile;
        this.config = config;
        this.gameNumberLabel = gameNumberID;

        this.currentGame = config.getInt(gameNumberLabel, 0);
        this.nextGame = findNextGame(world, this.currentGame + 1);
    }

    public void removeBossbar() {
        if (this.bossbar != null) {
            this.bossbar.removeAll();
            this.bossbar = null;
        }
    }

    public void startGame(World world, boolean isNewLocation) {
        if (isNewLocation) {
            Logger.info("Starting a new game: {0}", this.currentGame);
            this.playersInGame = new HashSet<>(Bukkit.getOnlinePlayers());
            this.world = world;
            this.currentGame = this.nextGame;
            this.nextGame = this.findNextGame(world, this.currentGame + 1);

            this.removeBossbar();
            this.bossbar = Bukkit.createBossBar("", this.bossbarColor, this.bossbarStyle);

            this.config.set(this.gameNumberLabel, this.currentGame);

            try {
                this.config.save(this.configFile);
            } catch (Exception e) {
                Logger.error("Could not save game config file: {0}", e.getMessage());
            }

            this.x = this.nextGame * this.gap;
            this.z = 0;
            this.y = world.getHighestBlockYAt(this.x, this.z);

            if (this.isWater(world, this.x, this.y, this.z)) {
                world.getBlockAt(this.x, this.y + 1, this.z).setType(Material.LILY_PAD);
            }

            if (this.borderSize > 0) {
                WorldBorder border = world.getWorldBorder();
                border.setCenter(this.x, this.z);
                border.setSize(this.borderSize);
                border.setDamageAmount(this.borderDamageAmount);
                border.setWarningDistance(this.borderWarnDistance);
            }
    
            for (Player player : playersInGame) {
                if (this.onPrepareStart != null) {
                    this.onPrepareStart.accept(player, playersInGame);
                }
    
                this.bossbar.addPlayer(player);
    
                player.teleport(new Location(world, this.x + 0.5, this.y + 1.09375, this.z + 0.5));
            }
        }

        Bukkit.getPluginManager().callEvent(new GamePrepareEvent());

        this.isPreparing = (this.prepareTimeSec > 0);
        this.isGame = !this.isPreparing;
        this.isGracePeriod = this.isGame && (this.gracePeriodSec > 0);

        if (isNewLocation && this.prepareTimeSec > 0) {
            this.timer = new Timer(prepareTimeSec * 20, 20, (secondsLeft) -> {
                // Prepare time tick
                Bukkit.getPluginManager().callEvent(new TickEvent(true, secondsLeft, this.prepareTimeSec));
            }, () -> {
                Logger.info("Prepare time is over, starting the game.");
                // Prepare time is over, start the game
                this.isPreparing = false;
                this.isGame = true;

                if (this.onPrepareEnd != null) {
                    for (Player player : playersInGame) {
                        this.onPrepareEnd.accept(player, playersInGame);
                    }
                }

                if (this.gracePeriodSec > 0) {
                    Logger.info("Starting grace period timer for {0} seconds.", this.gracePeriodSec);
                    // Start grace period
                    this.isGracePeriod = true;

                    this.timer = new Timer(this.gracePeriodSec * 20, 20, (secondsLeft) -> {
                        Bukkit.getPluginManager().callEvent(new TickEvent(true, secondsLeft, this.gracePeriodSec, true));
                    }, () -> {
                        Logger.info("Grace period is over.");
                        this.isGracePeriod = false;
                        Bukkit.getPluginManager().callEvent(new GracePeriodEndEvent());

                        this.gameStarted();
                    });

                    this.timer.start();
                } else {
                    Logger.info("No grace period, starting the game immediately.");
                    // No grace period, start the game immediately
                    this.gameStarted();
                }
            });

            Logger.info("Starting prepare timer for {0} seconds.", this.prepareTimeSec);
            this.timer.start();
        } else {
            Logger.info("Skipping prepare time (0 seconds), starting the game.");
            // No prepare time, start the game immediately
            this.isPreparing = false;
            this.isGame = true;

            if (isNewLocation && this.onPrepareEnd != null) {
                for (Player player : playersInGame) {
                    this.onPrepareEnd.accept(player, playersInGame);
                }
            }

            if (this.gracePeriodSec > 0) {
                Logger.info("Starting grace period timer for {0} seconds.", this.gracePeriodSec);
                // Start grace period
                this.isGracePeriod = true;

                this.timer = new Timer(this.gracePeriodSec * 20, 20, (secondsLeft) -> {
                    Bukkit.getPluginManager().callEvent(new TickEvent(true, secondsLeft, this.gracePeriodSec, true));
                }, () -> {
                    Logger.info("Grace period is over.");
                    this.isGracePeriod = false;
                    Bukkit.getPluginManager().callEvent(new GracePeriodEndEvent());

                    this.gameStarted();
                });

                this.timer.start();
            } else {
                Logger.info("No grace period, starting the game immediately.");
                // No grace period, start the game immediately
                this.gameStarted();
            }
        }
    }

    public void startGame(World world) {
        startGame(world, true);
    }

    public void gameStarted() {
        Logger.info("Game started.");
        this.isGracePeriod = false;
        Bukkit.getPluginManager().callEvent(new GameStartEvent());

        if (this.timer != null) {
            this.timer.cancel();
            this.timer = null;
        }

        if (this.gameTimeSec > 0) {
            Logger.info("Starting finite game timer.");
            // Start finite game timer
            this.timer = new Timer(this.gameTimeSec * 20, 20, (secondsLeft) -> {
                Bukkit.getPluginManager().callEvent(new TickEvent(true, secondsLeft, this.gameTimeSec, false));
            }, () -> {
                // Game over
                this.isGame = false;
                Bukkit.getPluginManager().callEvent(new GameEndEvent(null, GameEndEvent.TIMEOUT));
            });
        } else {
            Logger.info("Starting infinite game timer.");
            // Start infinite game timer
            this.timer = Timer.loop(20, (timeElapsed) -> {
                Bukkit.getPluginManager().callEvent(new TickEvent(timeElapsed / 20, 0, false));
            });
        }

        this.timer.start();
    }

    public void stopGame(boolean isNewLocation) {
        if (this.timer != null) {
            this.timer.cancel();
            this.timer = null;
        }

        this.isGame = false;
        this.isPreparing = false;
        this.isGracePeriod = false;

        if (isNewLocation) {
            if (this.bossbar != null) {
                this.bossbar.removeAll();
            }
            
            for (Player player : playersInGame) {
                if (this.onPrepareEnd != null) {
                    this.onPrepareEnd.accept(player, playersInGame);
                }
            }
            
            if (this.world != null) {this.world.getWorldBorder().reset();}
        }

        Bukkit.getPluginManager().callEvent(new GameEndEvent(null, GameEndEvent.STOPPED));
    }

    public void stopGame() {
        stopGame(true);
    }

    public long playersOnlineCount() {
        return this.playersInGame.stream().filter(p -> p.isOnline()).count();
    }

    public int findNextGame(World world, int start) {
        int g = start;

        while (!this.isSafe(world, g * gap, world.getHighestBlockYAt(g * gap, 0), 0)) {
            g++;
        }

        return g;
    }

    public int findNextGame(World world) {
        return this.findNextGame(world, this.nextGame);
    }

    public boolean isSafe(World world, int x, int y, int z) {
        if (avoidedBiomes.isEmpty()) {return true;}

        String b = world.getBiome(x, y, z).toString().toUpperCase();

        Logger.info("Checking safe location ({0}).", b);

        for (String biome : avoidedBiomes) {
            if (b.contains(biome)) {
                return false;
            }
        }

        Logger.info("Location is safe.");

        return true;
    }

    public boolean isWater(World world, int x, int y, int z) {
        return world.getBlockAt(x, y, z).getType().equals(Material.WATER) && world.getBlockAt(x, y - 1, z).getType().equals(Material.WATER);
    }

    public Location getGameLocation() {
        return new Location(world, this.x + 0.5, this.y + 1.09375, this.z + 0.5);
    }

    public Location getSpectatorLocation(int dy) {
        return getGameLocation().add(0, dy, 0);
    }

    public Location getSpectatorLocation() {
        return getSpectatorLocation(world.getHighestBlockYAt(getGameLocation()) + 20);
    }

    public Location getNextGameLocation() {
        int nextX = this.findNextGame(this.world) * this.gap;
        int nextY = this.world.getHighestBlockYAt(nextX, 0);
        return new Location(world, nextX + 0.5, nextY + 1.09375, this.z + 0.5);
    }

    public void checkForGameEnd(Player player) {
        if (this.playersInGame.size() == 1 && this.isGame && !this.isGracePeriod) {
            Bukkit.getPluginManager().callEvent(new GameEndEvent(player, GameEndEvent.GAME_END));
            this.stopGame();
        } else if (this.playersInGame.size() == 0 && this.isGame && !this.isGracePeriod) {
            Bukkit.getPluginManager().callEvent(new GameEndEvent(null, GameEndEvent.GAME_END));
            this.stopGame();
        }
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        if (!this.isPreparing) {return;}
        Player player = event.getPlayer();
        if (!this.playersInGame.contains(player)) {return;}

        Location from = event.getFrom();
        Location to = event.getTo();
        if (from.getX() != to.getX() || from.getZ() != to.getZ()) {
            event.setTo(new Location(to.getWorld(), from.getX(), to.getY(), from.getZ(), to.getYaw(), to.getPitch()));
        }
    }

    @EventHandler
    public void onGameTick(TickEvent event) {
        if (this.isGracePeriod) {
            Logger.info("Grace period tick: {0} seconds elapsed.", event.getElapsedSeconds());
        } else if (this.isGame) {
            Logger.info("Game tick: {0} seconds elapsed.", event.getElapsedSeconds());
        } else {
            Logger.info("Prepare tick: {0} seconds elapsed.", event.getElapsedSeconds());
        }
    }

    @EventHandler
    public void onPlayerAttack(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player)) {return;}
        Player damager = (Player) event.getDamager();
        if (!this.playersInGame.contains(damager)) {return;}

        if (this.isGracePeriod && !this.gracePeriodPVP && event.getEntity() instanceof Player) {event.setCancelled(true);}
    }
}
