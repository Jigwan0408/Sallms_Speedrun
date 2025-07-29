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
import java.util.UUID;

public final class Sallms_Speedrun extends JavaPlugin {

    // Location을 저장할 HashMap
    private final Map<Integer, Location> stageLocations = new HashMap<>();
    // 건축물 위치 저장을 위한 Map 객체, Getter
    private final Map<UUID, Location> pos1Map = new HashMap<>();
    private final Map<UUID, Location> pos2Map = new HashMap<>();
    private StructureManager structureManager;
    private RankingManager rankingManager;

    public StructureManager getStructureManager() {
        return structureManager;
    }

    public RankingManager getRankingManager() {
        return rankingManager;
    }

    public Map<UUID, Location> getPos1Map() {
        return pos1Map;
    }

    public Map<UUID, Location> getPos2Map() {
        return pos2Map;
    }

    @Override
    public void onEnable() {
        // GameManager에 메인 클래스 인스턴스(this)를 전달하여 초기화
        GameManager.getInstance().init(this);

        this.structureManager = new StructureManager(this);
        this.rankingManager = new RankingManager(this);

        // config.yml 파일이 없으면 기본 파일을 생성
        saveDefaultConfig();

        // 설정 파일에서 좌표를 불러오는 메서드 호출
        loadStageLocation();

        // 시작 로그 메서드 호출
        printStartupMessage();

        // 명령어 등록
        getCommand("sallms").setExecutor(new GameCommand(this));

        // 리스너 등록
        getServer().getPluginManager().registerEvents(new GameListener(this), this);
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
            // "teleport-point" 같은 하위 경로가 없는, 단순한 구조의 스테이지만 불러옴
            if (getConfig().isConfigurationSection("stages." + key) && !getConfig().contains("stages." + key + ".teleport-point")) {
                Location loc = loadLocationFromPath("stages." + key);
                if (loc != null) {
                    stageLocations.put(Integer.parseInt(key), loc);
                }
            }
            getLogger().info(stageLocations.size() + " simple stage locations loaded.");
        }

        getLogger().info(stageLocations.size() + " stages have been loaded!");
    }

    // 어떤 단계든 '텔레포트' 위치를 가져오는 최종 메서드
    public Location getStageTeleportLocation(int stageNum) {
        // 4단계처럼 복합 구조("teleport-point" 키)가 있는지 먼저 확인
        String complexPath = "stages." + stageNum + ".teleport-point";
        if (getConfig().contains(complexPath)) {
            return loadLocationFromPath(complexPath);
        }
        // 없다면 1~3단계처럼 단순한 구조로 간주하고 기존 Map에서 가져옴
        return stageLocations.get(stageNum);
    }

    /**
     * 특정 단계의 하위 키(key)에 해당하는 위치를 가져오는 헬퍼 메서드
     *
     * @param stageNum 단계 번호
     * @param key      하위 키 이름
     *                 (예: 4단계의 "sample-area-pos" 위치)
     */
    public Location getStageLocation(int stageNum, String key) {
        String path = "stages." + stageNum + "." + key;
        return loadLocationFromPath(path);
    }

    /**
     * 특정 단계의 하위 키(key)에 해당하는 위치를 가져옴
     * (예: 4단계의 "sample-area-pos" 위치)
     *
     * @param stageNumber 단계 번호
     * @return 성공 시 Location 객체, 실패 시 null
     */
    public Location getStageLocation(int stageNumber) {
        // 4단계처럼 복잡한 구조("teleport-point" 키)가 있는지 확인
        String complexPath = "stages." + stageNumber + ".teleport-point";
        if (getConfig().contains(complexPath)) {
            // 복잡한 구조라면 하위 경로에서 직접 위치를 불러옴
            return loadLocationFromPath(complexPath);
        }
        // 없다면 1~3단계처럼 단순한 구조로 간주하고 기존 Map에서 가져옴
        return stageLocations.get(stageNumber);
    }

    /**
     * config.yml의 특정 경로(path)에서 Location 객체를 불러옴
     *
     * @param path 확인할 경로 (예: "stage.4.teleport-point")
     * @return 성공 시 Location 객체, 실패 시 null
     */
    private Location loadLocationFromPath(String path) {
        // 경로 자체가 없으면 null 반환
        if (!getConfig().contains(path)) return null;

        World world = Bukkit.getWorld(getConfig().getString(path + ".world", "world"));
        if (world == null) return null; // 월드가 존재하지 않으면 null 반환

        double x = getConfig().getDouble(path + ".x");
        double y = getConfig().getDouble(path + ".y");
        double z = getConfig().getDouble(path + ".z");
        float yaw = (float) getConfig().getDouble(path + ".yaw");
        float pitch = (float) getConfig().getDouble(path + ".pitch");

        return new Location(world, x, y, z, yaw, pitch);
    }

    @Override
    public void onDisable() {
        getLogger().info("[" + this.getDescription().getName() + "] Plugin has been disabled!");
    }
}
