package io.stcat.jake48.sallms_Speedrun.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class CmdTabCompleter implements TabCompleter {

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        // 첫 번째 인자(/sallms 다음)를 입력하는 경우에만 자동완성 제안
        if (args.length == 1) {
            List<String> completions = new ArrayList<>();
            // 추천할 명령어 목록
            List<String> commands = List.of("start", "stop");

            // 현재 사용자가 입력한 내용으로 시작하는 명령어만 필터링하여 추천 목록에 추가
            for (String cmd : commands) {
                if (cmd.toLowerCase().startsWith(args[0].toLowerCase())) {
                    completions.add(cmd);
                }
            }
            return completions;
        }

        // 그 외에 경우에는 아무것도 추천하지 않음
        return null;
    }
}
