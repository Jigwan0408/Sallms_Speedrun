package io.stcat.jake48.sallms_speedrun.commands;

import io.stcat.jake48.sallms_speedrun.GameManager;
import io.stcat.jake48.sallms_speedrun.GameState;
import io.stcat.jake48.sallms_speedrun.Sallms_Speedrun;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.logging.Level;

public class GameCommand implements CommandExecutor, TabCompleter {

    private final JavaPlugin plugin;
    private static final String START_COMMAND = "start";
    private final Map<UUID, Location> pos1Map = new HashMap<>();
    private final Map<UUID, Location> pos2Map = new HashMap<>();

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

        switch (args[0].toLowerCase()) {
            case START_COMMAND:
                handleStart(player, args);
                break;
            case "stop":
                handleStop(player);
                break;
            case "join":
                handleJoin(player);
                break;
            case "leave":
                handleLeave(player);
                break;
            case "pos1":
                handlePos1(player);
                break;
            case "pos2":
                handlePos2(player);
                break;
            case "savesample":
                handleSaveSample(player, args);
                break;
            default:
                sendUsage(player);
                break;
        }
        return true;
    }

    private void handleStart(@NotNull Player player, String[] args) {
        if (!player.hasPermission("sallms.admin")) {
            player.sendMessage(Component.text("권한이 없습니다.", NamedTextColor.RED));
            return;
        }

        if (args.length == 1) {
            // 인자가 없을 때
            exeStartRandom();
        } else if  (args.length == 2) {
            // 인자가 하나일 때
            exeStartOneArg(player, args[1]);
        } else if  (args.length == 3) {
            // 인자가 두 개일 때
            exeStartTwoArg(player, args[1], args[2]);
        } else {
            player.sendMessage(Component.text("사용법: /sallms start <플레이어> <단계>"));
        }

    }

    /** /sallms start (랜덤 플레이어, 1단계부터) */
    private void exeStartRandom() {
        GameManager.getInstance().startGame();
    }

    /** /sallms start <플레이어> 또는 /sallms start <단계> */
    private void exeStartOneArg(Player sender, String arg) {
        GameManager gameManager = GameManager.getInstance();
        try {
            int stage = Integer.parseInt(arg);
            if (!gameManager.getGameStages().containsKey(stage)) {
                sender.sendMessage(Component.text("존재하지 않는 스테이지 번호입니다.", NamedTextColor.RED));
                return;
            }
            sender.sendMessage(Component.text("랜덤 플레이어로 " + stage + "단계부터 시작합니다.", NamedTextColor.GREEN));
            gameManager.startGame(stage);
        } catch (NumberFormatException e) {
            Player targetPlayer = Bukkit.getPlayer(arg);
            if (targetPlayer != null) {
                gameManager.startGame(targetPlayer, 1);
            } else {
                sender.sendMessage(Component.text("플레이어 '" + arg + "'를 찾을 수 없거나, 잘못된 숫자입니다.", NamedTextColor.RED));
            }
        }
    }

    /** /sallms start <플레이어> <단계> */
    private void exeStartTwoArg(Player sender, String playerArg, String stageArg) {
        GameManager gameManager = GameManager.getInstance();
        Player targetPlayer = Bukkit.getPlayer(playerArg);

        if (targetPlayer == null) {
            sender.sendMessage(Component.text("플레이어 '" + playerArg + "'를 찾을 수 없습니다.", NamedTextColor.RED));
            return;
        }
        try {
            int stage = Integer.parseInt(stageArg);
            if (!gameManager.getGameStages().containsKey(stage)) {
                sender.sendMessage(Component.text("존재하지 않는 스테이지 번호입니다.", NamedTextColor.RED));
                return;
            }
            gameManager.startGame(targetPlayer, stage);
        } catch (NumberFormatException e) {
            sender.sendMessage(Component.text("단계 번호는 숫자여야 합니다.", NamedTextColor.RED));
        }
    }

    private void handleStop(@NotNull Player player) {
        if (!player.hasPermission("sallms.admin")) {
            player.sendMessage(Component.text("권한이 없습니다.", NamedTextColor.RED));
            return;
        }
        GameManager gameManager = GameManager.getInstance();

        // 게임 진행 중 여부 검사
        if (gameManager.getGameState() != GameState.RUNNING) {
            // 게임이 진행(RUNNING)이 아닐 경우, 메시지 송출 후 반환
            player.sendMessage(Component.text("게임이 진행 중이지 않습니다.", NamedTextColor.RED));
            return;
        }

        // 게임이 진행 중일 경우 stopGame() 호출
        gameManager.stopGame();
        player.sendMessage(Component.text("게임을 중단했습니다.", NamedTextColor.RED));
    }

    private void handleJoin(Player player) {
        GameManager gameManager = GameManager.getInstance();
        if (gameManager.getGameState() != GameState.WAITING) {
            player.sendMessage(Component.text("게임이 진행 중이라 참여할 수 없습니다.",  NamedTextColor.RED));
            return;
        }

        // 입장 성공 여부 검사
        if (gameManager.addPlayer(player)) {
            // 성공한 경우
            Bukkit.broadcast(Component.text(player.getName() + "님이 쌀멋 게임에 참가했습니다. (현재 인원: " + gameManager.getPlayers().size() + "명)", NamedTextColor.YELLOW));
        } else {
            // 실패한 경우
            player.sendMessage(Component.text("이미 게임에 참여한 상태입니다.", NamedTextColor.RED));
        }
    }

    private void handleLeave(Player player) {
        GameManager gameManager = GameManager.getInstance();

        // 퇴장 성공 여부 검사
        if (gameManager.removePlayer(player)) {
            // 성공한 경우
            Bukkit.broadcast(Component.text(player.getName() + "님이 쌀멋 게임에 나갔습니다. (현재 인원: " + gameManager.getPlayers().size() + "명)", NamedTextColor.YELLOW));
        } else {
            // 실패한 경우
            player.sendMessage(Component.text("게임에 참여하지 않은 상태입니다.", NamedTextColor.RED));
        }
    }

    private void handlePos1(@NotNull Player player) {
        if (!player.hasPermission("sallms.admin")) return;

        Location pos1 = player.getLocation();
        ((Sallms_Speedrun) plugin).getPos1Map().put(player.getUniqueId(), pos1);
        player.sendMessage(Component.text("위치 1이 현재 위치로 설정되었습니다.", NamedTextColor.WHITE));

        if (plugin.getLogger().isLoggable(Level.INFO)) {
            plugin.getLogger().info(String.format(
                    "[Structure] Player %s set pos1: World=%s, X=%.1f, Y=%.1f, Z=%.1f",
                    player.getName(),
                    pos1.getWorld().getName(),
                    pos1.getX(),
                    pos1.getY(),
                    pos1.getZ()
            ));
        }
    }

    private void handlePos2(@NotNull Player player) {
        if (!player.hasPermission("sallms.admin")) return;

        Location pos2 = player.getLocation();
        ((Sallms_Speedrun) plugin).getPos2Map().put(player.getUniqueId(), player.getLocation());
        player.sendMessage(Component.text("위치 2가 현재 위치로 설정되었습니다.", NamedTextColor.WHITE));

        if (plugin.getLogger().isLoggable(Level.INFO)) {
            plugin.getLogger().info(String.format(
                    "[Structure] Player %s set pos1: World=%s, X=%.1f, Y=%.1f, Z=%.1f",
                    player.getName(),
                    pos2.getWorld().getName(),
                    pos2.getX(),
                    pos2.getY(),
                    pos2.getZ()
            ));
        }
    }

    private void handleSaveSample(@NotNull Player player, String[] args) {
        if (!player.hasPermission("sallms.admin")) return;
        if (args.length != 2) {
            player.sendMessage(Component.text("/sallms savesample <건축물 이름>"));
            return;
        }

        Sallms_Speedrun mainPlugin = (Sallms_Speedrun) plugin;
        Location p1 = mainPlugin.getPos1Map().get(player.getUniqueId());
        Location p2 = mainPlugin.getPos2Map().get(player.getUniqueId());

        if (p1 == null || p2 == null) {
            player.sendMessage(Component.text("위치 1과 위치 2를 먼저 설정해주세요.", NamedTextColor.RED));
            return;
        }
        try {
            String structureName = args[1];
            mainPlugin.getStructureManager().saveStructure(structureName, p1, p2);
            player.sendMessage(Component.text("건축물이 성공적으로 저장되었습니다!", NamedTextColor.GREEN));
        } catch (Exception e) {
            player.sendMessage(Component.text("건축물 저장 중 오류가 발생했습니다.", NamedTextColor.RED));
            e.printStackTrace();
        }
    }

    private void sendUsage(@NotNull Player player) {
        player.sendMessage(Component.text("사용법: /sallms <start | stop | join | leave>"));
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
        final String currentArg = args[args.length - 1].toLowerCase();

        if (args.length == 1) {
            List<String> subcommands = List.of(START_COMMAND, "stop", "join", "leave");
            return subcommands.stream()
                    .filter(s -> s.toLowerCase().startsWith(currentArg))
                    .toList();
        }

        if (args[0].equalsIgnoreCase(START_COMMAND)) {
            if (args.length == 2) {
                final List<String> completions = new ArrayList<>();
                GameManager.getInstance().getGameStages().keySet().stream()
                        .map(String::valueOf)
                        .forEach(completions::add);
                Bukkit.getOnlinePlayers().stream()
                        .map(Player::getName)
                        .forEach(completions::add);
                return completions.stream()
                        .filter(s -> s.toLowerCase().startsWith(currentArg))
                        .toList();
            } else if (args.length == 3 &&  Bukkit.getPlayer(args[1]) != null) {
                return GameManager.getInstance().getGameStages().keySet().stream()
                        .map(String::valueOf)
                        .filter(s -> s.toLowerCase().startsWith(currentArg))
                        .toList();
            }
        }
        return List.of();
    }
}
