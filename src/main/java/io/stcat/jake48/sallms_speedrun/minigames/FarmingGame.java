package io.stcat.jake48.sallms_speedrun.minigames;

import io.stcat.jake48.sallms_speedrun.GameManager;
import io.stcat.jake48.sallms_speedrun.GameState;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.data.Ageable;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockDropItemEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;


public class FarmingGame implements MiniGame{

    private static final Logger log = LoggerFactory.getLogger(FarmingGame.class);
    private final JavaPlugin plugin;
    // 실행 중인 모든 성장 타이머를 추적하기 위한 리스트
    private final List<BukkitTask> growthTasks = new ArrayList<>();
    // 아이템 정리를 위한 스테이지 중심 위치
    private Location stageCenter;

    public FarmingGame(JavaPlugin plugin){
        this.plugin = plugin;
    }

    @Override
    public boolean setup(Player player, Location startLocation) {
        this.stageCenter = startLocation.clone();
        player.setGameMode(GameMode.SURVIVAL);

        // 핫바 정리 및 왼손에 감자 64개 지급
        for (int i = 0; i < 9; i++) {
            player.getInventory().clear(i);
        }
        player.getInventory().setItemInOffHand(new ItemStack(Material.POTATO, 64));

        // 이전 게임의 타이머가 남아있을 경우 대비해 비워줌
        cleanupGrowthTasks();
        return true;
    }

    @Override
    public void cleanup() {
        plugin.getLogger().info("[DEBUG] FarmingGame cleanup");

        // 게임이 끝나면 실행 중인 모든 성장 타이머를 강제로 중지
        cleanupGrowthTasks();

        // 게임장소 청소
        if (stageCenter != null) {
           World world = stageCenter.getWorld();
           if (world != null) {
               // 맵에 남아있는 감자 제거
               int framRadius = 10;
               for (int x = -framRadius; x <= framRadius; x++) {
                   for (int z = -framRadius; z <= framRadius; z++) {
                       Block block = stageCenter.clone().add(x, 0, z).getBlock();
                       if (block.getType() == Material.POTATOES) {
                           block.setType(Material.AIR);
                       }
                   }
               }

               // 바닥에 떨어진 아이템 제거
               double itemCleanupRadius = 15.0;
               for (Entity entity : world.getEntities()) {
                   if (entity instanceof Item && entity.getLocation().distanceSquared(stageCenter) < itemCleanupRadius * itemCleanupRadius) {
                       entity.remove();
                   }
               }
           }
        }

    }

    @Override
    public void onPlayerInteract(PlayerInteractEvent event, JavaPlugin plugin) {
        GameManager gameManager = GameManager.getInstance();
        if (gameManager.getGameState() != GameState.RUNNING || gameManager.getCurrentStage() != 2) return;

        Player player = event.getPlayer();
        if (!player.getUniqueId().equals(gameManager.getSelectedPlayerUUID())) return;

        if (event.getAction() == Action.PHYSICAL) {
            Block clickedBlock = event.getClickedBlock();
            if (clickedBlock != null && clickedBlock.getType() == Material.FARMLAND) {
                event.setCancelled(true); // 경작지를 밟아도 흙으로 변하지 않도록 이벤트 취소
                return; 
            }
        }

        // 경작지를 감자를 들고 오른쪽 클릭을 했을 때
        if (event.getAction() == Action.RIGHT_CLICK_BLOCK &&
            (player.getInventory().getItemInMainHand().getType() == Material.POTATO || player.getInventory().getItemInOffHand().getType() == Material.POTATO) &&
            event.getClickedBlock() != null &&
            event.getClickedBlock().getType() == Material.FARMLAND) {

            Location cropLocation = event.getClickedBlock().getLocation().add(0, 1, 0);

            // 작물의 나이를 1씩 올리는 반복 작업을 예약
            BukkitTask growthTask = new BukkitRunnable() {
                @Override public void run() {
                    Block cropBlock = cropLocation.getBlock();
                    if (cropBlock.getType() != Material.POTATOES) {
                        this.cancel();
                        return;
                    }

                    if (cropBlock.getBlockData() instanceof Ageable) {
                        Ageable ageable = (Ageable) cropBlock.getBlockData();
                        int currentAge = ageable.getAge();

                        if (currentAge < ageable.getMaximumAge()) {
                            ageable.setAge(currentAge + 1);
                            cropBlock.setBlockData(ageable);
                        } else {
                            this.cancel();
                        }
                    } else {
                        this.cancel();
                    }
                }
            }.runTaskTimer(this.plugin, 5L, 5L); // 0.25초 뒤에 시작 해서, 0.25초마다 반복

            growthTasks.add(growthTask);
        }

    }

    @Override
    public void onPlayerPickupItem(EntityPickupItemEvent event, JavaPlugin plugin) {
        GameManager gameManager = GameManager.getInstance();
        if (gameManager.getGameState() != GameState.RUNNING || gameManager.getCurrentStage() != 2) return;

        // 리스너에서 이미 플레이어인지 확인했지만, 한번 더 형변환하여 사용
        Player player = (Player) event.getEntity();
        if (!player.getUniqueId().equals(gameManager.getSelectedPlayerUUID())) return;

        // 주운 아이템이 독감자인지 확인
        if (event.getItem().getItemStack().getType() == Material.POISONOUS_POTATO) {
            player.sendMessage(Component.text("2단계 클리어!", NamedTextColor.GOLD));

            gameManager.advanceToNextStage(player);
        }
    }

    private void cleanupGrowthTasks() {
        for (BukkitTask task : growthTasks) {
            if (!task.isCancelled()) {
                task.cancel();
            }
        }
        growthTasks.clear();
    }

}
