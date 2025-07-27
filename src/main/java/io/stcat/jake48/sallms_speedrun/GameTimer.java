package io.stcat.jake48.sallms_speedrun;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import java.util.UUID;

public class GameTimer extends BukkitRunnable {

    private final long startTime;
    private long elapsedTime;

    public GameTimer() {
        this.startTime = System.currentTimeMillis(); // 타이머가 생성된 시간 기록
    }

    @Override
    public void run() {
        // 1. 경과 시간 계산
        elapsedTime = System.currentTimeMillis() - startTime;

        // 2. 초:밀리초 형식으로 변환
        long seconds = elapsedTime / 1000;
        long milliseconds = (elapsedTime % 1000) / 10; // 2자리로 표현하기 위해 10으로 나눔

        // 3. 액션바에 표시할 텍스트 컴포넌트 생성
        Component timerComponent = Component.text()
                .append(Component.text(String.format("%02d", seconds), NamedTextColor.GREEN))
                .append(Component.text(":", NamedTextColor.GREEN))
                .append(Component.text(String.format("%02d", milliseconds), NamedTextColor.GREEN))
                .build();

        // 4. 게임에 참여 중인 모든 플레이어에게 액션바 전송
        for (UUID playerUUID : GameManager.getInstance().getPlayers()) {
            Player player = Bukkit.getPlayer(playerUUID);
            if (player != null) {
                player.sendActionBar(timerComponent);
            }
        }
    }

    // 최종 기록을 "초.밀리초" 형태의 문자열로 반환
    // @return 최종 기록 문자열 (예: "12.34초")
    public String getFinalTime() {
        double finalSeconds = elapsedTime / 1000.0;
        return String.format("%02.2f초", finalSeconds);
    }

}
