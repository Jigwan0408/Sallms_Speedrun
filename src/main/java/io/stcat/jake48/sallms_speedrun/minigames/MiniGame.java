package io.stcat.jake48.sallms_speedrun.minigames;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.plugin.java.JavaPlugin;

public interface MiniGame {

    // 게임 무대를 준비하는 메서드 (블록 생성, 아이템 지급 등)
    boolean setup(Player player, Location startLocation);

    // 게임 무대를 정리하는 메서드 (생성된 블록 제거, 아이템 제거 등)
    void cleanup();

    default void onPlayerInteract(PlayerInteractEvent event, JavaPlugin plugin) {
        // 플레이어 상호작용 메서드
    }

    default void onBlockBreak(BlockBreakEvent event, JavaPlugin plugin) {
        // 1단계 미니게임 메서드 (블록 캐기)
    }

    default void onPlayerPickupItem(EntityPickupItemEvent event, JavaPlugin plugin) {
        // 2단계 미니게임 메서드 (농사 게임)
    }

}
