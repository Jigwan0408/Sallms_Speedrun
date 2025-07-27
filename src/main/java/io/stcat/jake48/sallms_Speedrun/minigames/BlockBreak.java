package io.stcat.jake48.sallms_Speedrun.minigames;

import io.stcat.jake48.sallms_Speedrun.GameManager;
import io.stcat.jake48.sallms_Speedrun.GameState;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

public class BlockBreak implements MiniGame {

    private final List<Location> blockLocations = new ArrayList<>();
    private final List<Location> bedrockLocations = new ArrayList<>();

    private final JavaPlugin plugin;

    public BlockBreak(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean setup(Player player, Location startLocation) {
        // 인벤토리 정리 및 도구 지급
        for (int i = 0; i < 9; i++) {
            player.getInventory().clear(i);
        }
        player.getInventory().addItem(new ItemStack(Material.DIAMOND_PICKAXE));
        player.getInventory().addItem(new ItemStack(Material.DIAMOND_AXE));
        player.getInventory().addItem(new ItemStack(Material.DIAMOND_SHOVEL));
        player.getInventory().addItem(new ItemStack(Material.DIAMOND_HOE));

        // 랜덤 블록 목록 준비
        List<Material> blocksToPlace = new ArrayList<>(List.of(
                Material.STONE, Material.OAK_LOG, Material.DIRT, Material.COBBLESTONE,
                Material.OAK_PLANKS, Material.HAY_BLOCK, Material.SOUL_SAND, Material.OAK_LEAVES
        ));
        Collections.shuffle(blocksToPlace);

        // 디버그 1) setup 메서드 시작 및 기준 좌표 로그
        plugin.getLogger().info("--- Stage 1 Setup Initiated ---");
        plugin.getLogger().info("Player Teleport Loaction (startLocation): " + startLocation.toVector());

        // 지정된 영역에서 기반암 위치 찾기
        blockLocations.clear(); // 게임 시작 전 목록 초기화
        bedrockLocations.clear();

        Location areaStartPoint = startLocation.clone().add(-3.5, 1, -3.5);

        // 디버그 2) 오프셋이 적용된 최종 검색 시작 위치 로그
        plugin.getLogger().info("Area Search Start Point (areaStartPoint): " + areaStartPoint.toVector());
        plugin.getLogger().info("Searching for Bedrock in a 7-block line along +X axis...");

        int searchWidth = 7; // startLocation 기준으로 가로 7칸 검색
        for (int i = 0; i < searchWidth; i++) {
            Location checkLoc = areaStartPoint.clone().add(i, 0, 0);
            // 디버그 3) 현재 확인 중인 좌표와 해당 위치의 블록 종류를 출력
            plugin.getLogger().info("Checking -> " + checkLoc.toVector() + " | Block Type: " + checkLoc.getBlock().getType());
            if (checkLoc.getBlock().getType() == Material.BEDROCK) {
                bedrockLocations.add(checkLoc);
            }
        }

        // 디버그 4) 최종적으로 몇 개의 기반암을 찾았는지 결과 출력
        plugin.getLogger().info("Found " + bedrockLocations.size() + " Bedrock blocks to replace");

        // 예외 처리
        if (bedrockLocations.isEmpty()) {
            plugin.getLogger().severe("Stage 1 ERROR: No bedrock placeholders found at the location!");
            player.sendMessage("§c[오류] 미니게임이 제대로 설정되지 않았습니다. 관리자에게 문의하세요.");

            // 게임 중단
            GameManager.getInstance().stopGame();
            return false; // setup 메서드 즉시 종료
        }

        // 찾은 기반암을 게임 블록으로 교체하고, 게임 추적 목록에 추가
        for (int i = 0; i < bedrockLocations.size(); i++) {
            // 놓을 블록이 부족할 경우를 대비
            if (i >= blocksToPlace.size()) break;

            Location targetLoc = bedrockLocations.get(i);
            targetLoc.getBlock().setType(blocksToPlace.get(i));
            blockLocations.add(targetLoc); // 리스너가 추적할 실제 게임 블록 위치 추가
        }

        return true;
    }

    @Override
    public void cleanup() {
        // 원본 리스트를 순회하며 모든 위치를 기반암으로 복원
        for (Location loc : bedrockLocations) {
            loc.getBlock().setType(Material.BEDROCK);
        }

        // 맵 복원이 끝난 후, 주변의 아이템 엔티티를 제거
        if (!bedrockLocations.isEmpty()) {
            // 중앙 블록 위치를 중심으로 10블록 반경
            Location center = bedrockLocations.get(3);
            World world = center.getWorld();
            double cleanupRadius = 10.0;

            // 월드의 모든 엔티티 확인
            if (world != null) {
                for (Entity entity : world.getEntities()) {
                    // 엔티티가 '아이템'이고, 설정한 반경 내에 있다면 제거
                    if (entity instanceof Item && entity.getLocation().distanceSquared(center) < cleanupRadius * cleanupRadius) {
                        entity.remove();
                    }
                }
            }
        }

        // 다음 게임을 위해 모든 리스트 초기화
        bedrockLocations.clear();
        blockLocations.clear();
    }

    @Override
    public void onBlockBreak(BlockBreakEvent event, JavaPlugin plugin) {
        GameManager gameManager = GameManager.getInstance();

        // 디버그1) 이벤트가 발생했는지 확인
//        plugin.getLogger().info("[DEBUG] BlockBreakEvent triggered!");

        // 1. 게임이 진행 중인지 확인
        if (gameManager.getGameState() != GameState.RUNNING) {
            plugin.getLogger().warning("[DEBUG] GameState is not RUNNING!");
            return;
        }

        // 2. 이벤트를 발생시킨 플레이어가 현재 선택된 플레이어인지 확인
        Player player = event.getPlayer();

        // 디버그 2) 선택된 플레이어와 이벤트 발생 플레이어의 UUID 비교
//        plugin.getLogger().info("[DEBUG] Event Player UUID: " + player.getUniqueId());
//        plugin.getLogger().info("[DEBUG] Selected Player UUID: " + gameManager.getSelectedPlayerUUID());

        if (!player.getUniqueId().equals(gameManager.getSelectedPlayerUUID())) {
            plugin.getLogger().warning("[DEBUG] Player UUID is not the same UUID!");
            return;
        }

        // 디버그 3) 블록 디버그
//        plugin.getLogger().info("[DEBUG] player check passed. Checking block...");
        Block brokenBlock = event.getBlock();

        // 3. 부순 블록이 이 미니게임의 추적 대상인지 확인
        if (!isBlockInGame(brokenBlock.getLocation())) {
            // 미니게임과 관련 없는 블록은 부술 수 없음
            plugin.getLogger().warning("[DEBUG] Block is not in game!");
            event.setCancelled(true);
            return;
        }

//        // 4. 알맞은 도구를 사용했는지 확인 (하드모드 고려)
//        if (!brokenBlock.isPreferredTool(player.getInventory().getItemInMainHand())) {
//            plugin.getLogger().warning("[DEBUG] Incorrect Tool!");
//            player.sendMessage(Component.text("잘못된 도구입니다! 알맞은 도구를 사용하세요.", NamedTextColor.RED));
//            event.setCancelled(true);
//            return;
//        }

        // 디버그 4) 모든 검사 통과
//        plugin.getLogger().info("[DEBUG] All checks passed!");

        // 5. 모든 규칙을 통과했으면, 추적 목록에서 이 블록 제거
        removeBlock(brokenBlock.getLocation());

        // 6. 모든 블록을 다 캤는지 (게임을 클리어) 확인
        if (isComplete()) {
            player.sendMessage(Component.text("1단계 클리어!", NamedTextColor.GREEN));

            // advanceToNextStage를 다음 틱에 실행되도록 예약
            new BukkitRunnable() {
                @Override
                public void run() {
                    gameManager.advanceToNextStage(player);
                }
            }.runTaskLater(plugin, 1);
        }
    }

    // 해당 위치가 현재 게임에서 추적 중인 블록인지 확인
    // @param loc 확인할 위치
    // @return 추적 중인 블록이면 true
    public boolean isBlockInGame(Location loc) {
        for (Location storedLoc : blockLocations) {
            if (storedLoc.getWorld().equals(loc.getWorld()) &&
                    storedLoc.getBlockX() == loc.getBlockX() &&
                    storedLoc.getBlockY() == loc.getBlockY() &&
                    storedLoc.getBlockZ() == loc.getBlockZ()) {

                return true; // X, Y, Z 좌표와 월드가 모두 일치하면 같은 블록으로 판단
            }
        }
        return false; // 일치하는 블록을 찾지 못함
    }

    // 게임 진행 추적 목록에서 해당 위치 제거
    // @param loc 제거할 위치
    public void removeBlock(Location loc) {
        Iterator<Location> iterator = blockLocations.iterator();
        while (iterator.hasNext()) {
            Location storedLoc = iterator.next();

            if (storedLoc.getWorld().equals(loc.getWorld()) &&
                    storedLoc.getBlockX() == loc.getBlockX() &&
                    storedLoc.getBlockY() == loc.getBlockY() &&
                    storedLoc.getBlockZ() == loc.getBlockZ()) {

                iterator.remove();
                break;
            }
        }
    }

    // 게임 진행 추적 목록이 비어있는지 (모든 블록을 캤는지) 확인
    // @return 모든 블록을 캤으면 true
    public boolean isComplete() {
        return blockLocations.isEmpty();
    }

}
