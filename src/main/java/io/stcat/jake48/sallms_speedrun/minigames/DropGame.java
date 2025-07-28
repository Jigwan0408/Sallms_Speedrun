package io.stcat.jake48.sallms_speedrun.minigames;

import io.stcat.jake48.sallms_speedrun.GameManager;
import io.stcat.jake48.sallms_speedrun.GameState;
import io.stcat.jake48.sallms_speedrun.Sallms_Speedrun;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

public class DropGame implements MiniGame {

    private final Sallms_Speedrun plugin;
    private boolean waterDropSuccess = false; // 물낙법 성공 여부를 추적하는 변수
    private Location placedWaterLocation; // 플레이어가 설치한 물 블록 위치

    public DropGame(JavaPlugin plugin) {
        this.plugin = (Sallms_Speedrun) plugin;
    }

    @Override
    public boolean setup(@NotNull Player player, Location startLocation) {
        player.setGameMode(GameMode.SURVIVAL);
        for (int i = 0; i < 9; i++) {
            player.getInventory().clear(i);
        }
        player.getInventory().setItemInOffHand(new ItemStack(Material.WATER_BUCKET));

        this.waterDropSuccess = false; // 스테이지 시작 시 성공 여부 초기화
        this.placedWaterLocation = null;
        return true;
    }

    @Override
    public void cleanup() {
        // 플레이어가 설치한 물이 남아있으면 공기로 바꿔서 정리
        if (placedWaterLocation != null) {
            if (placedWaterLocation.getBlock().getType() == Material.WATER) {
                placedWaterLocation.getBlock().setType(Material.AIR);
            }
        }
    }

    // 플에이어가 낙하 대미지 입었을 때 (실패)
    @Override
    public void onEntityDamage(EntityDamageEvent event, JavaPlugin plugin) {
        GameManager gameManager = GameManager.getInstance();
        if (gameManager.getGameState() != GameState.RUNNING || gameManager.getCurrentStage() != 5) return;

        if (!(event.getEntity() instanceof Player player)) return;
        if (!player.getUniqueId().equals(gameManager.getSelectedPlayerUUID())) return;

        // 데미지 원인이 '추락'일 경우
        if (event.getCause() == EntityDamageEvent.DamageCause.FALL) {
            event.setCancelled(true); // 죽지 않도록 대미지 취소
            player.sendMessage(Component.text("실패! 다시 시도하세요.", NamedTextColor.RED));

            // 시작 지점으로 다시 텔레포트
            Location startPoint = this.plugin.getStageTeleportLocation(5);
            if (startPoint != null) player.teleport(startPoint);
            
            this.waterDropSuccess = false; // 성공 기록 초기화
        }
    }

    // 플레이어가 물 양동이를 비웠을 때 (일단 성공)
    @Override
    public void onPlayerBucketEmpty(PlayerBucketEmptyEvent event, JavaPlugin plugin) {
        GameManager gameManager = GameManager.getInstance();
        if (gameManager.getGameState() != GameState.RUNNING || gameManager.getCurrentStage() != 5) return;

        Player player = event.getPlayer();
        if (!player.getUniqueId().equals(gameManager.getSelectedPlayerUUID())) return;

        this.waterDropSuccess = true;
        this.placedWaterLocation = event.getBlock().getLocation(); // 물 위치 저장
    }

    // 플레이어가 발판을 밟았을 때 (최종 클리어)
    @Override
    public void onPlayerInteract(PlayerInteractEvent event, JavaPlugin plugin) {
        GameManager gameManager = GameManager.getInstance();
        if (gameManager.getGameState() != GameState.RUNNING || gameManager.getCurrentStage() != 5) return;

        Player player = event.getPlayer();
        if (!player.getUniqueId().equals(gameManager.getSelectedPlayerUUID())) return;

        Block clickedBlock = event.getClickedBlock();
        if (event.getAction() == Action.PHYSICAL && clickedBlock != null && clickedBlock.getType() == Material.LIGHT_WEIGHTED_PRESSURE_PLATE) {

            // 물낙법을 성공한 상태인지 확인
            if (waterDropSuccess) {
                player.sendMessage(Component.text("5단계 클리어!", NamedTextColor.GOLD));
                gameManager.advanceToNextStage(player);
            } else {
                player.sendMessage(Component.text("물낙법을 먼저 성공해야 합니다!", NamedTextColor.YELLOW));

                // 시작 지점으로 다시 텔레포트
                Location startPoint = this.plugin.getStageTeleportLocation(5);
                if (startPoint != null) player.teleport(startPoint);
            }
        }
    }
}
