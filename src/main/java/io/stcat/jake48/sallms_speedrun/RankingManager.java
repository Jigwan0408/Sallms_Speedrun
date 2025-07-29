package io.stcat.jake48.sallms_speedrun;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class RankingManager {

    private final JavaPlugin plugin;
    private final File file;
    private final FileConfiguration config;
    private final List<GameRecord> rankings = new ArrayList<>();
    private static final int MAX_RANKING_SIZE = 5;

    public RankingManager(@NotNull JavaPlugin plugin) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), "ranking.yml");
        if (!file.exists()) {
            plugin.saveResource("ranking.yml", false);
        }
        this.config = YamlConfiguration.loadConfiguration(file);
        loadRankings();
    }

    private void loadRankings() {
        // ranking.yml 파일에서 순위를 읽어와 rankings 리스트에 채우는 로직
    }

    private void saveRankings() {
        // rankings 리스트의 내용을 ranking.yml 파일에 저장하는 로직
    }

    public void addRecord(@NotNull Player player, long timeMills) {
        rankings.add(new GameRecord(player.getName(), player.getUniqueId(), timeMills));

        // 기록을 시간순으로 정렬 (오름차순)
        rankings.sort(Comparator.comparingLong(GameRecord::timeMillis));

        // 상위 5개만 출력
        if (rankings.size() >= MAX_RANKING_SIZE) {
            rankings.subList(MAX_RANKING_SIZE, rankings.size()).clear();
        }
        saveRankings();
    }

    // Tab 목록과 스코어보드를 업데이트하는 통합 메서드
    public void updatePlayerDisplay(Player player) {
        // 스코어보드 업데이트
        Scoreboard scoreboard = Bukkit.getScoreboardManager().getNewScoreboard();
        Component title = Component.text("▶ TOP 5 기록 ◀", NamedTextColor.GOLD, TextDecoration.BOLD);
        Objective objective = scoreboard.registerNewObjective("ranking", "dummy", title);
        objective.setDisplaySlot(DisplaySlot.SIDEBAR);

        if (rankings.isEmpty()) {
            objective.getScore("기록이 없음").setScore(0);
        } else {
            for (int i = 0; i < rankings.size(); i++) {
                GameRecord record = rankings.get(i);
                String timeStr = String.format("%.2f초", record.timeMillis() / 1000.0);

                // 텍스트에 순위, 기록을 표시
                String entryText = "§e" + (i + 1) + "위: §7- §a" + timeStr;

                // 스코어보드 라인은 40자를 넘을 수 없으므로 컷
                if (entryText.length() > 40) {
                    entryText = entryText.substring(0, 40);
                }

                // setScore()에는 순위를 뒤집은 값을 넣어 1위가 가장 위에 오도록 함
                objective.getScore(entryText).setScore(MAX_RANKING_SIZE - i);
            }
        }
        player.setScoreboard(scoreboard);

        // Tab 목록 Footer 업데이트
        Component footer = Component.text("\n--- TOP 5 기록 ---\n", NamedTextColor.GOLD, TextDecoration.BOLD);
        if (rankings.isEmpty()) {
            footer = footer.append(Component.text("기록이 없습니다.\n",  NamedTextColor.GRAY));
        } else {
            for (int i = 0; i < rankings.size(); i++) {
                GameRecord record = rankings.get(i);
                String timeStr = String.format("%.2f초", record.timeMillis() / 1000.0);
                Component rankLine = Component.text("§6" + (i + 1) + "위: §f" + record.playerName() + " - §a" + timeStr + "\n");
                footer = footer.append(rankLine);
            }
        }
        player.sendPlayerListFooter(footer);

    }


}
