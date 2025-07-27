package io.stcat.jake48.sallms_Speedrun.minigames;

import io.stcat.jake48.sallms_Speedrun.GameManager;
import io.stcat.jake48.sallms_Speedrun.GameState;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.*;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockDropItemEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

public class FarmingGame implements MiniGame {

    private World gameWorld;
    private final int originalTickSpeed;
    private static final int FAST_TICK_SPEED = 15; // 기본값의 5배
    private static final int DEFAULT_TICK_SPEED = 3;

    public FarmingGame(JavaPlugin plugin) {
        // 생성 시점에 서버의 기본 randomTickSpeed 값을 미리 저장
        this.originalTickSpeed = Bukkit.getServer().getWorlds().get(0).getGameRuleValue(GameRule.RANDOM_TICK_SPEED);
    }

    @Override
    public boolean setup(Player player, Location startLocation) {
        player.setGameMode(GameMode.SURVIVAL);
        this.gameWorld = startLocation.getWorld();

        // 인벤토리 정리 및 감자 지급
        for (int i = 0; i < 9; i++) {
            player.getInventory().clear(i);
        }
        player.getInventory().addItem(new ItemStack(Material.POTATO, 64));

        // 작물 성장 속도를 5배로 설정
        if (gameWorld != null) {
            gameWorld.setGameRule(GameRule.RANDOM_TICK_SPEED, FAST_TICK_SPEED);
        }

        return true;
    }

    @Override
    public void cleanup() {
        // 게임 규칙을 원래대로 복구
        if (gameWorld != null) {
            gameWorld.setGameRule(GameRule.RANDOM_TICK_SPEED, originalTickSpeed);
        }

    }

    @Override
    public void onBlockDropItem(BlockDropItemEvent event, JavaPlugin plugin) {
        GameManager gameManager = GameManager.getInstance();
        if (gameManager.getGameState() != GameState.RUNNING) return;

        Player player = event.getPlayer();
        if (!player.getUniqueId().equals(event.getPlayer().getUniqueId())) return;

        // 부서진 블록이 감자 작물이 맞는지 확인
        if (event.getBlockState().getType() != Material.POTATO) return;

        // 드랍된 아이템 목록을 확인
        for (Item item : event.getItems()) {
            if (item.getItemStack().getType() == Material.POISONOUS_POTATO) {
                // 독감자 감지
                player.sendMessage(Component.text("독감자 발견!", NamedTextColor.GOLD));
                gameManager.advanceToNextStage(player);
                break; // 반복 중단
            }
        }
    }

}
