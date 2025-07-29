package io.stcat.jake48.sallms_speedrun.minigames;

import io.stcat.jake48.sallms_speedrun.GameManager;
import io.stcat.jake48.sallms_speedrun.GameState;
import io.stcat.jake48.sallms_speedrun.Sallms_Speedrun;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Slime;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;

import org.jetbrains.annotations.NotNull;
import java.util.Random;

public class ShotGame implements MiniGame{

    private final Sallms_Speedrun plugin;
    
    private Slime targetSlime;
    private BukkitTask movementTask;
    private Location spawnCenter; // 슬라임 활동 영역의 중심

    private final Random random = new Random();

    public ShotGame (JavaPlugin plugin) {
        this.plugin = (Sallms_Speedrun) plugin;
    }

    @Override
    public boolean setup(@NotNull Player player, Location startLocation) {
        player.setGameMode(GameMode.SURVIVAL);
        for (int i = 0; i < 9; i++) player.getInventory().clear(i);
        player.getInventory().setItemInOffHand(null);

        // 무한 인챈트가 부여된 활가 화살 한 발 지급
        ItemStack infinityBow = new ItemStack(Material.BOW);
        infinityBow.addEnchantment(Enchantment.INFINITY, 1);
        player.getInventory().addItem(infinityBow);
        player.getInventory().addItem(new ItemStack(Material.ARROW, 1));

        // 슬라임 생성 및 설정
        this.spawnCenter = plugin.getStageLocation(6, "slime-spawn-pos");
        if (spawnCenter == null) {
            plugin.getLogger().severe("Stage 6 ERROR: slime-spawn-pos is not set!");
            player.sendMessage(Component.text("[오류] 6단계 미니게임이 제대로 설정되지 않았습니다. 관리자에게 문의해주세요.", NamedTextColor.RED));
            GameManager.getInstance().stopGame();
            return false;
        }

        this.targetSlime = spawnCenter.getWorld().spawn(spawnCenter, Slime.class);
        targetSlime.setSize(2); // 크기 설정
//        targetSlime.setAI(false); // 스스로 움직이지 못하게 설정
        targetSlime.setInvulnerable(true); // 무적 설정

        // 슬라임 이동 로직 시작
        startSlimeMovement();
        return true;
    }

    @Override
    public void cleanup() {
        // 실행 중인 이동 작업을 반드시 중지
        if (movementTask != null) {
            movementTask.cancel();
        }

        // 생성했던 슬라임 제거
        if (targetSlime != null && !targetSlime.isDead()) {
            targetSlime.remove();
        }

        // 발사된 화살 제거
        if (spawnCenter != null) {
            World world = spawnCenter.getWorld();
            if (world != null) {
                // 스폰 지점 주변 10블록 반경의 모든 엔티티 확인
                for (Entity entity : world.getNearbyEntities(spawnCenter, 10, 10, 10)) {
                    // 엔티티가 화살이면 제거
                    if (entity instanceof Arrow) {
                        entity.remove();
                    }
                }
            }
        }
    }

    private void startSlimeMovement() {
        this.movementTask = new BukkitRunnable() {
            private Location currentTarget = getRandomTargetLocation();

            @Override
            public void run() {
                if (targetSlime == null || targetSlime.isDead()) {
                    this.cancel();
                    return;
                }

                // 목표 지점에 거의 도착했다면 새로운 목표 지점 설정
                if (targetSlime.getLocation().distanceSquared(currentTarget) < 1.0) {
                    currentTarget = getRandomTargetLocation();
                }

                // 목표 지점을 향하는 향하는 방향 벡터를 계산
                Vector direction = currentTarget.toVector().subtract(targetSlime.getLocation().toVector()).normalize();

                // 속도를 적용하여 이번 틱에 이동할 아주 작은 거리를 계산
                double speed = 0.2; // 1틱당 0.2블록 이동

                // 슬라임의 현재 위치에 이동 거리를 더해 새로운 위치를 계산
                Location newLocation = targetSlime.getLocation().add(direction.multiply(speed));

                // setVelocity() 대신 teleport()를 사용하여 슬라임을 새 위치로 즉시 이동
                targetSlime.teleport(newLocation);
            }
        }.runTaskTimer(plugin, 0, 1);
    }

    // 스폰 지점 주변의 랜덤한 위치를 반환하는 헬퍼 메서드
    private @NotNull Location getRandomTargetLocation() {
        int radius = 5; // 슬라임이 배회할 반경
        double randomX = spawnCenter.getX() + (random.nextDouble() * 2 - 1) * radius;
        return new Location(spawnCenter.getWorld(), randomX, spawnCenter.getY(), spawnCenter.getZ());
    }

    @Override
    public void onProjectileHit(ProjectileHitEvent event, JavaPlugin plugin) {
        GameManager gameManager = GameManager.getInstance();
        if (gameManager.getGameState() != GameState.RUNNING || gameManager.getCurrentStage() != 6) return;

        // 화살을 쏜 사람이 현재 플레이어인지 확인
        if (!(event.getEntity() instanceof Arrow arrow) || !(arrow.getShooter() instanceof Player shooter)) return;
        if (!shooter.getUniqueId().equals(gameManager.getSelectedPlayerUUID())) return;

        if (event.getHitEntity() != null && event.getHitEntity().equals(targetSlime)) {
            shooter.sendMessage(Component.text("6단계 클리어!", NamedTextColor.GOLD));
            gameManager.advanceToNextStage(shooter);
        }
    }

}
