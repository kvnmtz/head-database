package dev.kvnmtz.headdatabase.fabric.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import dev.kvnmtz.headdatabase.config.HeadDatabaseServerConfig;
import net.fabricmc.loader.api.FabricLoader;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class HeadDatabaseFabricServerConfig implements HeadDatabaseServerConfig {

    private static final Path CONFIG_FILE = FabricLoader.getInstance().getConfigDir().resolve("head-database-server.json");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static HeadDatabaseFabricServerConfig INSTANCE;

    public PermissionMode permissionMode = PermissionMode.OP_ONLY;
    public int requiredOpLevel = 2;
    public List<String> whitelist = new ArrayList<>();

    private HeadDatabaseFabricServerConfig() {}

    public static void load() {
        if (CONFIG_FILE.toFile().exists()) {
            try (FileReader reader = new FileReader(CONFIG_FILE.toFile())) {
                INSTANCE = GSON.fromJson(reader, HeadDatabaseFabricServerConfig.class);
            } catch (IOException e) {
                System.err.println("Failed to load Head Database server config, using defaults");
                INSTANCE = new HeadDatabaseFabricServerConfig();
            }
        } else {
            INSTANCE = new HeadDatabaseFabricServerConfig();

            // create the file with default values
            save();
        }
    }

    public static void save() {
        if (INSTANCE == null) return;
        try (FileWriter writer = new FileWriter(CONFIG_FILE.toFile())) {
            GSON.toJson(INSTANCE, writer);
        } catch (IOException e) {
            System.err.println("Failed to save Head Database server config");
        }
    }

    public static HeadDatabaseFabricServerConfig getInstance() {
        if (INSTANCE == null) {
            load();
        }
        return INSTANCE;
    }

    @Override
    public PermissionMode getPermissionMode() {
        return INSTANCE.permissionMode;
    }

    @Override
    public int getRequiredOpLevel() {
        return INSTANCE.requiredOpLevel;
    }

    @Override
    public List<String> getWhitelist() {
        return INSTANCE.whitelist;
    }

    @Override
    public void reload() {
        load();
    }
}
