package io.stcat.jake48.sallms_speedrun.minigames;

import io.stcat.jake48.sallms_speedrun.GameManager;
import io.stcat.jake48.sallms_speedrun.GameState;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

public class JumpGame implements MiniGame {

    private final JavaPlugin plugin;
    public  JumpGame(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean setup(@NotNull Player player, Location startLocation) {
        player.setGameMode(GameMode.SURVIVAL);
        for (int i = 0; i < 9; i++) {
            player.getInventory().clear(i);
        }
        player.getInventory().setItemInOffHand(null);

        return true;
    }

    @Override
    public void cleanup() {
        // 이 미니게임은 맵 변경이 없으므로 필요하지 않음.
    }

    @Override
    public void onPlayerInteract(PlayerInteractEvent event, JavaPlugin plugin) {
        GameManager gameManager = GameManager.getInstance();
        if (gameManager.getGameState() != GameState.RUNNING || gameManager.getCurrentStage() != 3) return;

        Player player = event.getPlayer();
        if (!player.getUniqueId().equals(gameManager.getSelectedPlayerUUID())) return;

        // 밟은 블록이 금 발판인지 확인
        if (event.getAction() == Action.PHYSICAL &&
            event.getClickedBlock() != null &&
            event.getClickedBlock().getType() == Material.LIGHT_WEIGHTED_PRESSURE_PLATE) {

            player.sendMessage(Component.text("3단계 클리어!", NamedTextColor.GOLD));
            gameManager.advanceToNextStage(player);
        }
    }

}
