package io.stcat.jake48.sallms_speedrun;

import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.extent.clipboard.BlockArrayClipboard;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.extent.clipboard.io.*;
import com.sk89q.worldedit.function.operation.ForwardExtentCopy;
import com.sk89q.worldedit.function.operation.Operation;
import com.sk89q.worldedit.function.operation.Operations;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldedit.session.ClipboardHolder;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class SchematicManager {

    private final JavaPlugin plugin;
    private final File schematicFolder;

    public SchematicManager(@NotNull JavaPlugin plugin) {
        this.plugin = plugin;
        // 스케메틱을 플러그인 폴더 안에 저장하도록 경로 설정
        this.schematicFolder = new File(plugin.getDataFolder(), "schematics");
        if (!this.schematicFolder.exists()) {
            schematicFolder.mkdir();
        }
    }

    public boolean save(String name, @NotNull Location pos1, Location pos2) {
        try {
            CuboidRegion region = new CuboidRegion(BukkitAdapter.adapt(pos1.getWorld()),
                    BukkitAdapter.asBlockVector(pos1),
                    BukkitAdapter.asBlockVector(pos2));

            BlockArrayClipboard clipboard = new BlockArrayClipboard(region);

            try (EditSession editSession = WorldEdit.getInstance().newEditSession(region.getWorld())) {
                ForwardExtentCopy copyOperation = new ForwardExtentCopy(
                        editSession, region, clipboard, region.getMinimumPoint()
                );
                Operations.complete(copyOperation);
            }

            File file = new File(schematicFolder, name + ".schem");
            try (ClipboardWriter writer = BuiltInClipboardFormat.SPONGE_V3_SCHEMATIC.getWriter(new FileOutputStream(file))) {
                writer.write(clipboard);
            }
        } catch (IOException | WorldEditException e) {
            plugin.getLogger().severe("Failed to save schematic '" + name + "'!");
            e.printStackTrace();
            return false;
        }

        return true;
    }

    public Clipboard load(String name) throws IOException {
        File file = new File(schematicFolder, name + ".schem");
        if (!file.exists()) return null;

        ClipboardFormat format = ClipboardFormats.findByFile(file);
        try (ClipboardReader reader = format.getReader(new FileInputStream(file))) {
            return reader.read();
        }
    }

    public boolean paste(String name, Location targetLocation) {
        try {
            Clipboard clipboard = load(name);
            if (clipboard == null) {
                plugin.getLogger().warning("Could not load schematic " + name);
                return false;
            }

            try (EditSession editSession = WorldEdit.getInstance().newEditSession(BukkitAdapter.adapt(targetLocation.getWorld()))) {
                Operation operation = new ClipboardHolder(clipboard)
                        .createPaste(editSession)
                        .to(BukkitAdapter.asBlockVector(targetLocation))
                        .ignoreAirBlocks(true)
                        .build();
                Operations.complete(operation);
            }
        } catch (IOException | WorldEditException e) {
            plugin.getLogger().severe("Failed to paste schematic '" + name + "'!");
            e.printStackTrace();
            return false;
        }

        return true;
    }

    public List<String> getSchematicNames() {
        File[] files = schematicFolder.listFiles((dir, name) -> name.toLowerCase().endsWith(".schem"));
        if (files == null) return Collections.emptyList();
        return Arrays.stream(files)
                .map(file -> file.getName().replace(".schem", ""))
                .collect(Collectors.toList());
    }

}
