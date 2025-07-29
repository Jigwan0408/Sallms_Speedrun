package io.stcat.jake48.sallms_speedrun.listeners;

import io.stcat.jake48.sallms_speedrun.GameManager;
import io.stcat.jake48.sallms_speedrun.GameState;
import io.stcat.jake48.sallms_speedrun.Sallms_Speedrun;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class GameListener implements Listener {

    private final Sallms_Speedrun plugin;
    public GameListener(Sallms_Speedrun plugin) { this.plugin = plugin; }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        // 게임이 대기 중일 때만 참가자 목록에 추가
        if (GameManager.getInstance().getGameState() == GameState.WAITING) {
            GameManager.getInstance().addPlayer(event.getPlayer());
            Component inmsg = Component.text("게임에 참가했습니다.", NamedTextColor.GREEN);
            event.getPlayer().sendMessage(inmsg);

            // 플레이어가 접속하면 랭킹 정보 표시
            plugin.getRankingManager().updatePlayerDisplay(event.getPlayer());
        } else {
            // 게임이 진행 중이라면 관전 모드로 만들거나, 로비로 보내기
            Component outmsg = Component.text("현재 게임이 진행 중입니다.", NamedTextColor.RED);
            event.getPlayer().sendMessage(outmsg);
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        // 플레이어가 나가면 항상 목록에서 제거
        GameManager.getInstance().removePlayer(event.getPlayer());
    }

}
