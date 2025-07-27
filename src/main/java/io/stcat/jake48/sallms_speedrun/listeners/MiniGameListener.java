package io.stcat.jake48.sallms_speedrun.listeners;

import io.stcat.jake48.sallms_speedrun.GameManager;
import io.stcat.jake48.sallms_speedrun.minigames.MiniGame;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
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
    public void onBlockBreak(BlockBreakEvent event) {
        // 현재 활성화된 미니게임 객체를 가져옴
        MiniGame currentGame = GameManager.getInstance().getActiveMiniGame();
        if (currentGame != null) {
            // 이벤트 처리를 현재 미니게임에게 위임
            currentGame.onBlockBreak(event, plugin);
        }
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
        // 아이템을 주운 엔티티가 플레이어인지 먼저 확인
        if (event.getEntity() instanceof Player) {
            MiniGame currentGame = GameManager.getInstance().getActiveMiniGame();
            if (currentGame != null) {
                currentGame.onPlayerPickupItem(event, plugin);
            }
        }
    }

}
