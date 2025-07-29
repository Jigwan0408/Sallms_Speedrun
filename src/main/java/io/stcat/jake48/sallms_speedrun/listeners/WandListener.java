package io.stcat.jake48.sallms_speedrun.listeners;

import io.stcat.jake48.sallms_speedrun.Sallms_Speedrun;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;

public class WandListener implements Listener {

    private final Sallms_Speedrun plugin;

    public WandListener(Sallms_Speedrun plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();

        // 관리자 권한이 없거나, 손에 나무 도끼를 들고 있지 않으면 무시
        if (!player.hasPermission("sallms.admin") || player.getInventory().getItemInMainHand().getType() != Material.BLAZE_ROD) {
            return;
        }

        // 오른손으로 클릭한 이벤트만 처리 (중복 방지)
        if (event.getHand() != EquipmentSlot.HAND) {
            return;
        }

        // 블록을 클릭했을 때만 작동하도록 확인
        if (event.getClickedBlock() == null) {
            return;
        }

        // 블록을 좌클릭했을 때: pos1 설정
        if (event.getAction() == Action.LEFT_CLICK_BLOCK) {
            event.setCancelled(true); // 블록이 부서지지 않도록 이벤트 취소
            Location pos1 = event.getClickedBlock().getLocation();

            plugin.getPos1Map().put(player.getUniqueId(), pos1);
            player.sendMessage(Component.text("위치 1이 설정되었습니다.: " + formatLocation(pos1), NamedTextColor.LIGHT_PURPLE));
        }
        // 블록을 우클릭했을 때: pos2 설정
        else if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            event.setCancelled(true); // 상자 등이 열리지 않도록 이벤트 취소
            Location pos2 = event.getClickedBlock().getLocation();

            plugin.getPos2Map().put(player.getUniqueId(), pos2);
            player.sendMessage(Component.text("위치 2가 설정되었습니다: " + formatLocation(pos2), NamedTextColor.LIGHT_PURPLE));
        }
    }

    private String formatLocation(Location loc) {
        return String.format("§e(X: %d, Y: %d, Z: %d)", loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
    }

}
