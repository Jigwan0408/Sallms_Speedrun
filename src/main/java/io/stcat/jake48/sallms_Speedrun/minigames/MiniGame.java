package io.stcat.jake48.sallms_Speedrun.minigames;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.plugin.java.JavaPlugin;

public interface MiniGame {

    // 게임 무대를 준비하는 메서드 (블록 생성, 아이템 지급 등)
    boolean setup(Player player, Location startLocation);

    // 게임 무대를 정리하는 메서드 (생성된 블록 제거, 아이템 제거 등)
    void cleanup();

    default void onBlockBreak(BlockBreakEvent event, JavaPlugin plugin) {
        // 1단계 미니게임 메서드 (블록 캐기)
    }

    default void onBlockDropItem(BlockDropItemEvent event, JavaPlugin plugin) {
        // 2단계 미니게임 메서드 (농사 게임)
    }

}
