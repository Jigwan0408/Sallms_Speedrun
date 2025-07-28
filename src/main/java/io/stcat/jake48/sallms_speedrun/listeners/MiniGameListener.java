package io.stcat.jake48.sallms_speedrun.listeners;

import io.stcat.jake48.sallms_speedrun.GameManager;
import io.stcat.jake48.sallms_speedrun.minigames.MiniGame;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

public class MiniGameListener implements Listener {

    private final JavaPlugin plugin;
    public MiniGameListener(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        MiniGame currentGame = GameManager.getInstance().getActiveMiniGame();
        if (currentGame != null) {
            currentGame.onPlayerInteract(event, plugin);
        }
    }

    @EventHandler
    public void onPlayerPickupItem(@NotNull EntityPickupItemEvent event) {
        if (event.getEntity() instanceof Player) {
            MiniGame currentGame = GameManager.getInstance().getActiveMiniGame();
            if (currentGame != null) {
                currentGame.onPlayerPickupItem(event, plugin);
            }
        }
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        MiniGame currentGame = GameManager.getInstance().getActiveMiniGame();
        if (currentGame != null) {
            currentGame.onBlockBreak(event, plugin);
        }
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        MiniGame currentGame = GameManager.getInstance().getActiveMiniGame();
        if (currentGame != null) {
            currentGame.onBlockPlace(event, plugin);
        }
    }

}
