package io.stcat.jake48.sallms_Speedrun;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;

public class ScoreboardManager {

    // 플레이어에게 최종 기록 스코어보드 표시
    // @param player 스코어보드를 볼 플레이어
    // @param finalTime 표시할 최종 기록 문자열

    public static void displayFinalScore(Player player, String finalTime) {
        Scoreboard scoreboard = Bukkit.getScoreboardManager().getNewScoreboard();

        // 스코어보드 제목
        Component title = Component.text("게임 기록",  NamedTextColor.GOLD, TextDecoration.BOLD);
        Objective objective = scoreboard.registerNewObjective("FinalScore", "dummy", title);
        objective.setDisplaySlot(DisplaySlot.SIDEBAR);

        // 스코어보드 내용
        objective.getScore(" ").setScore(2); // 빈 줄
        objective.getScore("§a최종 기록: ").setScore(1);
        objective.getScore("§e" + finalTime).setScore(0);

        player.setScoreboard(scoreboard);
    }

    // 플레이어의 스코어보드 초기화
    // @param player 스코어보드를 지울 플레이어
    public static void clearScoreboard(Player player) {
        player.setScoreboard(Bukkit.getScoreboardManager().getMainScoreboard());
    }

}
