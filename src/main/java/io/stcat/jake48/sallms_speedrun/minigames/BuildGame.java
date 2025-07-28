package io.stcat.jake48.sallms_speedrun.minigames;

import io.stcat.jake48.sallms_speedrun.GameManager;
import io.stcat.jake48.sallms_speedrun.Sallms_Speedrun;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class BuildGame implements MiniGame{

    private final Sallms_Speedrun plugin;
    private List<RelativeBlock> currentSample; // 현재 출제될 문제
    private Location playerAreaPos; // 플레이어가 건축할 위치
    private Location sampleAreaPos; // 표본이 생성된 위치

    public BuildGame(JavaPlugin plugin) {
        this.plugin = (Sallms_Speedrun) plugin;
    }

    @Override
    public boolean setup(@NotNull Player player, Location startLocation) {
        // 인벤토리 정리 및 게임모드 설정
        player.setGameMode(GameMode.SURVIVAL);
        for (int i = 0; i < 9; i++) {
            player.getInventory().clear(i);
        }
        player.getInventory().setItemInOffHand(null);


        // 랜덤으로 건축물 표본 선택
        List<String> sampleNames = new ArrayList<>(plugin.getStructureManager().getStructureNames());
        if (sampleNames.isEmpty()) {
            player.sendMessage(Component.text("오류: 저장된 건축물 표본이 없습니다!", NamedTextColor.RED));
            return false;
        }
        Collections.shuffle(sampleNames);

        String selectedName = sampleNames.get(0);
        plugin.getLogger().info("[Stage 4] Selected sample for this round is: " + selectedName);
        player.sendMessage((Component.text("이번에 지을 건축물 '", NamedTextColor.AQUA))
                .append(Component.text(selectedName, NamedTextColor.GOLD))
                .append(Component.text("' 입니다!")));

        this.currentSample = plugin.getStructureManager().getStructures(sampleNames.get(0));

        this.sampleAreaPos = plugin.getStageLocation(4, "sample-area-pos");
        this.playerAreaPos = plugin.getStageLocation(4, "player-area-pos");

        if (sampleAreaPos == null || playerAreaPos == null) {
            plugin.getLogger().severe("Stage 4 ERROR: sample-area-pos or player-area-pos is not set in config.yml!");
            player.sendMessage(Component.text("4단계 경기장이 제대로 설정되지 않았습니다!", NamedTextColor.RED));
            GameManager.getInstance().stopGame();
            return false;
        }

        // 표본 생성 및 필요 블록 계산/지급
        Map<Material, Integer> requiredBlocks = new HashMap<>();
        for (RelativeBlock rb : currentSample) {
            sampleAreaPos.clone().add(rb.x(), rb.y(), rb.z()).getBlock().setType(rb.type());
            requiredBlocks.merge(rb.type(), 1, Integer::sum);
        }
        requiredBlocks.forEach((material, count) -> player.getInventory().addItem(new ItemStack(material, count)));

        return true;
    }

    @Override
    public void cleanup() {
        // 표본과 플레이어 건축물을 모두 공기로 변경하여 정리
        if (currentSample != null) {
            for (RelativeBlock rb : currentSample) {
                if (sampleAreaPos != null) sampleAreaPos.clone().add(rb.x(), rb.y(), rb.z()). getBlock().setType(Material.AIR);
                if (playerAreaPos != null) playerAreaPos.clone().add(rb.x(), rb.y(), rb.z()).getBlock().setType(Material.AIR);
            }
        }
    }

    @Override
    public void onBlockPlace(@NotNull BlockPlaceEvent event, JavaPlugin plugin) {
        // 게임 상태, 플레이어 확인

        // 블럭을 놓을 때마다 정답과 일치하는지 확인
        checkCompletion(event.getPlayer());
    }

    private void checkCompletion(Player player) {
        // 현재 검사하는 표본에 블록이 몇 개 있는지 확인
        plugin.getLogger().info("[DEBUG] Checking completion... Sample size is: " + currentSample.size());

        for (RelativeBlock rb : currentSample) {
            Block playerBlock = playerAreaPos.clone().add(rb.x(), rb.y(), rb.z()).getBlock();

            // 무엇과 무엇을 비교하는지 로그로 출력
            plugin.getLogger().info("-> Comparing Sample(" + rb.type() + ") with Player's(" + playerBlock.getType() + ")");

            if (playerBlock.getType() != rb.type()) {
                // 다른 블록을 발견하여 검사를 중단함을 알림
                plugin.getLogger().warning("[DEBUG] Mismatch found. Aborting check.");
                return; // 하나라도 다르면 즉시 중단
            }
        }
        // 반복문이 중단 없이 끝났음을 알림 (클리어)
        plugin.getLogger().info("[DEBUG] No mismatches found!");

        // 모든 블록이 일치하면 클리어
        player.sendMessage(Component.text("4단계 클리어!",  NamedTextColor.GOLD));
        GameManager.getInstance().advanceToNextStage(player);
    }

}
