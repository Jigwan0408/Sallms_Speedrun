package io.stcat.jake48.sallms_Speedrun.listeners;

import io.stcat.jake48.sallms_Speedrun.GameManager;
import io.stcat.jake48.sallms_Speedrun.GameState;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class GameListener implements Listener {

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent e) {
        // 게임이 대기 중일 때만 참가자 목록에 추가
        if (GameManager.getInstance().getGameState() == GameState.WAITING) {
            GameManager.getInstance().addPlayer(e.getPlayer());
            Component inmsg = Component.text("게임에 참가했습니다.", NamedTextColor.GREEN);
            e.getPlayer().sendMessage(inmsg);
        } else {
            // 게임이 진행 중이라면 관전 모드로 만들거나, 로비로 보내기
            Component outmsg = Component.text("현재 게임이 진행 중입니다.", NamedTextColor.RED);
            e.getPlayer().sendMessage(outmsg);
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent e) {
        // 플레이어가 나가면 항상 목록에서 제거
        GameManager.getInstance().removePlayer(e.getPlayer());
    }

}
