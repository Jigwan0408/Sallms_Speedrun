package io.stcat.jake48.sallms_speedrun.minigames;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.plugin.java.JavaPlugin;

public interface MiniGame {

    // 게임 무대를 준비하는 메서드 (블록 생성, 아이템 지급 등)
    boolean setup(Player player, Location startLocation);

    // 게임 무대를 정리하는 메서드 (생성된 블록 제거, 아이템 제거 등)
    void cleanup();

    default void onPlayerInteract(PlayerInteractEvent event, JavaPlugin plugin) {
        // 플레이어 상호작용 메서드
    }

    default void onPlayerPickupItem(EntityPickupItemEvent event, JavaPlugin plugin) {
        // 플레이어 아이템 획득 이벤트
    }

    default void onBlockBreak(BlockBreakEvent event, JavaPlugin plugin) {
        // 블록 파괴 이벤트
    }

    default void onBlockPlace(BlockPlaceEvent event, JavaPlugin plugin) {
        // 블록 설치 이벤트
    }
    
    default void onEntityDamage(EntityDamageEvent event, JavaPlugin plugin) {
        // 엔티티 대미지 이벤트
    }

    default void onPlayerBucketEmpty(PlayerBucketEmptyEvent event, JavaPlugin plugin) {
        // 양동이 상태 이벤트
    }

    default void onProjectileHit(ProjectileHitEvent event, JavaPlugin plugin) {
        // 화살 피격 이벤트
    }
    

}
