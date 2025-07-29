package io.stcat.jake48.sallms_speedrun.minigames;

import com.sk89q.worldedit.function.operation.Operations;
import io.stcat.jake48.sallms_speedrun.GameManager;
import io.stcat.jake48.sallms_speedrun.Sallms_Speedrun;
import io.stcat.jake48.sallms_speedrun.SchematicManager;

import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.world.block.BlockType;
import com.sk89q.worldedit.world.block.BlockTypes;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.*;

public class BuildGame implements MiniGame {

    private final Sallms_Speedrun plugin;
//    private List<RelativeBlock> currentSample; // 현재 출제될 문제
    private Clipboard currentSampleClipboard; // 스케메팅 데이터를 담을 변수
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

        String requestSampleName = GameManager.getInstance().consumeRequestedStructureName();
        SchematicManager schematicManager = plugin.getSchematicManager();
        List<String> sampleNames = schematicManager.getSchematicNames();

        plugin.getLogger().info("[DEBUG] requestSampleName " + requestSampleName);
        if (sampleNames.isEmpty()) {
            player.sendMessage(Component.text("오류: 저장된 건축물 표본이 없습니다!", NamedTextColor.RED));
            return false;
        }

        String finalSampleName;
        if (requestSampleName != null && sampleNames.contains(requestSampleName.toLowerCase())) {
            finalSampleName = requestSampleName;
            player.sendMessage(Component.text("지정된 건축물 '" + finalSampleName + "'(으)로 시작합니다!", NamedTextColor.AQUA));
        } else {
            Collections.shuffle(sampleNames);
            finalSampleName = sampleNames.get(0);
            player.sendMessage((Component.text("이번에 지을 건축물 '", NamedTextColor.AQUA))
                    .append(Component.text(finalSampleName, NamedTextColor.GOLD))
                    .append(Component.text("' 입니다!")));
        }

        try {
            this.currentSampleClipboard = schematicManager.load(finalSampleName);
            if (currentSampleClipboard == null) return false;
        } catch (IOException e) {
            plugin.getLogger().severe("Failed to load schematic '" + finalSampleName + "'!");
            return false;
        }

        this.sampleAreaPos = plugin.getStageLocation(4, "sample-area-pos");
        this.playerAreaPos = plugin.getStageLocation(4, "player-area-pos");

        if (sampleAreaPos == null || playerAreaPos == null) {
            plugin.getLogger().severe("Stage 4 ERROR: sample-area-pos or player-area-pos is not set in config.yml!");
            player.sendMessage(Component.text("4단계 경기장이 제대로 설정되지 않았습니다!", NamedTextColor.RED));
            GameManager.getInstance().stopGame();
            return false;
        }

        if (!schematicManager.paste(finalSampleName, sampleAreaPos)) {
            player.sendMessage(Component.text("오류: 표본 건축물에 생성하는 데 실패했습니다.", NamedTextColor.RED));
            return false;
        }

        schematicManager.paste(finalSampleName, sampleAreaPos);

        // 표본 생성 및 필요 블록 계산/지급
        Map<Material, Integer> requiredBlocks = new HashMap<>();
        for (BlockVector3 pt : currentSampleClipboard.getRegion()) {
            BlockType blockType = currentSampleClipboard.getBlock(pt).getBlockType();
            if (!blockType.equals(BlockTypes.AIR)) { // 공기 블록은 아이템으로 지급 X
                Material material = BukkitAdapter.adapt(blockType);
                requiredBlocks.merge(material, 1, Integer::sum);
            }
        }
        requiredBlocks.forEach((material, count) -> player.getInventory().addItem(new ItemStack(material, count)));

        return true;
    }

    @Override
    public void cleanup() {
        // EditSession을 사용해 영역을 공기로 채워 정리
        if (currentSampleClipboard != null && sampleAreaPos != null && playerAreaPos != null) {
            try (EditSession editSession = WorldEdit.getInstance().newEditSession(BukkitAdapter.adapt(sampleAreaPos.getWorld()))) {
                Region sampleRegion = currentSampleClipboard.getRegion().clone();
                sampleRegion.shift(BukkitAdapter.asBlockVector(sampleAreaPos).subtract(currentSampleClipboard.getOrigin()));

                Region playerRegion = currentSampleClipboard.getRegion().clone();
                playerRegion.shift(BukkitAdapter.asBlockVector(playerAreaPos).subtract(currentSampleClipboard.getOrigin()));

                editSession.setBlocks(sampleRegion, BlockTypes.AIR.getDefaultState().toBaseBlock());
                editSession.setBlocks(playerRegion, BlockTypes.AIR.getDefaultState().toBaseBlock());
                Operations.complete(editSession.commit()); // 변경사항 적용
            } catch (WorldEditException e) {
                e.printStackTrace();
            }

      }
    }

    @Override
    public void onBlockPlace(@NotNull BlockPlaceEvent event, JavaPlugin plugin) {
        // 게임 상태, 플레이어 확인

        // 블럭을 놓을 때마다 정답과 일치하는지 확인
        checkCompletion(event.getPlayer());
    }

    private void checkCompletion(@NotNull Player player) {
        if (currentSampleClipboard == null || playerAreaPos == null) return;

        // [디버그] 검사 시작 로그
        plugin.getLogger().info("[DEBUG] --- Starting structure completion check --- ");

        // 스케메틱의 시작점(Origin)을 가져옴
        BlockVector3 origin = currentSampleClipboard.getOrigin();

        // 표본 건축물의 모든 위치를 순회
        for (BlockVector3 pt : currentSampleClipboard.getRegion()) {
            // 현재 블록의 절대 좌표(pt)에서 시작점(origin)을 빼서 상대 좌표를 계산
            BlockVector3 relativePos = pt.subtract(origin);

            // 플레이어 건축 영역 시작점에 상대 좌표를 더해 확인할 정확한 위치 계산
            Block playerBlock = playerAreaPos.clone()
                    .add(relativePos.x(), relativePos.y(), relativePos.z())
                    .getBlock();

            // 표본의 해당 위치에 있는 블록의 상세 정보(BlockData)를 문자열로 가져옴
            // 예: "minecraft:oak_door[facing=north, half=lower]"
            String sampleBlockDataString = currentSampleClipboard.getBlock(pt).getAsString();

            // 플레이어의 건축 영역에서, 그에 해당하는 위치의 블록 정보를 가져옴
            String playerBlockDataString = playerBlock.getBlockData().getAsString();

            // [디버그] 현재 비교 중인 두 블록의 상세 정보를 모두 출력
            plugin.getLogger().info(String.format("-> Checking at (%d, %d, %d):", pt.x(), pt.y(), pt.z()));
            plugin.getLogger().info("    Sample: " + sampleBlockDataString);
            plugin.getLogger().info("    Player: " + playerBlockDataString);

            // 두 블록의 상세 정보 문자열을 직접 비교
            if (!sampleBlockDataString.equals(playerBlockDataString)) {
                return; // 하나라도 다르면 즉시 검사 중단
            }
        }

        // [디버그] 반복문이 중단 없이 끝났음을 알림 (클리어)
        plugin.getLogger().info("[DEBUG] No mismatches found!");

        // 모든 블록이 일치하면 클리어
        player.sendMessage(Component.text("4단계 클리어!",  NamedTextColor.GOLD));
        GameManager.getInstance().advanceToNextStage(player);
    }

}
