package io.stcat.jake48.sallms_speedrun.commands;

import io.stcat.jake48.sallms_speedrun.GameManager;
import io.stcat.jake48.sallms_speedrun.GameState;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class GameCommand implements CommandExecutor, TabCompleter {

    private static final String START_COMMAND = "start";
    private final JavaPlugin plugin;

    // 생성자를 통해 Main 클래스의 인스턴스를 받아옴
    public GameCommand(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {

        if (!(sender instanceof Player)) {
            sender.sendMessage("이 명령어는 플레이어만 사용할 수 있습니다.");
            return true;
        }
        Player player = (Player) sender;

        if (args.length == 0) {
            sendUsage(player);
            return true;
        }

        GameManager gameManager = GameManager.getInstance();

        switch (args[0].toLowerCase()) {
            case START_COMMAND:
                handleStart(player, args);
                break;
            case "stop":
                handStop(player);
                break;
            case "join":
                handleJoin(player);
                break;
            case "leave":
                handleLeave(player);
                break;
            default:
                sendUsage(player);
                break;
        }
        return true;

    }

    private void handleStart(Player player, String[] args) {
        if (!player.hasPermission("sallms.admin")) {
            player.sendMessage(Component.text("권한이 없습니다.", NamedTextColor.RED));
            return;
        }
        GameManager gameManager = GameManager.getInstance();

        if (args.length == 1) {
            gameManager.startGame();
        } else if (args.length == 2) {
            try {
                int stage = Integer.parseInt(args[1]);
                if (!gameManager.getGameStages().containsKey(stage)) {
                    player.sendMessage(Component.text("존재하지 않는 스테이지 번호입니다.", NamedTextColor.RED));
                    return;
                }
                player.sendMessage(Component.text("랜덤 플레이어로" + stage + "단계부터 시작합니다.", NamedTextColor.GREEN));
                gameManager.startGame(stage);
            } catch (NumberFormatException e) {
                Player targetPlayer = Bukkit.getPlayer(args[0]);
                if (targetPlayer != null) {
                    gameManager.startGame(targetPlayer, 1);
                } else {
                    player.sendMessage(Component.text("플레이어'" + args[1] + "'를 찾을 수 없습니다.", NamedTextColor.RED));
                }
            }
        } else if (args.length == 3) {
            Player targetPlayer = Bukkit.getPlayer(args[1]);
            if (targetPlayer != null) {
                player
            }
        }
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
        final List<String> completions = new ArrayList<>();
        final String currentArg = args[args.length - 1].toLowerCase();

        // 첫 번째 인자 자동완성 (/sallms 다음)
        if (args.length == 1) {
            List<String> subcommands = List.of("start", "stop", "join", "leave");
            // 현재 입력 중인 내용으로 시작하는 명령어만 필터링
            return subcommands.stream()
                    .filter(s -> s.toLowerCase().startsWith(currentArg))
                    .collect(Collectors.toList());
        }

        // start 명령어의 다음 인자들 자동완성
        if (args[0].equalsIgnoreCase("start")) {
            // /sallms start <인자>
            if (args.length == 2) {
                // 제안 목록에 스테이지 번호 추가
                GameManager.getInstance().getGameStages().keySet().stream()
                        .map(String::valueOf)
                        .forEach(completions::add);

                // 제안 목록에 온라인 플레이어 이름 추가
                Bukkit.getOnlinePlayers().stream()
                        .map(Player::getName)
                        .forEach(completions::add);

                // 현재 입력값으로 시작하는 것들만 최종 필터링하여 반환
                return completions.stream()
                        .filter(s -> s.toLowerCase().startsWith(currentArg))
                        .collect(Collectors.toList());
            }
            // /sallms start <플레이어> <인자>
            else if (args.length == 3) {
                // 두 번째 인자가 실제 플레이어 이름일 경우에만 스테이지 번호를 제안
                if (Bukkit.getPlayer(args[1]) != null) {
                    return GameManager.getInstance().getGameStages().keySet().stream()
                            .map(String::valueOf)
                            .filter(s -> s.toLowerCase().startsWith(currentArg))
                            .collect(Collectors.toList());
                }
            }
        }

        // 위 조건에 해당하지 않는 모든 경우, 빈 리스트 반환
        return List.of();
    }
}
