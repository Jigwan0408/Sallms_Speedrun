package io.stcat.jake48.sallms_Speedrun.minigames;

import io.stcat.jake48.sallms_Speedrun.GameManager;
import io.stcat.jake48.sallms_Speedrun.minigames.MiniGame;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.plugin.java.JavaPlugin;

public class MiniGameListener implements Listener {

    private final JavaPlugin plugin;
    public MiniGameListener(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        // 현재 활성화된 미니게임 객체를 가져옴
        MiniGame currentGame = GameManager.getInstance().getActiveMiniGame();
        if (currentGame != null) {
            // 이벤트 처리를 현재 미니게임에게 위임
            currentGame.onBlockBreak(event, plugin);
        }
    }

    @EventHandler
    public void onBlockDropItem(BlockDropItemEvent event) {
        MiniGame currentGame = GameManager.getInstance().getActiveMiniGame();
        if (currentGame != null) {
            currentGame.onBlockDropItem(event, plugin);
        }
    }

}
