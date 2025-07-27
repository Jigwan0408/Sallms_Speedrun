package io.stcat.jake48.sallms_Speedrun.commands;

import io.stcat.jake48.sallms_Speedrun.GameManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

public class GameCommand implements CommandExecutor {

    private final JavaPlugin plugin;

    // 생성자를 통해 Main 클래스의 인스턴스를 받아옴
    public GameCommand(JavaPlugin plugin) {
        this.plugin = plugin;
    }


    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        // 관리자 권한 확인
        if (!sender.hasPermission("sall.admin")) {
            sender.sendMessage("명령어를 사용할 권한이 없습니다.");
            return true;
        }

        if (args.length == 0) {
            sender.sendMessage("사용법: /sallms <start|stop>");
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "start":
                // startGame 호출 시 plugin 인스턴스를 넘겨줌
                GameManager.getInstance().startGame();
                Component startmsg = Component.text("게임을 시작했습니다.", NamedTextColor.GREEN);
                sender.sendMessage(startmsg);
                break;
            case "stop":
                GameManager.getInstance().stopGame();
                Component stopmsg = Component.text("게임을 중단했습니다.", NamedTextColor.RED);
                sender.sendMessage(stopmsg);
                break;
            default:
                Component cmdmsg = Component.text("사용법: /sallms <start|stop>", NamedTextColor.WHITE);
                sender.sendMessage(cmdmsg);
                break;
        }

        return true;
    }

}
