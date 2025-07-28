package io.stcat.jake48.sallms_speedrun;

import io.stcat.jake48.sallms_speedrun.minigames.RelativeBlock;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.logging.Level;

public class StructureManager {

    private final JavaPlugin plugin;
    private final File file;
    private final FileConfiguration config;
    private final Map<String, List<RelativeBlock>> structures = new HashMap<>();

    public StructureManager(@NotNull JavaPlugin plugin) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), "structures.yml");
        if (!file.exists()) {
            plugin.saveResource("structures.yml", false);
        }
        this.config = YamlConfiguration.loadConfiguration(file);
        loadStructures();
    }

    private void loadStructures() {
        ConfigurationSection structuresSection = config.getConfigurationSection("structures");
        if (structuresSection == null) return;

        for (String structureName : structuresSection.getKeys(false)) {
            List<Map<?, ?>> blockDataList = structuresSection.getMapList(structureName);
            List<RelativeBlock> relativeBlocks = new ArrayList<>();
            for (Map<?, ?> blockData : blockDataList) {
                relativeBlocks.add(new RelativeBlock(
                        (int) blockData.get("x"),
                        (int) blockData.get("y"),
                        (int) blockData.get("z"),
                        Material.valueOf((String) blockData.get("type"))
                ));
            }
            structures.put(structureName.toLowerCase(), relativeBlocks);
        }
        plugin.getLogger().info(structures.size() + " structures loaded.");
    }

    public void saveStructure(String name, @NotNull Location pos1, @NotNull Location pos2) throws IOException {
        // 구조물 저장 시작 로그
        plugin.getLogger().info("[Structure] Starting to save structure '" + name + "'...");

        List<Map<String, Object>> blockDataList = new ArrayList<>();
        Location min = new Location(pos1.getWorld(), Math.min(pos1.getX(), pos2.getX()), Math.min(pos1.getY(), pos2.getY()), Math.min(pos1.getZ(), pos2.getZ()));

        int savedBlockCount = 0; // 저장된 블록 수를 세기 위한 카운터
        
        for (int x = 0; x <= Math.abs(pos1.getBlockX() - pos2.getBlockX()); x++) {
            for (int y = 0; y <= Math.abs(pos1.getBlockY() - pos2.getBlockY()); y++) {
                for (int z = 0; z <= Math.abs(pos1.getBlockZ() - pos2.getBlockZ()); z++) {
                    Block block = min.clone().add(x, y, z).getBlock();
                    if (!block.getType().isAir()) {
                        savedBlockCount++;

                        if (plugin.getLogger().isLoggable(Level.INFO)) {
                            plugin.getLogger().info(String.format(
                                    " -> Saving block #%d Type=%s at (rel: %d, %d, %d)",
                                    savedBlockCount,
                                    block.getType().name(),
                                    x, y, z
                            ));
                        }

                        Map<String, Object> blockData = new HashMap<>();
                        blockData.put("x", x);
                        blockData.put("y", y);
                        blockData.put("z", z);
                        blockData.put("type", block.getType().name());
                        blockDataList.add(blockData);
                    }
                }
            }
        }
        config.set("structures." + name, blockDataList);
        config.save(file);

        // 최종적으로 몇 개의 블록이 저장되었는지 요약
        plugin.getLogger().info("[Structure] Successfully saved structure '" + name + "' with " + savedBlockCount + " blocks.");

        // 메모리에도 즉시 로드
        structures.put(name.toLowerCase(), loadStructuresFromFile(name));
    }

    // loadStructures 내부 로직 재활용
    private @NotNull List<RelativeBlock> loadStructuresFromFile(String name) {
        List<Map<?, ?>> blockDataList = config.getMapList("structures." + name);
        List<RelativeBlock> relativeBlocks = new ArrayList<>();
        for (Map<?, ?> blockData : blockDataList) {
            relativeBlocks.add(new RelativeBlock(
                    (int) blockData.get("x"),
                    (int) blockData.get("y"),
                    (int) blockData.get("z"),
                    Material.valueOf((String) blockData.get("type"))
            ));
        }
        return relativeBlocks;
    }

    public List<RelativeBlock> getStructures(@NotNull String name) {
        return structures.get(name.toLowerCase());
    }

    public Set<String> getStructureNames() {
        return structures.keySet();
    }
}
