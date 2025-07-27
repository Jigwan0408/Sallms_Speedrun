package io.stcat.jake48.sallms_speedrun;

import io.stcat.jake48.sallms_speedrun.commands.GameCommand;
import io.stcat.jake48.sallms_speedrun.listeners.GameListener;
import io.stcat.jake48.sallms_speedrun.listeners.MiniGameListener;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.Map;

public final class Sallms_Speedrun extends JavaPlugin {

    // Location을 저장할 HashMap
    private final Map<Integer, Location> stageLocations = new HashMap<>();

    @Override
    public void onEnable() {
        // GameManager에 메인 클래스 인스턴스(this)를 전달하여 초기화
        GameManager.getInstance().init(this);

        // config.yml 파일이 없으면 기본 파일을 생성
        saveDefaultConfig();

        // 설정 파일에서 좌표를 불러오는 메서드 호출
        loadStageLocation();

        // 시작 로그 메서드 호출
        printStartupMessage();

        // 명령어 등록
        getCommand("sallms").setExecutor(new GameCommand(this));

        // 리스너 등록
        getServer().getPluginManager().registerEvents(new GameListener(), this);
        getServer().getPluginManager().registerEvents(new MiniGameListener(this), this);
    }

    // 로그 메서드
    private void printStartupMessage() {
        PluginDescriptionFile pdf = this.getDescription();
        String version = pdf.getVersion();

        // 서버 로그
        getLogger().info(pdf.getName() + " has been enabled! (version: " + version + ")");
        
        // 인게임 로그
        Component message = Component.text(pdf.getName() + " has been enabled! (version: " + version + ")");
        Bukkit.broadcast(message);
    }
    
    // 좌표 정보 로드 메서드
    private void loadStageLocation() {
        // "stages" 섹션을 가져옴
        ConfigurationSection stageSection = getConfig().getConfigurationSection("stages");
        if (stageSection == null) {
            getLogger().severe("The stage section has not been set!");
            return;
        }

        // stages 섹션의 모든 키(0, 1, 2...)에 대해 반복
        for (String key : stageSection.getKeys(false)) {
            getLogger().info("-> Loading config for stage key: '" + key + "'");

            String path = "stages." + key;

            if (!getConfig().contains(path + ".world") || !getConfig().contains(path + ".x") ||
                    !getConfig().contains(path + ".y") || !getConfig().contains(path + ".z") ||
                    !getConfig().contains(path + ".yaw") || !getConfig().contains(path + ".pitch")) {

                getLogger().severe("ERROR: Stage '" + key + "' does not exist!");
                continue; // 문제가 있는 이 단계는 건너뛰고 다음 단계를 계속 로드
            }

            World world = Bukkit.getWorld(getConfig().getString(path + ".world"));
            if (world == null) {
                getLogger().severe("ERROR: Stage '" + key + "' requires world '" + getConfig().getString(path + ".world") + "' which is not loaded!");
                continue;
            }

            double x = getConfig().getDouble(path + ".x");
            double y = getConfig().getDouble(path + ".y");
            double z = getConfig().getDouble(path + ".z");
            double yaw = getConfig().getDouble(path + ".yaw");
            double pitch = getConfig().getDouble(path + ".pitch");

            Location loc = new Location(world, x, y, z, (float)yaw, (float)pitch);
            stageLocations.put(Integer.parseInt(key), loc);
        }
        getLogger().info(stageLocations.size() + " stages have been loaded!");
    }

    // GameManager를 위한 Getter 메서드
    public Location getStageLocation(int stageNumber) {
        return stageLocations.get(stageNumber);
    }

    @Override
    public void onDisable() {
        getLogger().info("[" + this.getDescription().getName() + "] Plugin has been disabled!");
    }
}
